# Changelog

All notable changes to **langstitch-spring-ai** are documented here.

## [0.1.0-SNAPSHOT] - 2026-07-19

### Added
- IR v2 compiler CLI for the `spring-ai` target (`compile`, `capabilities`, `version`).
- Spring Boot 3.4 + Spring AI 1.0 project emission: `application.yaml` contract, Maven POM,
  ChatClient LLM nodes, routers, functions, tools, response transformers, custom templates.
- Build manifest (`.langstitch-build-manifest.json`) preserving IR node ids.
- Conformance tests against sibling `langstitch-spec/fixtures` when present.
- UTF-8 BOM stripping on IR document load (Windows-friendly).
- Maven Central publish pipeline (`.github/workflows/publish.yml`) using the Central Portal
  plugin, GPG signing, sources/javadoc jars, and a fat CLI jar classifier `all`.
- Maven `groupId` set to `com.langstitch` (reverse-DNS for langstitch.com).
