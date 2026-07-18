package com.langstitch.springai.compiler.emit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CompileResult(
    Map<String, String> files, Map<String, Object> manifest, List<String> warnings) {

  public CompileResult {
    files = new LinkedHashMap<>(files);
    warnings = List.copyOf(warnings);
  }
}
