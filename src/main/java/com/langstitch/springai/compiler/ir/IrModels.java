package com.langstitch.springai.compiler.ir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Jackson-friendly IR v2 shapes (extra fields tolerated). */
public final class IrModels {
  private IrModels() {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IrDocument {
    public String irVersion;
    public String name;
    public String description;
    public String projectVersion = "0.1.0";
    public Logical logical = new Logical();
    public Target target = new Target();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Target {
    public String platform = "spring-ai";
    public Map<String, Object> options = new LinkedHashMap<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Logical {
    public String entryGraphId;
    public List<LogicalGraph> graphs = new ArrayList<>();
    public List<ConfigSection> configuration = new ArrayList<>();
    public List<ComponentDef> componentRegistry = new ArrayList<>();
    public List<ToolDef> toolRegistry = new ArrayList<>();
    public List<McpServerDef> mcpServers = new ArrayList<>();
    public Settings settings = new Settings();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class McpServerDef {
    public String id;
    public String name;
    public String transport = "stdio";
    public String command;
    public List<String> args = new ArrayList<>();
    public String url;
    public Map<String, String> envVars = new LinkedHashMap<>();
    public List<McpToolDef> tools = new ArrayList<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class McpToolDef {
    public String name;
    public String description;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LogicalGraph {
    public String id;
    public String name;
    public List<StateField> stateFields = new ArrayList<>();
    public List<JsonNode> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StateField {
    public String id;
    public String name;
    public String type;
    public String reducer;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Edge {
    public String id;
    public String source;
    public String target;
    public String sourceHandle;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ConfigSection {
    public String prefix;
    public String description;
    public List<ConfigProperty> properties = new ArrayList<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ConfigProperty {
    public String name;
    public String type;
    @com.fasterxml.jackson.annotation.JsonProperty("default")
    public Object defaultValue;
    public String envVar;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ComponentDef {
    public String id;
    public String label;
    public Codegen codegen = new Codegen();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Codegen {
    public Map<String, String> templates = new LinkedHashMap<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ToolDef {
    public String id;
    public String source;
    public String pythonCode;
    public String javaCode;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Settings {
    public ModelSettings model = new ModelSettings();
    public ServerSettings server = new ServerSettings();
    public SecuritySettings security = new SecuritySettings();
    public LoggingSettings logging = new LoggingSettings();
    public ObservabilitySettings observability = new ObservabilitySettings();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ModelSettings {
    public String provider = "openai";
    public String name = "gpt-4o-mini";
    public Double temperature;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ServerSettings {
    public String host = "0.0.0.0";
    public int port = 8080;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SecuritySettings {
    public String auth = "none";
    public String apiKeyEnvVar = "LANGSTITCH_API_KEY";
    public List<String> corsOrigins = new ArrayList<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LoggingSettings {
    public String level = "info";
    public String format = "json";
    public String sink = "stdout";
    public boolean captureContent;
    public Map<String, String> levels = new LinkedHashMap<>();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ObservabilitySettings {
    public boolean enabled;
    public String projectName;
    public LangsmithSettings langsmith = new LangsmithSettings();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class LangsmithSettings {
    public boolean enabled;
    public String apiKeyEnv = "LANGSMITH_API_KEY";
  }
}
