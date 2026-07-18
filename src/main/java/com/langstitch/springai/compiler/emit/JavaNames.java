package com.langstitch.springai.compiler.emit;

import java.util.Locale;

public final class JavaNames {
  private JavaNames() {}

  /** Sanitize an IR node id into a valid Java identifier (method / field). */
  public static String identifier(String nodeId) {
    String name = nodeId.replaceAll("[^0-9a-zA-Z_]", "_");
    if (name.isEmpty() || Character.isDigit(name.charAt(0))) {
      name = "n_" + name;
    }
    return name;
  }

  /** PascalCase class name from an IR node id. */
  public static String className(String nodeId) {
    String id = identifier(nodeId);
    String[] parts = id.split("_+");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part.isEmpty()) continue;
      sb.append(Character.toUpperCase(part.charAt(0)));
      if (part.length() > 1) sb.append(part.substring(1));
    }
    if (sb.isEmpty()) sb.append("Node");
    return sb.toString();
  }

  public static String packageSlug(String projectName) {
    String slug =
        projectName
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "")
            .replaceAll("^\\d+", "");
    return slug.isEmpty() ? "app" : slug;
  }

  public static String basePackage(String projectName) {
    return "com.langstitch." + packageSlug(projectName);
  }

  public static String javaString(String value) {
    if (value == null) return "\"\"";
    String escaped =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    return "\"" + escaped + "\"";
  }
}
