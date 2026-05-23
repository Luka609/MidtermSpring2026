#!/usr/bin/env sh
set -eu

scripts/compile.sh

case "$(uname)" in
  MINGW*|CYGWIN*|MSYS*) SEP=";" ;;
  *) SEP=":" ;;
esac

java -cp "out${SEP}lib/junit-platform-console-standalone-1.10.2.jar" \
  org.junit.platform.console.ConsoleLauncher \
  execute \
  --select-class=CharacterizationTest