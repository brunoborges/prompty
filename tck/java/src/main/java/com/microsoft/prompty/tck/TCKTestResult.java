package com.microsoft.prompty.tck;

import java.util.Map;

/**
 * Represents the result of a single TCK test execution.
 */
public class TCKTestResult {
    private String testId;
    private String result; // "pass", "fail", "error", "skip"
    private String runtime;
    private String runtimeVersion;
    private long executionTimeMs;
    private Object output;
    private String errorMessage;
    private String errorType;
    private Map<String, Object> metadata;
    
    // Default constructor
    public TCKTestResult() {}
    
    // Getters and setters
    public String getTestId() {
        return testId;
    }
    
    public void setTestId(String testId) {
        this.testId = testId;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getRuntime() {
        return runtime;
    }
    
    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }
    
    public String getRuntimeVersion() {
        return runtimeVersion;
    }
    
    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public Object getOutput() {
        return output;
    }
    
    public void setOutput(Object output) {
        this.output = output;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorType() {
        return errorType;
    }
    
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
