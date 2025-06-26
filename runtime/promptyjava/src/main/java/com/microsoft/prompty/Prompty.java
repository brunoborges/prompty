// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See LICENSE in the project root for license information.
package com.microsoft.prompty;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.prompty.invoker.Invoker;
import com.microsoft.prompty.invoker.InvokerFactory;
import com.microsoft.prompty.invoker.InvokerType;
import com.microsoft.prompty.model.Connection;
import com.microsoft.prompty.model.Input;
import com.microsoft.prompty.model.Metadata;
import com.microsoft.prompty.model.Model;
import com.microsoft.prompty.model.Output;
import com.microsoft.prompty.model.PropertyType;
import com.microsoft.prompty.model.Template;
import com.microsoft.prompty.model.Tool;
import com.microsoft.prompty.util.DictionaryUtils;
import com.microsoft.prompty.util.FileUtils;
import com.microsoft.prompty.util.Normalizer;
import com.microsoft.prompty.util.PropertyUtils;

/**
 * Defines a Prompty template which can be used to represent a prompt template or an agent template.
 */
public class Prompty {
    
    // Regular expression to parse prompty files with YAML frontmatter
    private static final Pattern PROMPTY_PATTERN = Pattern.compile(
        "^---\\s*\\n(?<header>.*?)\\n---\\s*\\n(?<content>.*)$", 
        Pattern.DOTALL
    );
    
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    // Properties from the C# implementation
    private String id;
    private String version = "";
    private String name = "";
    private String description = "";
    private Metadata metadata;
    private Model model;
    private Map<String, Input> inputs = new HashMap<>();
    private Map<String, Output> outputs = new HashMap<>();
    private Template template = new Template();
    private List<Tool> tools = new ArrayList<>();
    private String base = "";
    private String path;
    private String content = "";

    // Constructors
    public Prompty() {}

