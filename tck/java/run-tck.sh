#!/bin/bash

# Java TCK Runner Script
# This script runs the Java implementation of the Prompty TCK

set -e

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TCK_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
TEST_FILE="tck-tests.json"
OUTPUT_FILE="results/java-results.json"
JAVA_TCK_JAR="$SCRIPT_DIR/target/prompty-java-tck-1.0.0-SNAPSHOT.jar"

# Parse command line arguments
if [[ $# -gt 1 ]]; then
    echo "Too many arguments"
    echo "Usage: $0 [output-file]"
    echo "Use --help for usage information"
    exit 1
fi

if [[ $# -eq 1 ]]; then
    if [[ "$1" == "--help" || "$1" == "-h" ]]; then
        echo "Usage: $0 [options] [output-file]"
        echo "Arguments:"
        echo "  output-file          Output results file (default: results/java-results.json)"
        echo "Options:"
        echo "  --help, -h           Show this help message"
        exit 0
    fi
    OUTPUT_FILE="$1"
fi

echo "=== Java TCK Runner ==="
echo "TCK Root: $TCK_ROOT"
echo "Test file: $TEST_FILE"
echo "Output file: $OUTPUT_FILE"
echo "Java TCK JAR: $JAVA_TCK_JAR"
echo ""

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check Java version (require Java 21+)
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [[ "$JAVA_VERSION" -lt 21 ]]; then
    echo "Error: Java 21 or higher is required. Found Java $JAVA_VERSION"
    exit 1
fi

echo "Using Java version: $JAVA_VERSION"

# Build the Java runtime first if not already built
JAVA_RUNTIME_DIR="$TCK_ROOT/../runtime/promptyjava"
JAVA_RUNTIME_JAR="$JAVA_RUNTIME_DIR/target/prompty-java-0.0.1-SNAPSHOT.jar"

if [[ ! -f "$JAVA_RUNTIME_JAR" ]]; then
    echo "Building Java runtime..."
    cd "$JAVA_RUNTIME_DIR"
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    elif [[ -f "./mvnw" ]]; then
        ./mvnw clean package -DskipTests
    else
        echo "Error: Maven not found. Please install Maven or use the Maven wrapper in the runtime directory."
        exit 1
    fi
    cd "$SCRIPT_DIR"
fi

# Build the Java TCK if not already built
if [[ ! -f "$JAVA_TCK_JAR" ]]; then
    echo "Building Java TCK..."
    cd "$SCRIPT_DIR"
    if command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    elif [[ -f "./mvnw" ]]; then
        ./mvnw clean package -DskipTests
    else
        echo "Error: Maven not found. Please install Maven."
        exit 1
    fi
fi

# Create output directory if it doesn't exist
OUTPUT_DIR="$(dirname "$TCK_ROOT/$OUTPUT_FILE")"
mkdir -p "$OUTPUT_DIR"

# Run the Java TCK
echo "Running Java TCK..."
cd "$TCK_ROOT"

java -jar "$JAVA_TCK_JAR" "$TEST_FILE" "$OUTPUT_FILE"

echo ""
echo "Java TCK completed successfully!"
echo "Results written to: $TCK_ROOT/$OUTPUT_FILE"

# Display summary if jq is available
if command -v jq &> /dev/null; then
    echo ""
    echo "=== Test Summary ==="
    jq -r '
        "Runtime: " + .runtime + " " + .runtimeVersion,
        "Total Tests: " + (.totalTests | tostring),
        "Passed: " + (.passed | tostring),
        "Failed: " + (.failed | tostring),
        "Errors: " + (.errors | tostring),
        "Skipped: " + (.skipped | tostring)
    ' "$TCK_ROOT/$OUTPUT_FILE"
fi
