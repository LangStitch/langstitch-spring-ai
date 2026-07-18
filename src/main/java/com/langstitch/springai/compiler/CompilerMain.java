package com.langstitch.springai.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.langstitch.springai.compiler.errors.CompileException;
import com.langstitch.springai.compiler.errors.UnsupportedFeatureException;
import com.langstitch.springai.compiler.emit.CompileResult;
import com.langstitch.springai.compiler.emit.SpringAiEmitter;
import com.langstitch.springai.compiler.ir.IrLoader;
import com.langstitch.springai.compiler.ir.IrModels.IrDocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "langstitch-spring-ai",
    mixinStandardHelpOptions = true,
    version = Version.COMPILER_VERSION,
    description = "LangStitch IR v2 compiler for the Spring AI target",
    subcommands = {
      CompilerMain.CompileCmd.class,
      CompilerMain.CapabilitiesCmd.class,
      CompilerMain.VersionCmd.class
    })
public final class CompilerMain implements Callable<Integer> {
  public static void main(String[] args) {
    int code = new CommandLine(new CompilerMain()).execute(args);
    System.exit(code);
  }

  @Override
  public Integer call() {
    new CommandLine(this).usage(System.out);
    return 0;
  }

  @Command(name = "compile", description = "Compile a *.langstitch.json IR document into a Spring AI project")
  static class CompileCmd implements Callable<Integer> {
    @Parameters(index = "0", paramLabel = "DOCUMENT", description = "path to *.langstitch.json")
    Path document;

    @Option(names = "--out", description = "output directory (default: <name>-build beside the document)")
    Path out;

    @Option(names = "--force", description = "write into a non-empty directory")
    boolean force;

    @Override
    public Integer call() {
      try {
        IrDocument doc = IrLoader.load(document.toAbsolutePath().normalize());
        // Allow compiling fixtures that still say python-langstitch by requiring spring-ai,
        // unless the caller overrides via target — refuse mismatched targets loudly.
        CompileResult result = SpringAiEmitter.compile(doc);
        Path outDir =
            out != null
                ? out.toAbsolutePath().normalize()
                : document.toAbsolutePath().getParent().resolve(doc.name + "-build");
        if (Files.exists(outDir) && !force) {
          try (var stream = Files.list(outDir)) {
            if (stream.findAny().isPresent()) {
              System.err.println("error: " + outDir + " is not empty (use --force to overwrite)");
              return 1;
            }
          }
        }
        Files.createDirectories(outDir);
        for (Map.Entry<String, String> e : result.files().entrySet()) {
          Path dest = outDir.resolve(e.getKey());
          Files.createDirectories(dest.getParent());
          Files.writeString(dest, e.getValue());
        }
        @SuppressWarnings("unchecked")
        var nodes = (java.util.List<?>) result.manifest().get("nodes");
        System.out.println(
            "Compiled "
                + doc.name
                + " (IR "
                + doc.irVersion
                + ", target "
                + doc.target.platform
                + ") -> "
                + outDir);
        System.out.println(
            "  "
                + result.files().size()
                + " files written, "
                + (nodes == null ? 0 : nodes.size())
                + " nodes in build manifest");
        return 0;
      } catch (UnsupportedFeatureException e) {
        System.err.println("error: unsupported_feature " + e.feature() + ": " + e.getMessage());
        return 1;
      } catch (CompileException e) {
        System.err.println("error: " + e.getMessage());
        return 1;
      } catch (Exception e) {
        System.err.println("error: " + e.getMessage());
        return 1;
      }
    }
  }

  @Command(name = "capabilities", description = "Print compiler capabilities as JSON")
  static class CapabilitiesCmd implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
      Map<String, Object> caps = new LinkedHashMap<>();
      caps.put("compilerVersion", Version.COMPILER_VERSION);
      caps.put("platform", Version.PLATFORM);
      caps.put("irVersions", java.util.List.of(Version.SUPPORTED_IR_MAJOR_MINOR));
      caps.put("supportsParse", false);
      caps.put("supportsDaemon", false);
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      System.out.println(mapper.writeValueAsString(caps));
      return 0;
    }
  }

  @Command(name = "version", description = "Print compiler version")
  static class VersionCmd implements Callable<Integer> {
    @Override
    public Integer call() {
      System.out.println("langstitch-spring-ai " + Version.COMPILER_VERSION);
      return 0;
    }
  }
}
