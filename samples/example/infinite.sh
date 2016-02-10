#!/bin/bash

# Execute webResourceCompile with infinite loop.
# This script is for testing stability of tasks.

counter=1
while true
do
    printf "\e[1;35mTrial ${counter}:\e[0m\n"
    cmd="./gradlew clean webResourceCompile -s $@"
    printf "\e[1;37m${cmd}\e[0m\n"
    ${cmd}
    if [ $? -ne 0 ]; then
        exit 1
    fi
    printf "\n"
    counter=`expr ${counter} + 1`
done
