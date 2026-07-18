package com.langstitch.springai.compiler.emit;

import com.langstitch.springai.compiler.errors.CompileException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates common IR router conditions (Python-flavored) into Java boolean expressions over
 * {@code state}. Fails loudly on unrecognized shapes.
 */
public final class ConditionTranslator {
  private static final Pattern STATE_GET_EQ =
      Pattern.compile(
          "^state\\.get\\(\\s*[\"']([^\"']+)[\"']\\s*\\)\\s*==\\s*[\"']([^\"']*)[\"']\\s*$");
  private static final Pattern STATE_GET_EQ_NUM =
      Pattern.compile(
          "^state\\.get\\(\\s*[\"']([^\"']+)[\"']\\s*\\)\\s*==\\s*(-?\\d+(?:\\.\\d+)?)\\s*$");
  private static final Pattern STATE_BRACKET_EQ =
      Pattern.compile("^state\\[[\"']([^\"']+)[\"']\\]\\s*==\\s*[\"']([^\"']*)[\"']\\s*$");

  private ConditionTranslator() {}

  public static String toJava(String condition, String nodeId) {
    if (condition == null) {
      throw new CompileException("router node '" + nodeId + "' has a branch with null condition");
    }
    String trimmed = condition.trim();
    if (trimmed.equals("True") || trimmed.equals("true")) {
      return "true";
    }
    if (trimmed.equals("False") || trimmed.equals("false")) {
      return "false";
    }

    Matcher m = STATE_GET_EQ.matcher(trimmed);
    if (m.matches()) {
      return "java.util.Objects.equals(String.valueOf(state.get("
          + JavaNames.javaString(m.group(1))
          + ")), "
          + JavaNames.javaString(m.group(2))
          + ")";
    }
    m = STATE_BRACKET_EQ.matcher(trimmed);
    if (m.matches()) {
      return "java.util.Objects.equals(String.valueOf(state.get("
          + JavaNames.javaString(m.group(1))
          + ")), "
          + JavaNames.javaString(m.group(2))
          + ")";
    }
    m = STATE_GET_EQ_NUM.matcher(trimmed);
    if (m.matches()) {
      return "java.util.Objects.equals(String.valueOf(state.get("
          + JavaNames.javaString(m.group(1))
          + ")), "
          + JavaNames.javaString(m.group(2))
          + ")";
    }

    throw new CompileException(
        "router node '"
            + nodeId
            + "' condition is not supported by the Spring AI compiler: "
            + trimmed
            + ". Use state.get(\"field\") == \"value\" (or True/False).");
  }
}
