#!/bin/bash

if [ $# -ne 1 ]
then
	echo Need to specify number of clients
	exit 1
fi

./build.sh

gcc -O2 tests/*.c -o tests/testexec
# assumes server and workers already exist

./tests/testexec $1
