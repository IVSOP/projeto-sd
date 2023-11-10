#!/bin/bash

mvn exec:java -Dexec.mainClass="grupo49.Client" -Dexec.args="$@"
