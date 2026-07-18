# langstitch-spring-ai

IR v2 compiler for the **Spring AI** target. Reads `*.langstitch.json` documents and emits a runnable Spring Boot + Spring AI Maven project.

Implements the [langstitch-spec](https://github.com/LangStitch/langstitch-spec) compiler protocol:

```bash
langstitch-spring-ai compile <document.langstitch.json> --out <dir> [--force]
langstitch-spring-ai capabilities
langstitch-spring-ai version
```

## Requirements

- Java 21+
- Maven 3.9+

## Build

```bash
mvn -q package
# runnable fat jar:
java -jar target/langstitch-spring-ai-0.1.0-SNAPSHOT.jar compile path/to/doc.langstitch.json --out ./out --force
```

Install a `langstitch-spring-ai` shim on your `PATH` so LangTailor can discover the CLI:

```bash
# after mvn package
# Windows: add scripts/ to PATH, or copy scripts/langstitch-spring-ai.cmd
# Unix:
chmod +x scripts/langstitch-spring-ai
export PATH="$PWD/scripts:$PATH"
```

Or set `LANGSTITCH_SPRING_AI_JAR` to the fat-jar path; LangTailor also auto-discovers a sibling
`langstitch-spring-ai/target/*.jar` in a polyrepo workspace checkout.

## Capability matrix

Aligned with `langstitch-spec/capabilities/spring-ai.json`:

| Kind | Status |
|------|--------|
| start, end, llm, tool, router, function, response_transformer | supported |
| custom | template-required (`codegen.templates["spring-ai"]`) |
| subgraph, agent, rag, intent_classifier, hitl | unsupported (fail loudly) |

## Generated project layout

```
application.yaml          # IR YAML contract (root)
env.yaml
pom.xml
.langstitch-build-manifest.json
src/main/java/com/langstitch/<pkg>/
  Application.java
  GraphState.java
  graph/MainGraph.java
  nodes/*.java
  config/AiConfig.java
  web/GraphController.java
src/main/resources/application.yml
```

## Verify

```bash
mvn test
```

Tests compile the sibling `langstitch-spec/fixtures` when present (polyrepo workspace layout).
