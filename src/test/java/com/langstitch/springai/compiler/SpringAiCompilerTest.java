package com.langstitch.springai.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.langstitch.springai.compiler.errors.CompileException;
import com.langstitch.springai.compiler.errors.UnsupportedFeatureException;
import com.langstitch.springai.compiler.emit.CompileResult;
import com.langstitch.springai.compiler.emit.SpringAiEmitter;
import com.langstitch.springai.compiler.ir.IrLoader;
import com.langstitch.springai.compiler.ir.IrModels.IrDocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

class SpringAiCompilerTest {
  private static final Path SPEC_FIXTURES =
      Path.of("..").resolve("langstitch-spec").resolve("fixtures").normalize().toAbsolutePath();

  static boolean fixturesPresent() {
    return Files.isDirectory(SPEC_FIXTURES.resolve("minimal-llm"));
  }

  private static String minimalDocJson(String platform) {
    return """
        {
          "irVersion": "2.0.0",
          "name": "unit_test",
          "projectVersion": "0.1.0",
          "logical": {
            "entryGraphId": "main",
            "graphs": [{
              "id": "main",
              "name": "Main",
              "stateFields": [
                { "id": "sf1", "name": "messages", "type": "messages", "reducer": "append" },
                { "id": "sf2", "name": "query", "type": "str" }
              ],
              "nodes": [
                { "id": "start-1", "kind": "start", "label": "Start" },
                {
                  "id": "llm-1",
                  "kind": "llm",
                  "label": "LLM",
                  "model": "gpt-4o-mini",
                  "systemPrompt": "You are helpful.",
                  "userPrompt": "Input: {query}",
                  "temperature": 0.2,
                  "outputKey": "messages"
                },
                { "id": "end-1", "kind": "end", "label": "End" }
              ],
              "edges": [
                { "id": "e1", "source": "start-1", "target": "llm-1" },
                { "id": "e2", "source": "llm-1", "target": "end-1" }
              ]
            }],
            "configuration": [],
            "settings": {
              "logging": { "level": "info", "format": "json", "sink": "stdout", "captureContent": false },
              "security": { "auth": "none", "apiKeyEnvVar": "LANGSTITCH_API_KEY", "corsOrigins": [] }
            }
          },
          "target": { "platform": "%s", "options": {} }
        }
        """.formatted(platform);
  }

  @Test
  void refusesWrongTarget() {
    IrDocument doc = IrLoader.loadJson(minimalDocJson("python-langstitch"));
    CompileException ex =
        assertThrows(CompileException.class, () -> SpringAiEmitter.compile(doc));
    assertTrue(ex.getMessage().contains("python-langstitch"));
  }

  @Test
  void refusesNewerIrVersions() {
    String json = minimalDocJson("spring-ai").replace("2.0.0", "2.1.0");
    CompileException ex = assertThrows(CompileException.class, () -> IrLoader.loadJson(json));
    assertTrue(ex.getMessage().contains("unsupported irVersion"));
  }

  @Test
  void refusesUnsupportedNodeKind() {
    final String json =
        """
        {
          "irVersion": "2.0.0",
          "name": "unit_test",
          "projectVersion": "0.1.0",
          "logical": {
            "entryGraphId": "main",
            "graphs": [{
              "id": "main",
              "name": "Main",
              "stateFields": [],
              "nodes": [
                { "id": "start-1", "kind": "start", "label": "Start" },
                { "id": "rag-1", "kind": "rag", "label": "RAG", "pipelineId": "p1" },
                { "id": "end-1", "kind": "end", "label": "End" }
              ],
              "edges": [
                { "id": "e1", "source": "start-1", "target": "rag-1" },
                { "id": "e2", "source": "rag-1", "target": "end-1" }
              ]
            }],
            "settings": { "logging": { "level": "info", "format": "json", "sink": "stdout" } }
          },
          "target": { "platform": "spring-ai", "options": {} }
        }
        """;
    UnsupportedFeatureException ex =
        assertThrows(
            UnsupportedFeatureException.class,
            () -> SpringAiEmitter.compile(IrLoader.loadJson(json)));
    assertEquals("rag", ex.feature());
  }

