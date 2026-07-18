@echo off
setlocal
set "JAR=%~dp0..\target\langstitch-spring-ai-0.1.0-SNAPSHOT.jar"
if not "%LANGSTITCH_SPRING_AI_JAR%"=="" set "JAR=%LANGSTITCH_SPRING_AI_JAR%"
if not exist "%JAR%" (
  echo error: jar not found: %JAR%
  echo Build with: mvn -q package
  echo Or set LANGSTITCH_SPRING_AI_JAR to the fat jar path.
  exit /b 1
)
java -jar "%JAR%" %*
