#!/bin/bash

if [ $# -ne 2 ]
then
	echo Need to specify number of workers and number of clients
	exit 1
fi

./build.sh

# $1 is number of clients
# $2 is number of workers
# worker memory is allways random?
numWorkers=$1
numClients=$2

# limited to 255 clients and workers I guess
clientsIPPrefix="127.0.0"
workersIPPrefix="127.0.255"
# serverIP is 0.0.0.0

pids=() # pids of things that get disowned

echo Making server
./server.sh &> "tests/server/server.txt" &
pids+=($!)

for i in $(seq $numWorkers)
do
	echo Making worker in "$workersIPPrefix.$i"
    ./worker.sh "0.0.0.0" "$workersIPPrefix.$i" &> "tests/workers/worker$i.txt" &
	pids+=($!)
done

for i in $(seq $numClients)
do
	echo Making client in "$clientsIPPrefix.$i"
    ./client.sh "0.0.0.0" "$clientsIPPrefix.$i" &> "tests/clients/client$i.txt" &
	pids+=($!)
done

echo Generated processes ${pids[@]}
read -p "Enter anything to force kill them all" idk
# forcefully kill all generated processes
#kill -s SIGKILL ${pids[@]}
