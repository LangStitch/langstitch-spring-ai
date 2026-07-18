package com.langstitch.springai.compiler.errors;

public class UnsupportedFeatureException extends CompileException {
  private final String feature;
  private final String nodeId;

  public UnsupportedFeatureException(String feature, String message) {
    this(feature, message, null);
  }

  public UnsupportedFeatureException(String feature, String message, String nodeId) {
    super(message);
    this.feature = feature;
    this.nodeId = nodeId;
  }

  public String feature() {
    return feature;
  }

  public String nodeId() {
    return nodeId;
  }
}
