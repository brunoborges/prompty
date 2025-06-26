package com.microsoft.prompty.tck;

import java.util.List;

/**
 * Represents the summary of all TCK test executions.
 */
public class TCKTestSummary {
    private String runtime;
    private String runtimeVersion;
    private String timestamp;
    private int totalTests;
    private int passed;
    private int failed;
    private int errors;
    private int skipped;
    private List<TCKTestResult> results;
    
    // Default constructor
    public TCKTestSummary() {}
    
    // Getters and setters
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
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getTotalTests() {
        return totalTests;
    }
    
    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }
    
    public int getPassed() {
        return passed;
    }
    
    public void setPassed(int passed) {
        this.passed = passed;
    }
    
    public int getFailed() {
        return failed;
    }
    
    public void setFailed(int failed) {
        this.failed = failed;
    }
    
    public int getErrors() {
        return errors;
    }
    
    public void setErrors(int errors) {
        this.errors = errors;
    }
    
    public int getSkipped() {
        return skipped;
    }
    
    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }
    
    public List<TCKTestResult> getResults() {
        return results;
    }
    
    public void setResults(List<TCKTestResult> results) {
        this.results = results;
    }
}