  @Test
  void compilesMinimalDocument() {
    CompileResult result = SpringAiEmitter.compile(IrLoader.loadJson(minimalDocJson("spring-ai")));
    Map<String, String> files = result.files();
    assertTrue(files.containsKey("application.yaml"));
    assertTrue(files.containsKey("pom.xml"));
    assertTrue(files.containsKey(".langstitch-build-manifest.json"));
    assertEquals("spring-ai", result.manifest().get("platform"));
    assertTrue(
        files.keySet().stream().anyMatch(p -> p.endsWith("/nodes/Llm1Node.java")),
        "expected Llm1Node.java, got " + files.keySet());
    String llm =
        files.entrySet().stream()
            .filter(e -> e.getKey().endsWith("/nodes/Llm1Node.java"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow();
    assertTrue(llm.contains("@com.langstitch.unittest.annotation.LangstitchNode(\"llm-1\")"));
    assertTrue(llm.contains("ChatClient"));
    String graph =
        files.entrySet().stream()
            .filter(e -> e.getKey().endsWith("/graph/MainGraph.java"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow();
    assertTrue(graph.contains("\"llm-1\""));
  }

  @Test
  @EnabledIf("fixturesPresent")
  void compilesMinimalLlmFixtureWhenRetargeted() throws Exception {
    String raw = Files.readString(SPEC_FIXTURES.resolve("minimal-llm/document.langstitch.json"));
    raw = raw.replace("\"python-langstitch\"", "\"spring-ai\"");
    CompileResult result = SpringAiEmitter.compile(IrLoader.loadJson(raw));
    assertTrue(result.files().containsKey("pom.xml"));
    assertTrue(
        result.files().keySet().stream().anyMatch(p -> p.endsWith("/nodes/Llm1Node.java")));
  }

  @Test
  @EnabledIf("fixturesPresent")
  void compilesRouterWorkflowFixtureWhenRetargeted() throws Exception {
    String raw = Files.readString(SPEC_FIXTURES.resolve("router-workflow/document.langstitch.json"));
    raw = raw.replace("\"python-langstitch\"", "\"spring-ai\"");
    CompileResult result = SpringAiEmitter.compile(IrLoader.loadJson(raw));
    assertTrue(
        result.files().keySet().stream().anyMatch(p -> p.endsWith("/nodes/Router1Node.java")));
    String router =
        result.files().entrySet().stream()
            .filter(e -> e.getKey().endsWith("/nodes/Router1Node.java"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow();
    assertTrue(router.contains("public String route"));
    assertTrue(router.contains("refund"));
    String main =
        result.files().entrySet().stream()
            .filter(e -> e.getKey().endsWith("/graph/MainGraph.java"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow();
    assertTrue(main.contains("nextAfter_router_1"));
  }

  @Test
  void compilesMcpToolNode() {
    String json =
        """
        {
          "irVersion": "2.0.0",
          "name": "unit_test",
          "projectVersion": "0.1.0",
          "logical": {
            "entryGraphId": "main",
            "graphs": [{
              "id": "main",
              "name": "Main",
              "stateFields": [],
              "nodes": [
                { "id": "start-1", "kind": "start", "label": "Start" },
                {
                  "id": "tool-1",
                  "kind": "tool",
                  "label": "MCP Tool",
                  "connectionType": "mcp",
                  "mcpServerId": "fs",
                  "mcpToolName": "read_file",
                  "inputKey": "path",
                  "outputKey": "content"
                },
                { "id": "end-1", "kind": "end", "label": "End" }
              ],
              "edges": [
                { "id": "e1", "source": "start-1", "target": "tool-1" },
                { "id": "e2", "source": "tool-1", "target": "end-1" }
              ]
            }],
            "mcpServers": [
              {
                "id": "fs",
                "name": "Filesystem",
                "transport": "stdio",
                "command": "npx",
                "args": ["-y", "@modelcontextprotocol/server-filesystem"],
                "tools": [{ "name": "read_file", "description": "Read a file" }]
              }
            ],
            "settings": { "logging": { "level": "info", "format": "json", "sink": "stdout" } }
          },
          "target": { "platform": "spring-ai", "options": {} }
        }
        """;
    CompileResult result = SpringAiEmitter.compile(IrLoader.loadJson(json));
    assertTrue(result.files().containsKey("src/main/java/com/langstitch/unittest/mcp/McpToolClient.java"));
    assertTrue(result.files().get("application.yaml").contains("mcp:"));
    String tool =
        result.files().entrySet().stream()
            .filter(e -> e.getKey().endsWith("/nodes/Tool1Node.java"))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElseThrow();
    assertTrue(tool.contains("McpToolClient"));
    assertTrue(tool.contains("callTool"));
  }

  @Test
  void customNodeWithoutSpringTemplateFails() {
    String json =
        """
        {
          "irVersion": "2.0.0",
          "name": "unit_test",
          "projectVersion": "0.1.0",
          "logical": {
            "entryGraphId": "main",
            "graphs": [{
              "id": "main",
              "name": "Main",
              "stateFields": [],
              "nodes": [
                { "id": "start-1", "kind": "start", "label": "Start" },
                { "id": "custom-1", "kind": "custom", "label": "Widget", "componentId": "comp-1", "config": {} },
                { "id": "end-1", "kind": "end", "label": "End" }
              ],
              "edges": [
                { "id": "e1", "source": "start-1", "target": "custom-1" },
                { "id": "e2", "source": "custom-1", "target": "end-1" }
              ]
            }],
            "componentRegistry": [
              { "id": "comp-1", "label": "Widget", "codegen": { "templates": { "python-langstitch": "pass" } } }
            ],
            "settings": { "logging": { "level": "info", "format": "json", "sink": "stdout" } }
          },
          "target": { "platform": "spring-ai", "options": {} }
        }
        """;
    UnsupportedFeatureException ex =
        assertThrows(
            UnsupportedFeatureException.class,
            () -> SpringAiEmitter.compile(IrLoader.loadJson(json)));
    assertEquals("component-template", ex.feature());
  }
}
