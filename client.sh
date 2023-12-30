#!/bin/bash

#mvn exec:java -Dexec.mainClass="grupo49.ClientUI" # -Dexec.args="$@"
#mvn exec:java@client
java -cp target/projeto-sd-1.0-SNAPSHOT-shaded.jar grupo49.ClientUI
