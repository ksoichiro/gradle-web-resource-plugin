#!/bin/bash

# Execute webResourceCompile with infinite loop.
# This script is for testing stability of tasks.

counter=1
while true
do
    printf "\e[1;35mTrial ${counter}:\e[0m\n"
    ./gradlew clean webResourceCompile -s
    if [ $? -ne 0 ]; then
        exit 1
    fi
    printf "\n"
    counter=`expr ${counter} + 1`
done
