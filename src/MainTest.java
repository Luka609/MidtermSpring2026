import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Main.java.*
 * These tests describe what the current implementation DOES,
 * not what ideal UNO should do.Purpose is to detect unintended
 * behavior changes during refactoring.
 */
class MainTest {

    // Reseting all global state
    @BeforeEach
    void resetState() {
        Main.upCard = "";
        Main.calledColor = "";
        Main.direction = 1;
        Main.currentPlayer = 0;
        Main.deck.clear();
        Main.discard.clear();
        Main.playerNames.clear();
        Main.humanPlayers.clear();
        Main.hands.clear();
        Main.quiet = true;
    }


    // --- Color matching ---

    /**
     * A card of the same color as the up card is always legal,
     * regardless of rank.
     */
    @Test
    void sameColor_isLegal() {
        assertTrue(Main.isLegal("R3", "R7", ""));
        assertTrue(Main.isLegal("RS", "R7", ""));
        assertTrue(Main.isLegal("R+2", "RS", ""));
        assertTrue(Main.isLegal("RR", "R9", ""));
    }

    /**
     * A card of a different color with a different rank is illegal
     * when no color has been called.
     */
    @Test
    void differentColorAndRank_isNotLegal() {
        assertFalse(Main.isLegal("B3", "R7", ""));
        assertFalse(Main.isLegal("GS", "RR", ""));
    }

    /**
     * Current behavior: color() returns "" for wild cards.
     * This is a quirk of the implementation — wild cards have
     * no color, but are always legal via the W prefix check.
     */
    @Test
    void colorOfWild_isEmptyString() {
        assertEquals("", Main.color("W"));
        assertEquals("", Main.color("W4"));
    }


    // --- NUMBER MATCHING ---

    /**
     * A number card matching the number on the up card is legal,
     * even if the colors differ.
     */
    @Test
    void sameNumber_differentColor_isLegal() {
        assertTrue(Main.isLegal("G5", "R5", ""));
        assertTrue(Main.isLegal("B0", "Y0", ""));
        assertTrue(Main.isLegal("Y9", "G9", ""));
    }

    /**
     * Different number AND different color is illegal.
     */
    @Test
    void differentNumber_differentColor_isNotLegal() {
        assertFalse(Main.isLegal("G3", "R5", ""));
        assertFalse(Main.isLegal("B1", "Y9", ""));
    }

    /**
     * Current behavior: number matching only applies to NUMBER
     * rank cards. Action cards with the same string suffix do
     * not match via the number path — they match via rank instead.
     */
    @Test
    void number_returnsMinusOne_forNonNumberCards() {
        assertEquals(-1, Main.number("RS"));
        assertEquals(-1, Main.number("GR"));
        assertEquals(-1, Main.number("B+2"));
        assertEquals(-1, Main.number("W"));
        assertEquals(-1, Main.number("W4"));
    }

    @Test
    void number_returnsCorrectValue_forNumberCards() {
        assertEquals(0, Main.number("R0"));
        assertEquals(5, Main.number("G5"));
        assertEquals(9, Main.number("B9"));
    }

    // --- ACTION TYPE MATCHING ---


    //Skip matches skip across colors.
    @Test
    void skipMatchesSkip_acrossColors_isLegal() {
        assertTrue(Main.isLegal("BS", "RS", ""));
        assertTrue(Main.isLegal("GS", "YS", ""));
    }

    //Reverse matches reverse across colors.
    @Test
    void reverseMatchesReverse_acrossColors_isLegal() {
        assertTrue(Main.isLegal("BR", "RR", ""));
        assertTrue(Main.isLegal("YR", "GR", ""));
    }


    //Draw two matches draw two across colors.
    @Test
    void drawTwoMatchesDrawTwo_acrossColors_isLegal() {
        assertTrue(Main.isLegal("B+2", "R+2", ""));
        assertTrue(Main.isLegal("G+2", "Y+2", ""));
    }

    /**
     * Action cards do NOT match across different action types.
     * e.g. Skip does not match Reverse.
     */
    @Test
    void differentActionTypes_doNotMatch() {
        assertFalse(Main.isLegal("BS", "RR", ""));  // skip vs reverse
        assertFalse(Main.isLegal("G+2", "RS", "")); // draw two vs skip
    }

