# Supported UNO Rules

This lists which rules from `Final_Project_UNO_rules_reference.md` are
implemented, and the variants/simplifications used. Where behavior differs from
official UNO, the difference is noted.

## Deck Composition — implemented

Standard 108-card deck ([DeckBuilder](../src/main/java/DeckBuilder.java)):

- four colors (red, yellow, green, blue)
- one `0` and two each of `1`–`9` per color (76 number cards)
- two `Skip`, two `Reverse`, two `Draw Two` per color (24 action cards)
- four `Wild` and four `Wild Draw Four` (8 wild cards)

## Basic Turn Flow — implemented

Each player is dealt 7 cards; one card is turned up to start the discard pile.
On a turn a player plays a legal card or draws.

**Starting card variant:** if the first up card is a `Wild`/`Wild Draw Four` it is
redrawn until a non-wild card appears. A non-wild **action** card (Skip/Reverse/
Draw Two) is left as the top card and its effect is **not** applied at game start.

## Legal Play Validation — implemented

A card is legal ([Rules.isLegal](../src/main/java/Rules.java)) if it matches the
active color, matches the top card's number, matches the top card's action type,
or is a Wild / Wild Draw Four. After a wild, the chosen color becomes the active
color. Illegal plays are rejected (human is re-prompted; a forced illegal play
draws a penalty card).

## Skip — implemented

The next player loses their turn; play continues with the following player.

## Reverse — implemented

Play direction reverses for three or more players.
**Two-player variant:** `Reverse` acts as `Skip` (the opponent is skipped and the
player goes again).

## Draw Two — implemented

The next player draws two cards and loses their turn.
**Simplification:** Draw Two cards cannot be stacked.

## Wild — implemented

The player chooses the next active color (human is prompted; a bot picks the
color it holds most of). The chosen color is used for subsequent legal-play
checks. The next player takes a normal turn.

## Wild Draw Four — implemented

The player chooses the next active color; the next player draws four cards and
loses their turn.
**Simplification:** no challenge rule.

## Draw / Pass — implemented

Variant used: **draw one card, then play it immediately if it is legal, otherwise
pass.** A human is asked whether to play the drawn card; a bot plays it
automatically when legal. If no legal play exists, the turn passes.

## UNO Call and Missed-UNO Penalty — implemented

The one-card state is detected the moment a player plays down to a single card.

- **Humans** are prompted to call UNO (type `UNO`). If they do not, the
  missed-UNO penalty applies **immediately**: they draw two cards.
- **Bots** always call UNO (simplification: bots never miss).

Timing rule: the missed-UNO check happens at the instant the second-to-last card
is played. The penalty logic is `Rules.missedUno(handSize, calledUno)` and is unit
tested.

## Round End and Scoring — implemented

A round ends when a player empties their hand. The round winner scores the value
of all cards left in opponents' hands, using standard values (number = face
value, Skip/Reverse/Draw Two = 20, Wild/Wild Draw Four = 50).

## Multi-Round Target Score — implemented

With `--target N`, rounds continue until a player reaches or exceeds `N` points
(e.g., `--target 500`); that player is declared the overall winner. Without
`--target`, the game plays a fixed number of rounds (`--games N`, default 1).
The final winner is the highest total scorer (`Rules.leader`).

## Acceptable Simplifications Used

- no Wild Draw Four challenge rule
- no Draw Two / Wild Draw Four stacking
- simple bot strategy (priority: Draw Two, Skip, number, then Wild)
- text-only CLI
- target score is configurable (default mode plays fixed rounds)
- bots auto-call UNO
- deterministic deck setup in tests via a fixed RNG seed
