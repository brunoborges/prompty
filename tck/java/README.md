# Prompty Java TCK

This directory contains the Java implementation of the Prompty Test Compatibility Kit (TCK). The TCK ensures that the Java runtime implementation is compatible with other Prompty runtimes (Python, C#) and follows the official Prompty specification.

## Overview

The Java TCK validates:

1. **Specification Compliance** - YAML frontmatter parsing
2. **Functional Equivalence** - Template rendering with Jinja2
3. **Error Handling** - Consistent error behavior
4. **Input Validation** - Type checking and required field validation

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the included Maven wrapper `./mvnw`)
- Built Prompty Java runtime (`../../runtime/promptyjava/target/prompty-java-0.0.1-SNAPSHOT.jar`)

## Building

To build the Java TCK:

```bash
# Using Maven
mvn clean package

# Or using Maven wrapper
./mvnw clean package
```

This will create an executable JAR at `target/prompty-java-tck-1.0.0-SNAPSHOT.jar`.

## Running

### Quick Start

Use the provided shell script to run all tests:

```bash
./run-tck.sh
```

This will:
1. Build the Java runtime if needed
2. Build the Java TCK if needed  
3. Run all tests from `../tck-tests.json`
4. Write results to `../results/java-results.json`

### Manual Execution

You can also run the TCK manually:

```bash
java -jar target/prompty-java-tck-1.0.0-SNAPSHOT.jar ../tck-tests.json ../results/java-results.json
```

### Command Line Options

The shell script supports the following options:

```bash
./run-tck.sh --test-file <file> --output-file <file> --help
```

- `--test-file`: Path to test configuration file (default: `../tck-tests.json`)
- `--output-file`: Path to output results file (default: `../results/java-results.json`)
- `--help`: Show help message

## Test Categories

The Java TCK supports all standard TCK test categories:

### Specification Tests
- Parse YAML frontmatter
- Extract metadata (name, description, authors, etc.)
- Parse model configuration
- Validate input/output specifications

### Functional Tests  
- Render Jinja2 templates
- Variable substitution
- Environment variable resolution
- Complex template features (loops, conditionals)

### Error Tests
- Invalid YAML handling
- Missing required inputs
- Template syntax errors
- Type validation errors

## Output Format

The TCK generates JSON results in the following format:

```json
{
  "runtime": "java",
  "runtimeVersion": "0.0.1-SNAPSHOT",
  "timestamp": "2025-06-26T09:30:00.000Z",
  "totalTests": 10,
  "passed": 8,
  "failed": 1,
  "errors": 1,
  "skipped": 0,
  "results": [
    {
      "testId": "basic-parsing",
      "result": "pass",
      "runtime": "java",
      "runtimeVersion": "0.0.1-SNAPSHOT",
      "executionTimeMs": 150,
      "output": { ... }
    }
  ]
}
```

## Architecture

The Java TCK consists of:

- `JavaTCK.java` - Main TCK implementation
- `TCKTestResult.java` - Individual test result model
- `TCKTestSummary.java` - Test summary model
- `pom.xml` - Maven build configuration
- `run-tck.sh` - Shell script runner

## Integration with TCK Suite

The Java TCK integrates with the broader TCK suite:

1. Reads test definitions from `../tck-tests.json`
2. Compares results with expected outputs in `../expected/`
3. Writes results to `../results/java-results.json`
4. Can be compared with other runtimes using `../tools/compare_runtimes.py`

## Development

### Adding New Tests

New tests are defined in the shared `../tck-tests.json` file. The Java TCK will automatically pick up and execute new tests based on their category.

### Debugging

To enable debug logging, set the logging level:

```bash
java -Dlogback.configurationFile=logback-debug.xml -jar target/prompty-java-tck-1.0.0-SNAPSHOT.jar ...
```

### Contributing

When modifying the Java TCK:

1. Ensure all existing tests pass
2. Add new test cases for new functionality
3. Update documentation
4. Follow Java coding standards
5. Test against other runtime implementations

## Troubleshooting

### Common Issues

**"Java runtime JAR not found"**
- Build the Java runtime first: `cd ../../runtime/promptyjava && mvn clean package`

**"Maven not found"**
- Install Maven 3.6+ or use the Maven wrapper: `./mvnw`

**"Java version error"**
- Ensure Java 21+ is installed and in your PATH

**"System dependency warning"**
- This is expected for the local prompty-java dependency and doesn't affect functionality

### Getting Help

For issues specific to the Java TCK:
1. Check the logs for detailed error messages
2. Verify the Java runtime is built and accessible
3. Ensure test files exist in the expected locations
4. Compare with working C# or Python TCK implementations