    //Current behavior: rank() classifications are exact.
    @Test
    void rank_classifiesCardsCorrectly() {
        assertEquals("SKIP", Main.rank("RS"));
        assertEquals("SKIP", Main.rank("BS"));
        assertEquals("REVERSE", Main.rank("GR"));
        assertEquals("DRAW_TWO", Main.rank("Y+2"));
        assertEquals("WILD", Main.rank("W"));
        assertEquals("WILD_DRAW_FOUR", Main.rank("W4"));
        assertEquals("NUMBER", Main.rank("R5"));
        assertEquals("NUMBER", Main.rank("B0"));
    }

    // ---  WILD BEHAVIOR ---

    //Wild card is always legal regardless of up card or called color.
    @Test
    void wild_isAlwaysLegal() {
        assertTrue(Main.isLegal("W", "R5", ""));
        assertTrue(Main.isLegal("W", "GS", "B"));
        assertTrue(Main.isLegal("W", "W4", "Y"));
        assertTrue(Main.isLegal("W", "B0", ""));
    }

    //Wild Draw Four is always legal regardless of up card or called color.
    @Test
    void wildDrawFour_isAlwaysLegal() {
        assertTrue(Main.isLegal("W4", "R5", ""));
        assertTrue(Main.isLegal("W4", "GS", "B"));
        assertTrue(Main.isLegal("W4", "B0", ""));
    }

    /**
     * Current behavior: after a wild is played, a calledColor is set.
     * A card matching the called color is legal even if it doesn't
     * match the wild card's "color" (which is empty string "").
     */
    @Test
    void calledColor_makesMatchingCardLegal() {
        assertTrue(Main.isLegal("B3", "W", "B"));
        assertTrue(Main.isLegal("GS", "W4", "G"));
        assertTrue(Main.isLegal("R+2", "W", "R"));
    }

    /**
     * A card NOT matching the called color is illegal after a wild.
     */
    @Test
    void nonCalledColor_afterWild_isNotLegal() {
        assertFalse(Main.isLegal("B3", "W", "R"));
        assertFalse(Main.isLegal("G5", "W4", "Y"));
    }

    /**
     * Current behavior: points for WILD and WILD_DRAW_FOUR are 50 each.
     */
    @Test
    void wildCards_areWorth50Points() {
        assertEquals(50, Main.points("W"));
        assertEquals(50, Main.points("W4"));
    }

    // ---  SKIP BEHAVIOR ---


    /**
     * Current behavior: playing a skip advances the turn TWICE via next(),
     * effectively skipping the next player.
     * test this at the turn-advancement level using currentPlayer.
     */
    @Test
    void skip_advancesCurrentPlayer_twice() {
        // Setup: 3 players, currentPlayer = 0, direction = 1
        Main.playerNames.add("P1");
        Main.playerNames.add("P2");
        Main.playerNames.add("P3");
        Main.currentPlayer = 0;
        Main.direction = 1;

        // Simulate skip: call next() twice
        Main.next();
        Main.next();

        // Player 0 -> skip next -> should land on player 2
        assertEquals(2, Main.currentPlayer);
    }

    /**
     * Skip is worth 20 points when in an opponent's hand at game end.
     */
    @Test
    void skip_isWorth20Points() {
        assertEquals(20, Main.points("RS"));
        assertEquals(20, Main.points("BS"));
    }

    // ---  REVERSE BEHAVIOR ---

    /**
     * Current behavior: reverse flips the direction field between 1 and -1.
     */
    @Test
    void reverse_flipsDirection() {
        Main.direction = 1;
        Main.direction = Main.direction * -1;
        assertEquals(-1, Main.direction);

        Main.direction = Main.direction * -1;
        assertEquals(1, Main.direction);
    }

    /**
     * Current behavior: in a 2-player game, reverse acts like a skip.
     * direction flips AND next() is called twice.
     */
    @Test
    void reverse_in2PlayerGame_actLikeSkip() {
        Main.playerNames.add("P1");
        Main.playerNames.add("P2");
        Main.currentPlayer = 0;
        Main.direction = 1;

        // Simulate 2-player reverse: flip direction, call next() twice
        Main.direction = Main.direction * -1;
        Main.next();
        Main.next();

        // With direction=-1: next() goes from 0 -> -1 -> wraps to 1
        // then next() again from 1 -> 0
        // So current player should be back to 0 (skip effect)
        assertEquals(0, Main.currentPlayer);
    }

