#!/bin/bash

#mvn exec:java -Dexec.mainClass="grupo49.Worker" # -Dexec.args="$@"
mvn exec:java@worker
