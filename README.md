# UNO CLI

A command-line UNO-like game in Java. Play against bots or join an interactive
game yourself. Built with Maven, tested with JUnit 5, and runnable from a jar or
a Docker container.

## Requirements

- Java 17 or newer
- Maven 3.6+ (for local build/test/run)
- Docker (optional, only for the container workflow)

## Project layout

```
src/main/java   application sources
src/test/java   JUnit 5 tests
pom.xml         Maven build configuration
Dockerfile      multi-stage build + run image
```

## Build, test, run, package

All commands are run from the project root.

### Local build

```bash
mvn compile
```

### Local test

```bash
mvn test
```

Runs the JUnit 5 suite (`CharacterizationTest`) through Maven Surefire. No manual
classpath setup is required.

### Package creation

```bash
mvn package
```

Compiles, runs the tests, and produces a runnable jar at `target/uno-cli.jar`.

### Local run

After packaging, run the jar:

```bash
java -jar target/uno-cli.jar --bots 3 --games 1
```

Or compile and run in one step without packaging:

```bash
mvn compile exec:java -Dexec.args="--bots 3 --games 1"
```

Play an interactive game:

```bash
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

## Docker

The image builds the application entirely from the repository, so no local Java
or Maven install is needed to run it.

### Docker build

```bash
docker build -t uno-cli .
```

### Docker run

Bot-only game:

```bash
docker run --rm uno-cli
```

Pass options after the image name:

```bash
docker run --rm uno-cli --bots 4 --games 3
```

Interactive game (the `-it` flags attach your terminal for input):

```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

## Command-line options

| Option      | Description                          | Default |
|-------------|--------------------------------------|---------|
| `--bots N`  | number of bot players                | `3`     |
| `--games N` | number of games to play             | `1`     |
| `--human`   | add a human player (you)            | off     |
| `--quiet`   | suppress per-turn output            | off     |
| `--seed N`  | random seed for reproducible games  | `10`    |
| `--help`    | print usage and exit                |         |

Player count (bots plus the optional human) must be between 2 and 4.

## Card input (interactive mode)

Choose a card by its index in your hand or by its code, or type `draw`.

```text
R5    red 5
YS    yellow skip
BR    blue reverse
G+2   green draw two
W     wild
W4    wild draw four
draw  draw a card
```

## Logging

Game events (game start, each player turn, cards played and drawn, invalid
input, and round/game end) are logged via `java.util.logging` to `logs/uno.log`.
Logging is kept separate from the player-facing console output so the CLI stays
readable. The log file is written to the working directory of the running
process (inside the container when run with Docker).
