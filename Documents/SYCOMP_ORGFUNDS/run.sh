#!/bin/sh
# Compile and run (requires JDK 17+). logo.png is copied next to the classes.
mkdir -p out && javac -d out src/*.java && cp src/logo.png out/ && java -cp out Main