    /**
     * Load a prompty file using the provided file path.
     * @param path File path to the prompty file
     * @param configuration Id of the configuration to use
     * @return Loaded Prompty instance
     */
    public static Prompty load(String path, String configuration) {
        try {
            String text = FileUtils.readAllText(path);
            
            Map<String, Object> globalConfig = GlobalConfig.load(
                Paths.get(path).getParent().toString(), configuration);
            if (globalConfig == null) {
                globalConfig = new HashMap<>();
            }
            globalConfig = Normalizer.normalize(globalConfig, path);
            
            Map<String, Object> frontmatter = loadRaw(text, globalConfig, path);
            return convert(frontmatter, path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompty file: " + path, e);
        }
    }

    /**
     * Load a prompty file using the provided file path with default configuration.
     * @param path File path to the prompty file
     * @return Loaded Prompty instance
     */
    public static Prompty load(String path) {
        return load(path, "default");
    }

    /**
     * Load a prompty file using the provided file path asynchronously.
     * @param path File path to the prompty file
     * @param configuration Id of the configuration to use
     * @return CompletableFuture with loaded Prompty instance
     */
    public static CompletableFuture<Prompty> loadAsync(String path, String configuration) {
        return CompletableFuture.supplyAsync(() -> load(path, configuration));
    }

    /**
     * Load a prompty file using the provided file path asynchronously with default configuration.
     * @param path File path to the prompty file
     * @return CompletableFuture with loaded Prompty instance
     */
    public static CompletableFuture<Prompty> loadAsync(String path) {
        return loadAsync(path, "default");
    }

    /**
     * Load a prompty file using the provided text content.
     * @param text Content of the prompty file
     * @param globalConfig Global configuration to use
     * @param path Optional file path to the prompty file
     * @return Loaded Prompty instance
     */
    public static Prompty load(String text, Map<String, Object> globalConfig, String path) {
        String parentPath = (path != null && Paths.get(path).toFile().exists()) 
            ? Paths.get(path).getParent().toString() 
            : System.getProperty("user.dir");
        
        Map<String, Object> frontmatter = loadRaw(text, globalConfig, parentPath);
        return convert(frontmatter, path);
    }

    /**
     * Get sample inputs for this prompty.
     * @return Map of sample input values
     */
    public Map<String, Object> getSample() {
        Map<String, Object> sample = new HashMap<>();
        if (inputs == null) {
            return sample;
        }

        for (Map.Entry<String, Input> entry : inputs.entrySet()) {
            Input input = entry.getValue();
            if (input.getSample() != null) {
                sample.put(entry.getKey(), input.getSample());
            } else if (input.getDefaultValue() != null) {
                sample.put(entry.getKey(), input.getDefaultValue());
            }
        }
        return sample;
    }

    /**
     * Validate and clean input parameters.
     * @param inputs Input parameters to validate
     * @param mergeSample Whether to merge with sample values
     * @return Validated and cleaned input map
     */
    public Map<String, Object> validateInputs(Object inputs, boolean mergeSample) {
        Map<String, Object> cleanInputs = new HashMap<>();

        if (inputs != null) {
            cleanInputs = DictionaryUtils.toParamDictionary(inputs);
        }

        if (mergeSample) {
            cleanInputs = DictionaryUtils.paramHoisting(cleanInputs, getSample());
        }

        for (String key : this.inputs.keySet()) {
            if (!cleanInputs.containsKey(key)) {
                throw new IllegalArgumentException("Missing required input '" + key + "'");
            }
        }

        return cleanInputs;
    }

    /**
     * Prepare the prompty for execution by rendering and parsing.
     * @param inputs Input parameters
     * @param mergeSample Whether to merge with sample values
     * @return Prepared content ready for execution
     */
    public Object prepare(Object inputs, boolean mergeSample) {
        Map<String, Object> resolvedInputs = validateInputs(inputs, mergeSample);
        Object rendered = runInvoker(InvokerType.RENDERER, resolvedInputs, content);
        return runInvoker(InvokerType.PARSER, rendered);
    }

    /**
     * Prepare the prompty for execution by rendering and parsing with default parameters.
     * @return Prepared content ready for execution
     */
    public Object prepare() {
        return prepare(null, false);
    }

    /**
     * Prepare the prompty for execution asynchronously.
     * @param inputs Input parameters
     * @param mergeSample Whether to merge with sample values
     * @return CompletableFuture with prepared content
     */
    public CompletableFuture<Object> prepareAsync(Object inputs, boolean mergeSample) {
        Map<String, Object> resolvedInputs = validateInputs(inputs, mergeSample);
        return runInvokerAsync(InvokerType.RENDERER, resolvedInputs, content)
            .thenCompose(rendered -> runInvokerAsync(InvokerType.PARSER, rendered));
    }

    /**
     * Run the prompty with prepared content.
     * @param content Prepared content
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param raw Whether to return raw results
     * @return Execution result
     */
    public Object run(Object content, Object configuration, Object parameters, boolean raw) {
        if (configuration != null && parameters != null && model == null) {
            this.model = new Model();
        }

        if (configuration != null) {
            if (model.getConnection() == null) {
                model.setConnection(new Connection());
            }
            Map<String, Object> configMap = DictionaryUtils.toParamDictionary(configuration);
            Map<String, Object> existingExtData = model.getConnection().getExtensionData();
            model.getConnection().setExtensionData(DictionaryUtils.paramHoisting(configMap, existingExtData));
        }

        if (parameters != null) {
            Map<String, Object> paramMap = DictionaryUtils.toParamDictionary(parameters);
            model.setOptions(DictionaryUtils.paramHoisting(paramMap, model.getOptions()));
        }

        Object executed = runInvoker(InvokerType.EXECUTOR, content);

        if (raw) {
            return executed;
        } else {
            return runInvoker(InvokerType.RENDERER, executed);
        }
    }

    /**
     * Run the prompty asynchronously.
     * @param content Prepared content
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param raw Whether to return raw results
     * @return CompletableFuture with execution result
     */
    public CompletableFuture<Object> runAsync(Object content, Object configuration, Object parameters, boolean raw) {
        return CompletableFuture.supplyAsync(() -> run(content, configuration, parameters, raw));
    }

    /**
     * Execute the prompty end-to-end.
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param inputs Input parameters
     * @param raw Whether to return raw results
     * @return Execution result
     */
    public Object execute(Object configuration, Object parameters, Object inputs, boolean raw) {
        Object preparedContent = prepare(DictionaryUtils.toParamDictionary(inputs), false);
        return run(preparedContent, configuration, parameters, raw);
    }

    /**
     * Execute the prompty end-to-end with default parameters.
     * @return Execution result
     */
    public Object execute() {
        return execute(null, null, null, false);
    }

    /**
     * Execute the prompty end-to-end asynchronously.
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param inputs Input parameters
     * @param raw Whether to return raw results
     * @return CompletableFuture with execution result
     */
    public CompletableFuture<Object> executeAsync(Object configuration, Object parameters, Object inputs, boolean raw) {
        return prepareAsync(DictionaryUtils.toParamDictionary(inputs), false)
            .thenCompose(content -> runAsync(content, configuration, parameters, raw));
    }

    /**
     * Static method to execute a prompty file directly.
     * @param promptyPath Path to the prompty file
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param inputs Input parameters
     * @param config Configuration name
     * @param raw Whether to return raw results
     * @return Execution result
     */
    public static Object execute(String promptyPath, Object configuration, Object parameters, 
                               Object inputs, String config, boolean raw) {
        Prompty prompt = load(promptyPath, config != null ? config : "default");
        return prompt.execute(configuration, parameters, inputs, raw);
    }

    /**
     * Static method to execute a prompty file asynchronously.
     * @param promptyPath Path to the prompty file
     * @param configuration Configuration override
     * @param parameters Parameter override
     * @param inputs Input parameters
     * @param config Configuration name
     * @param raw Whether to return raw results
     * @return CompletableFuture with execution result
     */
    public static CompletableFuture<Object> executeAsync(String promptyPath, Object configuration, 
                                                       Object parameters, Object inputs, String config, boolean raw) {
        return loadAsync(promptyPath, config != null ? config : "default")
            .thenCompose(prompt -> prompt.executeAsync(configuration, parameters, inputs, raw));
    }

    // Private helper methods
    
    private String getInvokerName(InvokerType type) {
        return switch (type) {
            case RENDERER -> template != null ? template.getFormat() : null;
            case PARSER -> template != null && model != null ? 
                template.getParser() + "." + model.getApi() : null;
            case EXECUTOR, PROCESSOR -> model != null && model.getConnection() != null ? 
                model.getConnection().getType() : null;
        };
    }

    private Object runInvoker(InvokerType type, Object input, Object alt) {
        String invokerType = getInvokerName(type);

        if (invokerType == null) {
            throw new RuntimeException("Invalid invoker type " + invokerType);
        }

        if ("NOOP".equals(invokerType)) {
            return input;
        }

        Invoker invoker = InvokerFactory.getInstance().createInvoker(invokerType, type, this);
        if (invoker != null) {
            return invoker.invoke(input);
        }

        return alt != null ? alt : input;
    }

    private Object runInvoker(InvokerType type, Object input) {
        return runInvoker(type, input, null);
    }

    private CompletableFuture<Object> runInvokerAsync(InvokerType type, Object input, Object alt) {
        String invokerType = getInvokerName(type);

        if (invokerType == null) {
            return CompletableFuture.failedFuture(
                new RuntimeException("Invalid invoker type " + invokerType));
        }

        if ("NOOP".equals(invokerType)) {
            return CompletableFuture.completedFuture(input);
        }

        Invoker invoker = InvokerFactory.getInstance().createInvoker(invokerType, type, this);
        if (invoker != null) {
            return invoker.invokeAsync(input);
        }

        return CompletableFuture.completedFuture(alt != null ? alt : input);
    }

    private CompletableFuture<Object> runInvokerAsync(InvokerType type, Object input) {
        return runInvokerAsync(type, input, null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadRaw(String promptyContent, Map<String, Object> globalConfig, String path) {
        // Parse the YAML frontmatter and content from the prompty template
        Matcher matcher = PROMPTY_PATTERN.matcher(promptyContent);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid prompty template. Header and content could not be parsed.");
        }

        String header = matcher.group("header");
        if (header == null || header.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid prompty template. Header is empty.");
        }

        String content = matcher.group("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid prompty template. Content is empty.");
        }

        try {
            Map<String, Object> frontmatter = yamlMapper.readValue(header, Map.class);

            // Frontmatter normalization
            if (path != null) {
                frontmatter = Normalizer.normalize(frontmatter, Paths.get(path).toAbsolutePath().toString());
            }

            // Model configuration hoisting
            if (!frontmatter.containsKey("model")) {
                frontmatter.put("model", new HashMap<String, Object>());
            }

            Map<String, Object> modelDict = (Map<String, Object>) frontmatter.get("model");
            if (modelDict == null) {
                modelDict = new HashMap<>();
                frontmatter.put("model", modelDict);
            }

            Object configValue = modelDict.get("configuration");
            if (configValue instanceof Map) {
                // Parameter hoisting
                modelDict.put("configuration", 
                    DictionaryUtils.paramHoisting((Map<String, Object>) configValue, globalConfig));
            } else {
                // Empty - use global configuration
                modelDict.put("configuration", globalConfig);
            }

            frontmatter.put("content", content);
            return frontmatter;

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse YAML frontmatter", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Prompty convert(Map<String, Object> frontmatter, String path) {
        Prompty prompty = new Prompty();

        // Metadata
        prompty.setId((String) frontmatter.get("id"));
        // Handle version as either String or Number and convert to String
        Object versionObj = frontmatter.getOrDefault("version", "");
        prompty.setVersion(versionObj != null ? versionObj.toString() : "");
        prompty.setName((String) frontmatter.getOrDefault("name", ""));
        prompty.setDescription((String) frontmatter.getOrDefault("description", ""));
        prompty.setMetadata(convertToMetadata(frontmatter));

        // Model settings
        prompty.setModel(convertToModel((Map<String, Object>) frontmatter.get("model")));

        // Properties
        prompty.setInputs(convertToInputs((Map<String, Object>) frontmatter.get("inputs")));
        prompty.setOutputs(convertToOutputs(frontmatter.get("outputs")));

        // Template
        prompty.setTemplate(convertToTemplate((Map<String, Object>) frontmatter.get("template")));

        // Tools
        prompty.setTools(convertToTools((List<Map<String, Object>>) frontmatter.get("tools")));

        // Base
        prompty.setBase((String) frontmatter.getOrDefault("base", ""));

        // Internals
        if (path != null) {
            prompty.setPath(Paths.get(path).toAbsolutePath().toString());
        }
        prompty.setContent((String) frontmatter.getOrDefault("content", ""));

        return prompty;
    }

    // Helper conversion methods would go here...
    // I'll implement them in the next files to keep this manageable

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public Model getModel() { return model; }
    public void setModel(Model model) { this.model = model; }

    public Map<String, Input> getInputs() { return inputs; }
    public void setInputs(Map<String, Input> inputs) { this.inputs = inputs; }

    public Map<String, Output> getOutputs() { return outputs; }
    public void setOutputs(Map<String, Output> outputs) { this.outputs = outputs; }

    public Template getTemplate() { return template; }
    public void setTemplate(Template template) { this.template = template; }

    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools; }

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @SuppressWarnings("unchecked")
    private static Metadata convertToMetadata(Map<String, Object> frontmatter) {
        Map<String, Object> metadataDict = DictionaryUtils.getConfig(frontmatter, "metadata");
        
        if (metadataDict.isEmpty()) {
            // Fallback to top-level authors and tags
            return new Metadata(
                DictionaryUtils.getList(frontmatter, "authors"),
                DictionaryUtils.getList(frontmatter, "tags")
            );
        }
        
        return new Metadata(
            DictionaryUtils.getList(metadataDict, "authors"),
            DictionaryUtils.getList(metadataDict, "tags")
        );
    }

    @SuppressWarnings("unchecked")
    private static Model convertToModel(Map<String, Object> dictionary) {
        if (dictionary == null) {
            return null;
        }

        Map<String, Object> options = DictionaryUtils.getConfig(dictionary, "options");
        if (options.isEmpty()) {
            options = DictionaryUtils.getConfig(dictionary, "parameters");
        }
        
        String modelId = DictionaryUtils.getValue(options, "model_id");

        Model model = new Model();
        model.setId(DictionaryUtils.getValue(dictionary, "id", modelId));
        model.setApi(DictionaryUtils.getValue(dictionary, "api", Model.DEFAULT_API));
        model.setOptions(options);
        model.setConnection(convertToConnection(dictionary));
        
        return model;
    }

    @SuppressWarnings("unchecked")
    private static Connection convertToConnection(Map<String, Object> model) {
        Map<String, Object> dictionary = DictionaryUtils.getConfig(model, "connection");
        if (dictionary.isEmpty()) {
            dictionary = DictionaryUtils.getConfig(model, "configuration");
        }
        if (dictionary.isEmpty()) {
            return null;
        }

        Connection connection = new Connection();
        connection.setType(DictionaryUtils.getValue(dictionary, "type"));
        connection.setServiceId(DictionaryUtils.getValue(dictionary, "service_id"));
        
        // Set extension data (all properties except type and service_id)
        Map<String, Object> extensionData = new HashMap<>(dictionary);
        extensionData.remove("type");
        extensionData.remove("service_id");
        connection.setExtensionData(extensionData);
        
        return connection;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Input> convertToInputs(Map<String, Object> dictionary) {
        Map<String, Input> inputs = new HashMap<>();
        if (dictionary == null) {
            return inputs;
        }

        for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
            Input input = createInput(entry.getKey(), entry.getValue());
            inputs.put(entry.getKey(), input);
        }

        return inputs;
    }

    @SuppressWarnings("unchecked")
    private static Input createInput(String name, Object value) {
        Input input = new Input(name);
        
        if (value == null) {
            return input;
        }

        if (value instanceof Map) {
            Map<String, Object> dictionary = (Map<String, Object>) value;
            
            // Check if this is an input definition
            if (PropertyUtils.isInput(dictionary)) {
                PropertyType propertyType = PropertyUtils.getPropertyType(dictionary);
                
                input.setType(propertyType);
                input.setDescription(DictionaryUtils.getValue(dictionary, "description"));
                input.setDefaultValue(PropertyUtils.getPropertyValue(propertyType, 
                    dictionary.get("default")));
                input.setSample(PropertyUtils.getPropertyValue(propertyType, 
                    dictionary.get("sample")));
                input.setRequired(DictionaryUtils.getValue(dictionary, "required", true));
                input.setStrict(DictionaryUtils.getValue(dictionary, "strict", true));
                input.setJsonSchema(dictionary.get("json_schema"));
                
                return input;
            }
        }

        // Simple value - infer type and use as sample
        input.setType(PropertyUtils.getPropertyTypeFromValue(value));
        input.setSample(value);
        return input;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Output> convertToOutputs(Object value) {
        Map<String, Output> outputs = new HashMap<>();
        if (value == null) {
            return outputs;
        }

        if (value instanceof Map) {
            Map<String, Object> dictionary = (Map<String, Object>) value;
            
            for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
                Map<String, Object> outputDict = DictionaryUtils.getConfig(dictionary, entry.getKey());
                Output output = createOutput(entry.getKey(), outputDict);
                outputs.put(entry.getKey(), output);
            }
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            
            for (Object item : list) {
                if (item instanceof Map) {
                    Map<String, Object> outputDict = (Map<String, Object>) item;
                    String outputName = DictionaryUtils.getValue(outputDict, "name");
                    if (outputName == null) {
                        throw new IllegalArgumentException("Output name is required");
                    }
                    Output output = createOutput(outputName, outputDict);
                    outputs.put(outputName, output);
                }
            }
        } else {
            throw new IllegalArgumentException("Outputs must be a dictionary or a list of dictionaries");
        }

        return outputs;
    }

    private static Output createOutput(String name, Map<String, Object> value) {
        Output output = new Output(name);
        
        if (value != null) {
            output.setType(PropertyUtils.getPropertyType(value));
            output.setDescription(DictionaryUtils.getValue(value, "description"));
            output.setJsonSchema(value.get("json_schema"));
        }
        
        return output;
    }

    private static Template convertToTemplate(Map<String, Object> dictionary) {
        if (dictionary == null) {
            return new Template();
        }

        return new Template(
            DictionaryUtils.getValue(dictionary, "format", Template.DEFAULT_FORMAT),
            DictionaryUtils.getValue(dictionary, "parser", Template.DEFAULT_PARSER)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Tool> convertToTools(List<Map<String, Object>> list) {
        List<Tool> tools = new ArrayList<>();
        if (list == null) {
            return tools;
        }

        for (Map<String, Object> item : list) {
            Tool tool = new Tool();
            tool.setId(DictionaryUtils.getValue(item, "id"));
            tool.setDescription(DictionaryUtils.getValue(item, "description"));
            tool.setType(DictionaryUtils.getValue(item, "type"));
            
            Map<String, Object> options = DictionaryUtils.getConfig(item, "options");
            tool.setOptions(options);
            
            tools.add(tool);
        }

        return tools;
    }
}
