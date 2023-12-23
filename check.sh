#!/bin/bash

echo "Checking status files"

for i in $(seq $1 $2)
	do cat "outputs/$i-$(($(($i % 10)) + 1)).txt"
	echo
done

echo "Checking for holes"

for i in $(seq $1 $2)
do
	for j in $(seq 1 10)
	do
		if [ ! -e "outputs/$i-$j.txt" ]
		then
			echo "File $i-$j.txt not found"
		fi
	done
done
