#!/bin/sh
# Compile and run DEAD SIGNAL (requires Java 11+)
cd "$(dirname "$0")"
rm -rf out && mkdir out
javac -d out $(find src -name "*.java") && java -cp out deadsignal.Main
