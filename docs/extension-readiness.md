# Extension Readiness

## Which extension would this design support best?

Adding a smarter bot strategy, or swapping in a completely different one,
is the easiest extension this design supports.

## Where would that change be implemented?

`BotStrategy` is a stateless class with two static methods:

- `chooseCard(hand, upCard, calledColor)` — picks which card to play
- `chooseColor(hand)` — picks which color to call after a wild

To add a smarter bot you would either:

- Add logic inside `chooseCard` (e.g. track which colors opponents are low on)
- Create a second class like `AggressiveBotStrategy` with the same method
  signatures and pass it in as a parameter to `GameRunner`

Neither change touches `Rules`, `GameState`, `ConsoleView`, or `Main`.
The bot decision logic is fully isolated from the rest of the game.

A second easy extension would be replacing or improving the CLI view.
`ConsoleView` is the only class that calls `System.out`. Swapping it for
a richer terminal UI or a log-based replay view would require no changes
to game logic at all.

## What part of the design still makes change difficult?

The hardest extension to add right now would be a new card effect or rule
variant. Here is why:

- `applyCardEffect` in `GameRunner` is a switch statement over card ranks.
  Adding a new card type means editing that method directly rather than
  adding a new class.
- `Card` uses string codes like `"RS"` and `"W4"`. Adding a new card type
  means updating the parsing logic in `Card.rank()` and `Card.color()` in
  multiple places.
- There is no formal concept of a card effect as an object. If the game
  grew to have many card types, a `CardEffect` interface with implementations
  per card type would scale better than the current switch.

For the scope of this project these are acceptable tradeoffs. The design
is meaningfully better than the original monolith and the riskiest areas
are isolated enough that a careful developer could extend them without
breaking unrelated behavior.