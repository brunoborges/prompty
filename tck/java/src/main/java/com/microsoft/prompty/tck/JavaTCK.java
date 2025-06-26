package com.microsoft.prompty.tck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.prompty.GlobalConfig;
import com.microsoft.prompty.Prompty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java implementation of the Prompty Test Compatibility Kit (TCK).
 * 
 * This class provides methods to test the Prompty Java runtime against
 * a standardized set of test cases to ensure compatibility with other
 * runtime implementations.
 */
public class JavaTCK {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaTCK.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final Pattern PROMPTY_PATTERN = Pattern.compile(
        "^---\\s*\\n(?<header>.*?)\\n---\\s*\\n(?<content>.*)$", 
        Pattern.DOTALL
    );
    
    private final String tckRootPath;
    private final String runtimeName = "java";
    private final String runtimeVersion = "0.0.1-SNAPSHOT";
    
    public JavaTCK() {
        // Get the TCK root directory (parent of java/)
        String currentDir = System.getProperty("user.dir");
        if (currentDir.endsWith("java")) {
            this.tckRootPath = Paths.get(currentDir).getParent().toString();
        } else {
            // We're running from the TCK root directory
            this.tckRootPath = currentDir;
        }
        logger.info("TCK Root Path: {}", tckRootPath);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: JavaTCK <test-file> <output-file>");
            System.exit(1);
        }
        
        String testFile = args[0];
        String outputFile = args[1];
        
        JavaTCK tck = new JavaTCK();
        try {
            tck.runTests(testFile, outputFile);
        } catch (Exception e) {
            logger.error("TCK execution failed", e);
            System.err.println("TCK execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Run all TCK tests and write results to output file.
     */
    public void runTests(String testFile, String outputFile) throws IOException {
        logger.info("Starting Java TCK tests");
        logger.info("Test file: {}", testFile);
        logger.info("Output file: {}", outputFile);
        
        // Load test configuration
        String testConfigPath = Paths.get(tckRootPath, testFile).toString();
        JsonNode testConfig = objectMapper.readTree(new File(testConfigPath));
        
        List<TCKTestResult> results = new ArrayList<>();
        
        // Process each test
        JsonNode tests = testConfig.get("tests");
        if (tests != null && tests.isArray()) {
            for (JsonNode test : tests) {
                TCKTestResult result = runSingleTest(test);
                results.add(result);
                logger.info("Test {} completed with result: {}", result.getTestId(), result.getResult());
            }
        }
        
        // Create summary
        TCKTestSummary summary = new TCKTestSummary();
        summary.setRuntime(runtimeName);
        summary.setRuntimeVersion(runtimeVersion);
        summary.setTimestamp(new Date().toString());
        summary.setResults(results);
        
        // Calculate statistics
        long passed = results.stream().mapToLong(r -> "pass".equals(r.getResult()) ? 1 : 0).sum();
        long failed = results.stream().mapToLong(r -> "fail".equals(r.getResult()) ? 1 : 0).sum();
        long errors = results.stream().mapToLong(r -> "error".equals(r.getResult()) ? 1 : 0).sum();
        long skipped = results.stream().mapToLong(r -> "skip".equals(r.getResult()) ? 1 : 0).sum();
        
        summary.setTotalTests(results.size());
        summary.setPassed((int) passed);
        summary.setFailed((int) failed);
        summary.setErrors((int) errors);
        summary.setSkipped((int) skipped);
        
        // Write results
        String outputPath = Paths.get(tckRootPath, outputFile).toString();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), summary);
        
        logger.info("TCK completed. Results written to: {}", outputPath);
        logger.info("Summary: {} total, {} passed, {} failed, {} errors, {} skipped", 
                   results.size(), passed, failed, errors, skipped);
    }
    
