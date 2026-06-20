# Final Report

## What UNO rules are implemented

The project implements UNO ruleset. Full details and variants are
in [rules-supported.md](rules-supported.md). Summary:

- correct 108-card deck composition
- legal-play validation (color / number / action type / wild / active color)
- Skip, Reverse (with two-player = Skip), Draw Two
- Wild and Wild Draw Four with color selection
- draw-one-then-play-if-legal, otherwise pass
- UNO call with a missed-UNO penalty (draw two)
- round scoring on remaining cards
- multi-round play to a target score with an overall winner

## How the game is played from the CLI

Build and run:

```bash
mvn package
java -jar target/uno-cli.jar --human --bots 2
```

Useful options:

| Option       | Meaning                                            |
|--------------|----------------------------------------------------|
| `--bots N`   | number of bot players                              |
| `--human`    | add yourself as a player                           |
| `--games N`  | play a fixed number of rounds (default 1)          |
| `--target N` | play rounds until a player reaches N points (e.g. 500) |
| `--seed N`   | deterministic shuffles                             |
| `--quiet`    | suppress per-turn output                           |
| `--report T` | show stats (`recent`/`wins`/`scores`/`all`) and exit |

On your turn the game prints the up card and your hand, then prompts:

```
Choose card index/code or draw:
```

Enter a card's index (e.g. `2`), its code (e.g. `R5`, `YS`, `G+2`, `W`, `W4`), or
`draw`. After a wild you choose a color (`R/Y/G/B`). When you reach one card you
are prompted to type `UNO`; forgetting draws a two-card penalty. Invalid input is
re-prompted and never crashes the game, including end-of-input.

A full multi-round game:

```bash
java -jar target/uno-cli.jar --human --bots 3 --target 500
```

## How the architecture separates game logic from CLI

Game rules and state are independent of the console, so they are unit-testable
without any input:

- [`Rules`](../src/main/java/Rules.java) — pure rule logic: legality, scoring,
  missed-UNO, target/leader checks.
- [`Card`](../src/main/java/Card.java) — card parsing, classification, points.
- [`DeckBuilder`](../src/main/java/DeckBuilder.java) — deck composition.
- [`BotStrategy`](../src/main/java/BotStrategy.java) — bot decisions.
- [`GameState`](../src/main/java/GameState.java) — turn order, draw pile, scores.

All input/output is isolated in [`ConsoleView`](../src/main/java/ConsoleView.java).
[`GameRunner`](../src/main/java/GameRunner.java) orchestrates a round using the
rule classes and `ConsoleView`, but contains no `System.in`/`System.out` itself.
[`Main`](../src/main/java/Main.java) wires everything together and runs the
multi-round session. Persistence lives in its own
[`persistence`](../src/main/java/persistence) package behind a repository, so the
game and the database stay decoupled.

Because rule behavior has a clear home in `Rules`/`Card`/`DeckBuilder`, the CLI is
not the only place where rules exist.

## What tests were added

Tests run with `mvn test` (no manual setup). **60 tests total:**

- [`CharacterizationTest`](../src/test/java/CharacterizationTest.java) (43) — the
  midterm suite: legality, action cards, wilds, scoring, deck, bot, draw pile.
- [`FinalRulesTest`](../src/test/java/FinalRulesTest.java) (12) — added for the
  final project: missed-UNO penalty, target-score and leader logic, per-type deck
  counts, the Wild Draw Four draw-four effect, and the draw/pass decision.
- [`persistence.GameRepositoryTest`](../src/test/java/persistence/GameRepositoryTest.java)
  (5) — persistence against an isolated in-memory database.

These cover card legality, action cards, wild cards, draw/pass flow, scoring, and
target-score game-over behavior.

## Limitations that remain

- No Wild Draw Four challenge rule.
- No Draw Two / Wild Draw Four stacking.
- Bots always call UNO (they never incur the missed-UNO penalty).
- A non-wild action card used as the starting up card does not apply its effect.
- Bot strategy is intentionally simple (fixed priority order).
- Two-player Reverse is treated as Skip.
