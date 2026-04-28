#!/bin/sh
cp -r /opa/policies/. /shared-policies/
exec su appuser -c "java $JAVA_OPTS -jar app.jar"