    /**
     * Run a single test case.
     */
    private TCKTestResult runSingleTest(JsonNode test) {
        String testId = test.get("id").asText();
        String category = test.get("category").asText();
        
        TCKTestResult result = new TCKTestResult();
        result.setTestId(testId);
        result.setRuntime(runtimeName);
        result.setRuntimeVersion(runtimeVersion);
        
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Running test: {} (category: {})", testId, category);
            
            switch (category) {
                case "specification" -> runSpecificationTest(test, result);
                case "functional" -> runFunctionalTest(test, result);
                case "error" -> runErrorTest(test, result);
                default -> {
                    result.setResult("skip");
                    result.setErrorMessage("Unknown test category: " + category);
                }
            }
        } catch (Exception e) {
            logger.error("Test {} failed with exception", testId, e);
            result.setResult("error");
            result.setErrorMessage(e.getMessage());
            result.setErrorType(e.getClass().getSimpleName());
        }
        
        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs(endTime - startTime);
        
        return result;
    }
    
    /**
     * Run a specification test (parsing validation).
     */
    private void runSpecificationTest(JsonNode test, TCKTestResult result) throws IOException {
        String promptyFile = test.get("prompty_file").asText();
        String promptyPath = Paths.get(tckRootPath, promptyFile).toString();
        
        // Parse the prompty file
        Map<String, Object> parsed = parsePrompty(promptyPath, null);
        
        // Check if there's an expected parsing result to compare
        JsonNode expectedParsingNode = test.get("expected_parsing");
        if (expectedParsingNode != null) {
            String expectedPath = Paths.get(tckRootPath, expectedParsingNode.asText()).toString();
            Map<String, Object> expected = objectMapper.readValue(new File(expectedPath), 
                new TypeReference<Map<String, Object>>() {});
            
            if (compareObjects(parsed, expected)) {
                result.setResult("pass");
            } else {
                result.setResult("fail");
                result.setErrorMessage("Parsing result does not match expected output");
            }
        } else {
            // If no expected result, just verify parsing succeeded
            result.setResult("pass");
        }
        
        result.setOutput(parsed);
    }
    
    /**
     * Run a functional test (template rendering).
     */
    private void runFunctionalTest(JsonNode test, TCKTestResult result) throws IOException {
        String promptyFile = test.get("prompty_file").asText();
        String promptyPath = Paths.get(tckRootPath, promptyFile).toString();
        
        // Set up environment variables if specified
        JsonNode envVars = test.get("environment_vars");
        Map<String, String> originalEnv = new HashMap<>();
        if (envVars != null) {
            envVars.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String value = entry.getValue().asText();
                originalEnv.put(key, System.getProperty(key));
                System.setProperty(key, value);
            });
        }
        
        try {
            // Get input data
            Map<String, Object> inputData = new HashMap<>();
            JsonNode inputNode = test.get("input_data");
            if (inputNode != null) {
                inputData = objectMapper.convertValue(inputNode, new TypeReference<Map<String, Object>>() {});
            }
            
            // Render the template
            List<Map<String, Object>> rendered = renderTemplate(promptyPath, inputData, null);
            
            // Check if there's an expected rendering result to compare
            JsonNode expectedRenderingNode = test.get("expected_rendering");
            if (expectedRenderingNode != null) {
                String expectedPath = Paths.get(tckRootPath, expectedRenderingNode.asText()).toString();
                List<Map<String, Object>> expected = objectMapper.readValue(new File(expectedPath), 
                    new TypeReference<List<Map<String, Object>>>() {});
                
                if (compareObjects(rendered, expected)) {
                    result.setResult("pass");
                } else {
                    result.setResult("fail");
                    result.setErrorMessage("Rendering result does not match expected output");
                }
            } else {
                // If no expected result, just verify rendering succeeded
                result.setResult("pass");
            }
            
            result.setOutput(rendered);
            
        } finally {
            // Restore original environment variables
            originalEnv.forEach((key, value) -> {
                if (value != null) {
                    System.setProperty(key, value);
                } else {
                    System.clearProperty(key);
                }
            });
        }
    }
    
    /**
     * Run an error test (expected failure scenarios).
     */
    private void runErrorTest(JsonNode test, TCKTestResult result) throws IOException {
        String promptyFile = test.get("prompty_file").asText();
        String promptyPath = Paths.get(tckRootPath, promptyFile).toString();
        
        try {
            // Attempt to parse - this should fail
            parsePrompty(promptyPath, null);
            
            // If we get here, the test failed because no exception was thrown
            result.setResult("fail");
            result.setErrorMessage("Expected parsing to fail, but it succeeded");
            
        } catch (Exception e) {
            // Expected failure
            result.setResult("pass");
            result.setOutput(Map.of(
                "error_type", e.getClass().getSimpleName(),
                "error_message", e.getMessage()
            ));
        }
    }
    
    /**
     * Parse a Prompty file and return structured representation.
     */
    public Map<String, Object> parsePrompty(String promptyPath, Map<String, Object> globalConfig) throws IOException {
        String content = Files.readString(Paths.get(promptyPath));
        
        Matcher matcher = PROMPTY_PATTERN.matcher(content);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid prompty file format");
        }
        
        String yamlHeader = matcher.group("header");
        String templateContent = matcher.group("content");
        
        // Parse YAML frontmatter
        Map<String, Object> frontmatter = yamlMapper.readValue(yamlHeader, 
            new TypeReference<Map<String, Object>>() {});
        
        // Add template content
        frontmatter.put("template", templateContent.trim());
        
        return frontmatter;
    }
    
    /**
     * Render a Prompty template with given inputs.
     */
    public List<Map<String, Object>> renderTemplate(String promptyPath, Map<String, Object> inputs, 
                                                    Map<String, Object> globalConfig) throws IOException {
        try {
            // Load the prompty file using the Java runtime
            Prompty prompty = Prompty.load(promptyPath);
            
            // Prepare the template (this renders and parses it)
            Object prepared = prompty.prepare(inputs, false);
            
            // Convert to the expected format (list of messages)
            List<Map<String, Object>> messages = new ArrayList<>();
            
            if (prepared instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> preparedList = (List<Object>) prepared;
                for (Object item : preparedList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> message = (Map<String, Object>) item;
                        messages.add(message);
                    }
                }
            } else if (prepared instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) prepared;
                messages.add(message);
            } else if (prepared instanceof String) {
                // Simple string content - wrap in a user message
                messages.add(Map.of(
                    "role", "user",
                    "content", prepared.toString()
                ));
            }
            
            return messages;
            
        } catch (Exception e) {
            throw new IOException("Failed to render template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate inputs against prompty specification.
     */
    public List<String> validateInputs(String promptyPath, Map<String, Object> inputs) throws IOException {
        List<String> errors = new ArrayList<>();
        
        try {
            Map<String, Object> parsed = parsePrompty(promptyPath, null);
            
            // Get input specifications from the prompty
            @SuppressWarnings("unchecked")
            Map<String, Object> inputSpecs = (Map<String, Object>) parsed.get("inputs");
            
            if (inputSpecs != null) {
                for (Map.Entry<String, Object> entry : inputSpecs.entrySet()) {
                    String inputName = entry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> spec = (Map<String, Object>) entry.getValue();
                    
                    // Check if required input is present
                    Boolean isDefault = (Boolean) spec.get("is_default");
                    if (Boolean.FALSE.equals(isDefault) && !inputs.containsKey(inputName)) {
                        errors.add("Missing required input: " + inputName);
                    }
                    
                    // Validate type if specified
                    String expectedType = (String) spec.get("type");
                    Object value = inputs.get(inputName);
                    if (value != null && expectedType != null) {
                        if (!validateType(value, expectedType)) {
                            errors.add("Invalid type for input '" + inputName + "': expected " + expectedType);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            errors.add("Failed to validate inputs: " + e.getMessage());
        }
        
        return errors;
    }
    
    /**
     * Validate that a value matches the expected type.
     */
    private boolean validateType(Object value, String expectedType) {
        return switch (expectedType.toLowerCase()) {
            case "string" -> value instanceof String;
            case "number", "int", "integer" -> value instanceof Number;
            case "boolean", "bool" -> value instanceof Boolean;
            case "array", "list" -> value instanceof List;
            case "object", "dict", "map" -> value instanceof Map;
            default -> true; // Unknown type, assume valid
        };
    }
    
    /**
     * Compare two objects for equality (deep comparison).
     */
    private boolean compareObjects(Object obj1, Object obj2) {
        try {
            String json1 = objectMapper.writeValueAsString(obj1);
            String json2 = objectMapper.writeValueAsString(obj2);
            return json1.equals(json2);
        } catch (Exception e) {
            logger.warn("Failed to compare objects", e);
            return false;
        }
    }
}
