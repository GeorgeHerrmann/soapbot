#!/bin/bash

echo "Cleaning project..."
mvn clean

echo "Building project..."
mvn compile

echo "Running SoapBot..."
mvn exec:java
