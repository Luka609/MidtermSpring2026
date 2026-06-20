import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the rule features added in the final project: UNO call/penalty,
 * multi-round target scoring, fuller deck-composition checks, the Wild Draw
 * Four effect, and the draw/pass decision.
 */
class FinalRulesTest {

    // -- UNO CALL AND PENALTY --

    @Test
    void missedUno_true_whenAtOneCardAndNotCalled() {
        assertTrue(Rules.missedUno(1, false));
    }

    @Test
    void missedUno_false_whenCalled() {
        assertFalse(Rules.missedUno(1, true));
    }

    @Test
    void missedUno_false_whenMoreThanOneCard() {
        assertFalse(Rules.missedUno(2, false));
        assertFalse(Rules.missedUno(7, false));
    }

    // -- MULTI-ROUND TARGET SCORE --

    @Test
    void reachedTarget_true_whenAnyPlayerAtOrAboveTarget() {
        assertTrue(Rules.reachedTarget(new int[]{120, 510, 0}, 3, 500));
        assertTrue(Rules.reachedTarget(new int[]{500, 10}, 2, 500));
    }

    @Test
    void reachedTarget_false_whenAllBelowTarget() {
        assertFalse(Rules.reachedTarget(new int[]{120, 499, 300}, 3, 500));
    }

    @Test
    void reachedTarget_ignoresStaleSlotsBeyondPlayerCount() {
        // the score array is fixed-size; unused slots must not end the game
        int[] scores = new int[10];
        scores[5] = 999;
        assertFalse(Rules.reachedTarget(scores, 3, 500));
    }

    @Test
    void leader_returnsHighestScoringPlayer() {
        assertEquals(1, Rules.leader(new int[]{30, 80, 50}, 3));
    }

    @Test
    void leader_breaksTiesByLowestIndex() {
        assertEquals(0, Rules.leader(new int[]{40, 40, 10}, 3));
    }

    // -- DECK COMPOSITION --

    @Test
    void deck_hasCorrectCountOfEachCardType() {
        ArrayList<String> deck = DeckBuilder.buildStandardDeck();
        assertEquals(108, deck.size());
        assertEquals(76, countRank(deck, "NUMBER"));
        assertEquals(8, countRank(deck, "SKIP"));
        assertEquals(8, countRank(deck, "REVERSE"));
        assertEquals(8, countRank(deck, "DRAW_TWO"));
        assertEquals(4, countRank(deck, "WILD"));
        assertEquals(4, countRank(deck, "WILD_DRAW_FOUR"));
    }

    @Test
    void deck_containsAllFourColors() {
        ArrayList<String> deck = DeckBuilder.buildStandardDeck();
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            assertTrue(deck.stream().anyMatch(c -> new Card(c).color().equals(color)),
                    "missing color " + color);
        }
    }

    // -- WILD DRAW FOUR EFFECT --

    @Test
    void wildDrawFour_addsFourCardsToNextPlayer() {
        GameState state = new GameState();
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.hands.add(new ArrayList<>());
        state.hands.add(new ArrayList<>());
        for (int i = 0; i < 6; i++) {
            state.deck.add("R" + i);
        }
        state.currentPlayer = 0;
        state.direction = 1;

        state.next();
        for (int i = 0; i < 4; i++) {
            state.hands.get(state.currentPlayer).add(state.draw(new Random(0)));
        }

        assertEquals(4, state.hands.get(1).size());
    }

    // -- DRAW / PASS DECISION --

    @Test
    void drawnCard_isPlayable_whenLegal_otherwisePass() {
        // a drawn card matching the up card may be played
        assertTrue(Rules.isLegal(new Card("R5"), new Card("R2"), ""));
        // a drawn card matching nothing means the player passes
        assertFalse(Rules.isLegal(new Card("B5"), new Card("R2"), ""));
    }

    private long countRank(ArrayList<String> deck, String rank) {
        return deck.stream().filter(c -> new Card(c).rank().equals(rank)).count();
    }
}
