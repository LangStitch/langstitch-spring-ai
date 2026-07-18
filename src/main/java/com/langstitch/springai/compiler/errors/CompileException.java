package com.langstitch.springai.compiler.errors;

public class CompileException extends RuntimeException {
  public CompileException(String message) {
    super(message);
  }

  public CompileException(String message, Throwable cause) {
    super(message, cause);
  }
}
