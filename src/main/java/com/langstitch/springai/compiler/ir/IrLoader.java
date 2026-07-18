package com.langstitch.springai.compiler.ir;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langstitch.springai.compiler.Version;
import com.langstitch.springai.compiler.errors.CompileException;
import com.langstitch.springai.compiler.ir.IrModels.IrDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class IrLoader {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final Pattern IR_2_0 = Pattern.compile("^2\\.0\\.\\d+$");

  private IrLoader() {}

  public static IrDocument load(Path path) {
    try {
      String text = Files.readString(path);
      return loadJson(text);
    } catch (IOException e) {
      throw new CompileException("failed to read IR document: " + path, e);
    }
  }

  public static IrDocument loadJson(String json) {
    try {
      if (json != null && !json.isEmpty() && json.charAt(0) == '\uFEFF') {
        json = json.substring(1);
      }
      var root = MAPPER.readTree(json);
      if (root.has("version") && !root.has("irVersion")) {
        throw new CompileException(
            "IR v1 documents are not supported by this compiler; migrate to IR v2 "
                + "(irVersion 2.0.x) first.");
      }
      IrDocument doc = MAPPER.treeToValue(root, IrDocument.class);
      validate(doc);
      return doc;
    } catch (CompileException e) {
      throw e;
    } catch (Exception e) {
      throw new CompileException("invalid IR document: " + e.getMessage(), e);
    }
  }

  private static void validate(IrDocument doc) {
    if (doc.irVersion == null || doc.irVersion.isBlank()) {
      throw new CompileException("missing irVersion");
    }
    if (!IR_2_0.matcher(doc.irVersion).matches()) {
      throw new CompileException(
          "unsupported irVersion '"
              + doc.irVersion
              + "' (this compiler supports "
              + Version.SUPPORTED_IR_MAJOR_MINOR
              + ".x). Upgrade langstitch-spring-ai to compile newer documents.");
    }
    if (doc.name == null || doc.name.isBlank()) {
      throw new CompileException("document name is required");
    }
    if (doc.logical == null || doc.logical.graphs == null || doc.logical.graphs.isEmpty()) {
      throw new CompileException("logical.graphs must contain at least one graph");
    }
    if (doc.logical.entryGraphId == null || doc.logical.entryGraphId.isBlank()) {
      doc.logical.entryGraphId = doc.logical.graphs.get(0).id;
    }
    if (doc.target == null) {
      doc.target = new IrModels.Target();
    }
    if (doc.target.platform == null || doc.target.platform.isBlank()) {
      doc.target.platform = Version.PLATFORM;
    }
  }
}