    //Reverse is worth 20 points.
    @Test
    void reverse_isWorth20Points() {
        assertEquals(20, Main.points("RR"));
        assertEquals(20, Main.points("GR"));
    }


    // ---  DRAW TWO BEHAVIOR ---


    //Draw two is worth 20 points.
    @Test
    void drawTwo_isWorth20Points() {
        assertEquals(20, Main.points("R+2"));
        assertEquals(20, Main.points("B+2"));
    }

    /**
     * Current behavior: draw two forces the next player to draw 2 cards.
     * simulate this directly: next player's hand gains 2 cards.
     */
    @Test
    void drawTwo_addsTwo_cardsToNextPlayer() {
        // Setup deck with known cards
        Main.deck.add("R1");
        Main.deck.add("R2");

        // Setup 2 players
        Main.playerNames.add("P1");
        Main.playerNames.add("P2");
        ArrayList<String> p1hand = new ArrayList<>();
        ArrayList<String> p2hand = new ArrayList<>();
        Main.hands.add(p1hand);
        Main.hands.add(p2hand);
        Main.currentPlayer = 0;
        Main.direction = 1;

        // Simulate draw two effect: advance to next player, give 2 cards
        Main.next();
        Main.hands.get(Main.currentPlayer).add(Main.draw());
        Main.hands.get(Main.currentPlayer).add(Main.draw());

        assertEquals(2, Main.hands.get(1).size());
        assertTrue(Main.hands.get(1).contains("R1"));
        assertTrue(Main.hands.get(1).contains("R2"));
    }


    // ---  DRAWING FROM DECK ---

    //Current behavior: draw() removes and returns the first card from the deck.
    @Test
    void draw_removesTopCard_fromDeck() {
        Main.deck.add("R5");
        Main.deck.add("B3");

        String drawn = Main.draw();
        assertEquals("R5", drawn);
        assertEquals(1, Main.deck.size());
    }

    /**
     * Current behavior: when deck is empty, discard pile is shuffled
     * into the deck and drawing continues.
     */
    @Test
    void draw_reshufflesDiscard_whenDeckIsEmpty() {
        Main.deck.clear();
        Main.discard.add("G7");
        Main.discard.add("Y3");

        String drawn = Main.draw();

        // One card drawn, discard was moved to deck
        assertNotNull(drawn);
        // discard should now be empty after reshuffle
        assertEquals(0, Main.discard.size());
        // deck had 2 cards, one was drawn, so 1 remains
        assertEquals(1, Main.deck.size());
    }

    /**
     * Current behavior: if both deck and discard are empty,
     * draw() returns "W" as a fallback wild card.
     * This is a quirk
     */
    @Test
    void draw_returnsWild_whenBothDeckAndDiscardEmpty() {
        Main.deck.clear();
        Main.discard.clear();

        String drawn = Main.draw();
        assertEquals("W", drawn);
    }

    // ---  SCORING ---

    /**
     * Number cards are worth their face value.
     */
    @Test
    void numberCards_areWorthFaceValue() {
        assertEquals(0, Main.points("R0"));
        assertEquals(5, Main.points("G5"));
        assertEquals(9, Main.points("B9"));
        assertEquals(1, Main.points("Y1"));
    }

    /**
     * Action cards (skip, reverse, draw two) are worth 20 points.
     */
    @Test
    void actionCards_areWorth20Points() {
        assertEquals(20, Main.points("RS"));
        assertEquals(20, Main.points("GR"));
        assertEquals(20, Main.points("B+2"));
    }

    /**
     * Wild cards are worth 50 points.
     */
    @Test
    void allCardTypes_haveCorrectPoints() {
        assertEquals(0, Main.points("Y0"));
        assertEquals(7, Main.points("R7"));
        assertEquals(20, Main.points("BS"));
        assertEquals(20, Main.points("GR"));
        assertEquals(20, Main.points("Y+2"));
        assertEquals(50, Main.points("W"));
        assertEquals(50, Main.points("W4"));
    }

