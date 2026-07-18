@echo off
setlocal EnableDelayedExpansion
set "ROOT=%~dp0.."
if not "%LANGSTITCH_SPRING_AI_JAR%"=="" (
  set "JAR=%LANGSTITCH_SPRING_AI_JAR%"
) else (
  set "JAR="
  for %%F in ("%ROOT%\target\langstitch-spring-ai-*-all.jar") do set "JAR=%%~fF"
)
if "%JAR%"=="" (
  echo error: fat jar not found under target\*-all.jar
  echo Build with: mvn -q package
  echo Or set LANGSTITCH_SPRING_AI_JAR to the *-all.jar path.
  exit /b 1
)
if not exist "%JAR%" (
  echo error: jar not found: %JAR%
  exit /b 1
)
java -jar "%JAR%" %*
