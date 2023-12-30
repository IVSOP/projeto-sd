#!/bin/bash

#mvn exec:java -Dexec.mainClass="grupo49.Server" -Dexec.args="$@"
#mvn exec:java@server
java -cp target/projeto-sd-1.0-SNAPSHOT-shaded.jar grupo49.Server
