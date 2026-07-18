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

## Install (Maven Central)

After the first release is published:

```xml
<dependency>
  <groupId>com.langstitch</groupId>
  <artifactId>langstitch-spring-ai</artifactId>
  <version>0.1.0</version>
</dependency>
```

Runnable fat jar (classifier `all`):

```bash
mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
  -Dartifact=com.langstitch:langstitch-spring-ai:0.1.0:jar:all \
  -DoutputDirectory=.
java -jar langstitch-spring-ai-0.1.0-all.jar version
```

## Build locally

```bash
mvn -q package
# thin jar:  target/langstitch-spring-ai-*-SNAPSHOT.jar
# fat CLI:   target/langstitch-spring-ai-*-SNAPSHOT-all.jar
java -jar target/langstitch-spring-ai-*-SNAPSHOT-all.jar compile path/to/doc.langstitch.json --out ./out --force
```

Install a `langstitch-spring-ai` shim on your `PATH` so LangTailor can discover the CLI:

```bash
chmod +x scripts/langstitch-spring-ai
export PATH="$PWD/scripts:$PATH"
```

Or set `LANGSTITCH_SPRING_AI_JAR` to the `*-all.jar` path.

## Publish to Maven Central

Publishing is automated by [`.github/workflows/publish.yml`](.github/workflows/publish.yml).

### One-time setup

1. Create a [Central Publisher Portal](https://central.sonatype.com/) account.
2. Claim / verify the `com.langstitch` namespace (DNS TXT on `langstitch.com` — see below).
3. Generate a [portal user token](https://central.sonatype.org/publish/generate-portal-token/).
4. Create a GPG key and publish the public key to a keyserver (`keys.openpgp.org` or `keyserver.ubuntu.com`).
5. In the GitHub repo, create an Environment named `maven-central` (optional protection rules).
6. Add repository (or environment) secrets:

| Secret | Value |
|--------|-------|
| `MAVEN_USERNAME` | Central Portal token username |
| `MAVEN_PASSWORD` | Central Portal token password |
| `GPG_PRIVATE_KEY` | ASCII-armored private key (`gpg --export-secret-keys --armor <KEYID>`) |
| `GPG_PASSPHRASE` | Passphrase for that key |

### Verify `com.langstitch`

Until the namespace shows **Verified**, Central will reject publishes under `com.langstitch`.

1. In Central Portal → Publishing Settings → Namespace → **Add Namespace** → `com.langstitch`.
2. Copy the verification key.
3. Add a DNS **TXT** record on the apex of `langstitch.com` (Hostinger DNS):

   ```
   Host / name:  @   (apex)
   Type:         TXT
   Value:        <verification-key-from-portal>
   ```

   Keep any existing SPF TXT; add this as an additional TXT record.
4. Wait for DNS propagation, then click **Verify Namespace** in the portal.

### Release

```bash
# on main, after CI is green
git tag v0.1.0
git push origin v0.1.0
```

Or create a GitHub Release for `v0.1.0`, or run **Actions → Publish → Run workflow** with version `0.1.0`.

The workflow sets the POM version from the tag, runs tests, signs artifacts, and deploys with the `release` Maven profile (`central-publishing-maven-plugin`, `autoPublish=true`).

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
