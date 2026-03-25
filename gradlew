#!/bin/bash
set -e

APP_JAR="$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar"

# Use JAVA_HOME if set, otherwise look for java in PATH
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java &> /dev/null; then
    JAVA="java"
else
    echo "ERROR: Cannot find java. JAVA_HOME is not set and java is not in PATH." >&2
    exit 1
fi

if [ ! -f "$JAVA" ] && ! command -v "$JAVA" &> /dev/null; then
    echo "ERROR: Java not found at $JAVA" >&2
    exit 1
fi

export GRADLE_OPTS="${GRADLE_OPTS:--Dorg.gradle.appname=gradlew}"

exec "$JAVA" $GRADLE_OPTS -classpath "$APP_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