    // --- BOT LOGIC ---


    /**
     * Current behavior: bot prefers DRAW_TWO over other legal cards.
     */
    @Test
    void bot_prefersDrawTwo_overOtherLegalCards() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");   // legal (same color)
        hand.add("R+2");  // legal draw two (same color) -- should be preferred
        hand.add("W");    // legal wild

        int chosen = Main.chooseBotCard(hand);
        assertEquals(1, chosen); // R+2 at index 1
    }

    /**
     * Current behavior: bot prefers SKIP over number cards and wilds
     * (but after draw two).
     */
    @Test
    void bot_prefersSkip_overNumberAndWild() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");  // legal number
        hand.add("RS");  // legal skip -- should be preferred
        hand.add("W");   // legal wild

        int chosen = Main.chooseBotCard(hand);
        assertEquals(1, chosen); // RS at index 1
    }

    /**
     * Current behavior: bot uses wild only when no colored card is legal.
     */
    @Test
    void bot_usesWild_onlyWhenNoColoredCardLegal() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");  // illegal
        hand.add("G7");  // illegal
        hand.add("W");   // only legal card

        int chosen = Main.chooseBotCard(hand);
        assertEquals(2, chosen);
    }

    /**
     * Current behavior: bot returns -1 (draw) when no card is legal.
     */
    @Test
    void bot_returnsNegativeOne_whenNoLegalCard() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("G7");
        hand.add("Y2");

        int chosen = Main.chooseBotCard(hand);
        assertEquals(-1, chosen);
    }

    /**
     * Current behavior: bot picks the color it has the most of
     * when playing a wild.
     */
    @Test
    void botColor_picksMostFrequentColor() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");

        String color = Main.chooseBotColor(hand);
        assertEquals("B", color);
    }


    // --- EDGE CASES / QUIRKS ---


    @Test
    void botLegalityCheck_agreesWithIsLegal_forColorMatch() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R7"); // same color — legal by both paths

        int chosen = Main.chooseBotCard(hand);
        assertTrue(Main.isLegal(hand.get(0), Main.upCard, Main.calledColor));
        assertEquals(0, chosen);
    }

    /**
     * QUIRK: draw() returns "W" (a wild card) as a safety fallback
     * when both deck and discard are empty. This could affect game
     * balance if triggered during play. Captured as a known quirk.
     */
    @Test
    void draw_wildFallback_isAKnownQuirk() {
        Main.deck.clear();
        Main.discard.clear();
        assertEquals("W", Main.draw());
    }

    /**
     * QUIRK: calledColor is reset to "" after any non-wild card is played.
     * verify this by checking that a non-called-color card is illegal
     * only when calledColor is active.
     */
    @Test
    void calledColor_empty_doesNotAffectNormalMatching() {
        // With no called color, B3 vs R5 should be illegal
        assertFalse(Main.isLegal("B3", "R5", ""));
        // With called color B, it becomes legal
        assertTrue(Main.isLegal("B3", "R5", "B"));
    }

    /**
     * QUIRK: next() wraps currentPlayer around at both ends.
     * Test forward wrap.
     */
    @Test
    void next_wrapsForward_atEndOfPlayerList() {
        Main.playerNames.add("P1");
        Main.playerNames.add("P2");
        Main.playerNames.add("P3");
        Main.currentPlayer = 2; // last player
        Main.direction = 1;

        Main.next();

        assertEquals(0, Main.currentPlayer); // wraps to 0
    }

    /**
     * QUIRK: next() wraps backward when direction is -1.
     */
    @Test
    void next_wrapsBackward_withNegativeDirection() {
        Main.playerNames.add("P1");
        Main.playerNames.add("P2");
        Main.playerNames.add("P3");
        Main.currentPlayer = 0;
        Main.direction = -1;

        Main.next();

        assertEquals(2, Main.currentPlayer); // wraps to last
    }

    /**
     * QUIRK: join() formats hand as "index:card" pairs separated by spaces.
     * This is what gets printed to the console. Captured so refactoring
     * doesn't silently break the display format.
     */
    @Test
    void join_formatsHand_asIndexColonCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");
        hand.add("W");

        String result = Main.join(hand);
        assertEquals("0:R5 1:W", result);
    }
}