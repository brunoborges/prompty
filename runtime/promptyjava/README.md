# Prompty Java Runtime

This is the Java implementation of the Prompty runtime, providing support for LLM prompt execution with observability, understandability, and portability.

## Features

- **Template Rendering**: Support for Jinja2 templates using Jinjava
- **Multiple Parsers**: Chat, completion, embedding, and image parsers
- **Flexible Configuration**: YAML-based configuration with global settings
- **Async Support**: CompletableFuture-based asynchronous operations
- **Extensible Architecture**: Plugin-based invoker system for renderers, parsers, and executors

## Quick Start

### Loading and Executing a Prompty

```java
import com.microsoft.prompty.Prompty;
import java.util.Map;

// Load a prompty file
Prompty prompty = Prompty.load("path/to/your/template.prompty");

// Execute with inputs
Map<String, Object> inputs = Map.of("name", "World");
Object result = prompty.execute(null, null, inputs, false);
System.out.println(result);
```

### Asynchronous Execution

```java
import java.util.concurrent.CompletableFuture;

// Load and execute asynchronously
CompletableFuture<Object> future = Prompty.executeAsync(
    "path/to/your/template.prompty",
    null,  // configuration
    null,  // parameters
    Map.of("name", "World"),  // inputs
    "default",  // config name
    false   // raw output
);

future.thenAccept(result -> {
    System.out.println("Result: " + result);
});
```

### Creating Prompty from Text

```java
String promptyContent = """
    ---
    name: "Greeting"
    description: "A simple greeting template"
    model:
      api: chat
      configuration:
        type: openai
    inputs:
      name:
        type: string
        description: "Name to greet"
    ---
    Hello {{name}}!
    """;

Map<String, Object> globalConfig = new HashMap<>();
Prompty prompty = Prompty.load(promptyContent, globalConfig, null);
```

## Prompty File Format

A prompty file consists of YAML frontmatter and template content:

```yaml
---
name: "Example Prompty"
description: "An example prompty template"
version: "1.0"
authors: ["Your Name"]
tags: ["example", "demo"]

model:
  api: chat
  configuration:
    type: openai
    api_key: env:OPENAI_API_KEY
  options:
    model: gpt-3.5-turbo
    temperature: 0.7

inputs:
  topic:
    type: string
    description: "The topic to write about"
    required: true
  
  style:
    type: string
    description: "Writing style"
    default: "formal"

outputs:
  text:
    type: string
    description: "Generated text"

template:
  format: jinja2
  parser: prompty

---
Write a {{ style }} article about {{ topic }}.
```

## Model Configuration

### Input Types

- `string`: Text input
- `number`: Numeric input
- `integer`: Integer input
- `boolean`: Boolean input
- `array`: Array input
- `object`: Object input

### Template Formats

- `jinja2`: Jinja2 templating (default)
- More formats can be added via the invoker system

### Parser Types

- `prompty.chat`: Chat completion format
- `prompty.completion`: Text completion format
- `prompty.embedding`: Embedding format
- `prompty.image`: Image processing format

## Architecture

The Java runtime follows a modular architecture:

### Core Components

- **Prompty**: Main class for loading and executing prompty templates
- **Model Classes**: Data models for prompty specification (Input, Output, Model, etc.)
- **Invoker System**: Extensible plugin system for different operations

### Invoker Types

1. **Renderers**: Transform templates with data (e.g., Jinja2Renderer)
2. **Parsers**: Convert rendered content to structured format (e.g., ChatParser)
3. **Executors**: Execute requests against LLM providers
4. **Processors**: Post-process results

### Utilities

- **DictionaryUtils**: Helper methods for map operations
- **FileUtils**: File I/O utilities
- **Normalizer**: Configuration normalization
- **PropertyUtils**: Property type handling

## Dependencies

The implementation uses the following key dependencies:

- **Jackson**: JSON/YAML processing
- **Jinjava**: Jinja2 template rendering
- **JUnit 5**: Testing framework

Optional dependencies for specific providers:

- **Azure SDK**: For Azure OpenAI integration
- **OpenAI Java Client**: For OpenAI API integration

## Building

```bash
mvn clean compile
```

## Testing

```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
