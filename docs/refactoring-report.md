# Refactoring Report

## What behavior did I characterize before refactoring?

Before touching anything, I wrote 43 characterization tests covering the
existing behavior of the game as-is. These tests were written to describe
what the code actually does, not what a clean UNO implementation would do.

The behaviors I covered:

- Color matching, number matching, and action type matching
- Wild and Wild Draw Four behavior (always legal regardless of top card)
- Skip, Reverse, and Draw Two effects
- Drawing from the deck, including the reshuffle-on-empty behavior
- Scoring (number cards at face value, action cards at 20, wilds at 50)
- Bot card selection priority (Draw Two > Skip > Number > Wild)
- Bot color selection (picks most frequent color in hand)
- Turn order wrapping in both directions
- The wild fallback quirk when both deck and discard are empty

I also explicitly captured the known quirks from rules.html:

- All hands are visible in the terminal at all times
- A human can type `draw` even when holding a legal card
- Illegal index input causes a penalty card and turn loss
- Bots automatically play a drawn card if it is legal

These tests became the safety net for every refactoring step that followed.

## What were the worst design problems?

The original `Main.java` was doing everything:

- Parsing command line arguments
- Managing all mutable game state as static fields
- Containing all rule logic (legal move checks, scoring)
- Running the turn loop
- Handling console input and output
- Encoding bot decision logic
- Building the deck

The biggest problems were:

- **No separation between rules and I/O.** Testing any rule meant running
  the full CLI game. There was no way to check if a card was legal without
  triggering console output.
- **Static mutable state everywhere.** `state`, `random`, `view` were all
  static fields on `Main`, making tests interfere with each other and making
  the code impossible to reason about in isolation.
- **Duplicated legal-play logic.** The same color/rank/number checks appeared
  in multiple places across the turn loop.
- **Bot logic was buried in the turn loop.** There was no way to test bot
  decisions independently.

## Which refactorings did I perform?

Each step was committed separately with the game still passing after each one.

1. **Extract `Card` class** — moved card parsing (color, rank, number, points)
   out of scattered conditionals into a single immutable value object.

2. **Extract `Rules` class** — centralized all legal-move and scoring logic
   into a stateless class with pure methods. Rules are now testable without
   any I/O.

3. **Extract `GameState` class** — moved all mutable state (players, hands,
   deck, discard, scores, direction) into one place. Removed static fields
   from `Main`.

4. **Extract `ConsoleView` class** — moved all console input and output into
   a dedicated class. Game logic no longer calls `System.out` directly.

5. **Extract `BotStrategy` class** — moved bot card selection and color
   choice into a stateless class. Bot behavior is now independently testable.

6. **Extract `DeckBuilder` class** — moved deck construction out of the turn
   loop into a focused utility class.

7. **Extract `GameConfig` class** — moved argument parsing into an immutable
   config object, including the `--help` flag.

8. **Extract `GameRunner` class** — moved the turn loop, card effects, win
   handling, and player setup out of `Main` into a dedicated orchestrator.

9. **Clean up `Main`** — after all extractions, `Main` became a pure entry
   point that wires the pieces together and nothing else.

## What behavior did I intentionally preserve?

Everything. No documented behavior was changed. Specifically:

- The wild fallback (returning `"W"` when deck and discard are both empty)
  is preserved and documented in tests as a known quirk.
- The human draw quirk (allowed even with legal cards) is preserved.
- The penalty card behavior for illegal index input is preserved.
- Bot auto-play of drawn cards is preserved.
- All hands remain visible in the terminal.
- Card priority in bot logic (Draw Two > Skip > Number > Wild) is unchanged.

## What risks remain?

- **`GameState` fields are package-private.** Direct field access is
  restricted to classes within the same package, which reduces accidental
  corruption from outside. Full encapsulation with private fields and
  meaningful accessors would be the next step if the project grew larger.
- **`scores` is a fixed-size array of 10.** If someone tries to run a game
  with more than 10 players the scores array would break silently.
- **No integration tests.** The 43 characterization tests cover individual
  classes well, but there are no end-to-end tests that run a full game and
  check the final output. A subtle interaction bug in `GameRunner` could go
  undetected.
- **`GameRunner` is still fairly large.** It is cohesive and each method has
  a clear job, but `handlePlayPhase` in particular is doing several things
  and could be split further if the game grows.