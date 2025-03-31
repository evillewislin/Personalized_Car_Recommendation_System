#!/bin/bash
export JAVA_OPTS="--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED"
export JAVA_TOOL_OPTIONS="-Djdk.reflect.useDirectMethodHandle=false"
java $JAVA_OPTS -jar your-app.jar