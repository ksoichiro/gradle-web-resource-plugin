#!/usr/bin/env bash

# Install git
which git > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Installing git..."
  yum install -y git > /dev/null 2>&1
fi

# Install JDK
which java > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Installing JDK..."
  yum install -y java-1.7.0-openjdk.x86_64 > /dev/null 2>&1
  # Without this, downloading Gradle fails with java.security.KeyException
  yum upgrade -y nss > /dev/null 2>&1
fi
