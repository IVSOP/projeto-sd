#!/bin/bash

mvn exec:java -Dexec.mainClass="grupo49.Server" -Dexec.args="$@"
