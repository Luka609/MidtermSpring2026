#!/usr/bin/env sh
set -eu

rm -rf out
mkdir -p out
javac -cp lib/junit-platform-console-standalone-1.10.2.jar \
  -d out \
  src/*.java