#!/bin/bash

#mvn exec:java -Dexec.mainClass="grupo49.Worker" # -Dexec.args="$@"
#mvn exec:java@worker
java -cp target/projeto-sd-1.0-SNAPSHOT-shaded.jar grupo49.Worker
