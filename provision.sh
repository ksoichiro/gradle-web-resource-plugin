#!/usr/bin/env bash

apt-get update -qq > /dev/null 2>&1

# Install git
which git > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Installing git..."
  apt-get install -y -qq --no-install-recommends git > /dev/null 2>&1
fi

# Install JDK
which java > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Installing JDK..."
  apt-get install -y -qq --no-install-recommends openjdk-7-jdk > /dev/null 2>&1
fi
apt-get clean
