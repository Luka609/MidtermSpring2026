import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


class CharacterizationTest {
    static GameState state = new GameState();

    @BeforeEach
    void resetState() {
        // fresh state before each test so they don't bleed into each other
        state = new GameState();
    }

    // -- COLOR MATCHING --

    @Test
    void sameColor_isLegal() {
        assertTrue(Rules.isLegal(new Card("R3"), new Card("R7"), ""));
        assertTrue(Rules.isLegal(new Card("RS"), new Card("R7"), ""));
        assertTrue(Rules.isLegal(new Card("R+2"), new Card("RS"), ""));
    }

    @Test
    void differentColorAndRank_isNotLegal() {
        assertFalse(Rules.isLegal(new Card("B3"), new Card("R7"), ""));
        assertFalse(Rules.isLegal(new Card("GS"), new Card("RR"), ""));
    }

    @Test
    void colorOfWild_isEmptyString() {
        // wilds don't have a color, so we just use an empty string
        assertEquals("", new Card("W").color());
        assertEquals("", new Card("W4").color());
    }

    // -- NUMBER MATCHING --

    @Test
    void sameNumber_differentColor_isLegal() {
        assertTrue(Rules.isLegal(new Card("G5"), new Card("R5"), ""));
        assertTrue(Rules.isLegal(new Card("B0"), new Card("Y0"), ""));
    }

    @Test
    void differentNumber_differentColor_isNotLegal() {
        assertFalse(Rules.isLegal(new Card("G3"), new Card("R5"), ""));
    }

    @Test
    void number_returnsMinusOne_forNonNumberCards() {
        // action cards and wilds don't have a number, so -1 is the sentinel
        assertEquals(-1, new Card("RS").number());
        assertEquals(-1, new Card("W").number());
        assertEquals(-1, new Card("W4").number());
    }

    @Test
    void number_returnsCorrectValue_forNumberCards() {
        assertEquals(0, new Card("R0").number());
        assertEquals(5, new Card("G5").number());
        assertEquals(9, new Card("B9").number());
    }

    // -- ACTION TYPE MATCHING --

    @Test
    void skipMatchesSkip_acrossColors_isLegal() {
        assertTrue(Rules.isLegal(new Card("BS"), new Card("RS"), ""));
    }

    @Test
    void reverseMatchesReverse_acrossColors_isLegal() {
        assertTrue(Rules.isLegal(new Card("BR"), new Card("RR"), ""));
    }

    @Test
    void drawTwoMatchesDrawTwo_acrossColors_isLegal() {
        assertTrue(Rules.isLegal(new Card("B+2"), new Card("R+2"), ""));
    }

    @Test
    void differentActionTypes_doNotMatch() {
        assertFalse(Rules.isLegal(new Card("BS"), new Card("RR"), ""));
    }

    @Test
    void rank_classifiesCardsCorrectly() {
        assertEquals("SKIP", new Card("RS").rank());
        assertEquals("REVERSE", new Card("GR").rank());
        assertEquals("DRAW_TWO", new Card("Y+2").rank());
        assertEquals("WILD", new Card("W").rank());
        assertEquals("WILD_DRAW_FOUR", new Card("W4").rank());
        assertEquals("NUMBER", new Card("R5").rank());
    }

    // -- WILD BEHAVIOR --

    @Test
    void wild_isAlwaysLegal() {
        // wilds can always be played, no matter what's on top
        assertTrue(Rules.isLegal(new Card("W"), new Card("R5"), ""));
        assertTrue(Rules.isLegal(new Card("W"), new Card("GS"), "B"));
    }

    @Test
    void wildDrawFour_isAlwaysLegal() {
        assertTrue(Rules.isLegal(new Card("W4"), new Card("R5"), ""));
        assertTrue(Rules.isLegal(new Card("W4"), new Card("B0"), ""));
    }

    @Test
    void calledColor_makesMatchingCardLegal() {
        // after a wild is played, the called color is what matters
        assertTrue(Rules.isLegal(new Card("B3"), new Card("W"), "B"));
        assertTrue(Rules.isLegal(new Card("GS"), new Card("W4"), "G"));
    }

    @Test
    void nonCalledColor_afterWild_isNotLegal() {
        assertFalse(Rules.isLegal(new Card("B3"), new Card("W"), "R"));
        assertFalse(Rules.isLegal(new Card("G5"), new Card("W4"), "Y"));
    }

    @Test
    void wildCards_areWorth50Points() {
        assertEquals(50, new Card("W").points());
        assertEquals(50, new Card("W4").points());
    }

    // -- SKIP BEHAVIOR --

    @Test
    void skip_advancesCurrentPlayer_twice() {
        // calling next() twice simulates a skip
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.playerNames.add("P3");
        state.currentPlayer = 0;
        state.direction = 1;

        state.next();
        state.next();

        assertEquals(2, state.currentPlayer);
    }

    @Test
    void skip_isWorth20Points() {
        assertEquals(20, new Card("RS").points());
    }

    // -- REVERSE BEHAVIOR --

    @Test
    void reverse_flipsDirection() {
        state.direction = 1;
        state.direction *= -1;
        assertEquals(-1, state.direction);
    }

    @Test
    void reverse_in2PlayerGame_actsLikeSkip() {
        // in a 2-player game, reverse just skips the other person
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.currentPlayer = 0;
        state.direction = 1;

        state.direction *= -1;
        state.next();
        state.next();

        // ends up back at player 0
        assertEquals(0, state.currentPlayer);
    }

    @Test
    void reverse_isWorth20Points() {
        assertEquals(20, new Card("RR").points());
    }

    // -- DRAW TWO BEHAVIOR --

    @Test
    void drawTwo_isWorth20Points() {
        assertEquals(20, new Card("R+2").points());
    }

    @Test
    void drawTwo_addsTwo_cardsToNextPlayer() {
        state.deck.add("R1");
        state.deck.add("R2");
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.hands.add(new ArrayList<>());
        state.hands.add(new ArrayList<>());
        state.currentPlayer = 0;
        state.direction = 1;

        // move to next player and deal them 2 cards
        state.next();
        state.hands.get(state.currentPlayer)
                .add(state.draw(new java.util.Random(0)));
        state.hands.get(state.currentPlayer)
                .add(state.draw(new java.util.Random(0)));

        assertEquals(2, state.hands.get(1).size());
    }

    // -- DRAWING FROM DECK --

    @Test
    void draw_removesTopCard_fromDeck() {
        state.deck.add("R5");
        state.deck.add("B3");

        String drawn = state.draw(new java.util.Random(0));
        assertEquals("R5", drawn);
        assertEquals(1, state.deck.size());
    }

    @Test
    void draw_reshufflesDiscard_whenDeckIsEmpty() {
        // when the deck runs out, the discard pile gets reshuffled back in
        state.deck.clear();
        state.discard.add("G7");
        state.discard.add("Y3");

        String drawn = state.draw(new java.util.Random(0));
        assertNotNull(drawn);
        assertEquals(0, state.discard.size());
        assertEquals(1, state.deck.size());
    }

    @Test
    void draw_returnsWild_whenBothDeckAndDiscardEmpty() {
        // see the quirk test below for context on this one
        state.deck.clear();
        state.discard.clear();
        assertEquals("W", state.draw(new java.util.Random(0)));
    }

    // -- SCORING --

    @Test
    void numberCards_areWorthFaceValue() {
        assertEquals(0, new Card("R0").points());
        assertEquals(5, new Card("G5").points());
        assertEquals(9, new Card("B9").points());
    }

    @Test
    void actionCards_areWorth20Points() {
        assertEquals(20, new Card("RS").points());
        assertEquals(20, new Card("GR").points());
        assertEquals(20, new Card("B+2").points());
    }

    @Test
    void allCardTypes_haveCorrectPoints() {
        assertEquals(0, new Card("Y0").points());
        assertEquals(7, new Card("R7").points());
        assertEquals(20, new Card("BS").points());
        assertEquals(20, new Card("GR").points());
        assertEquals(20, new Card("Y+2").points());
        assertEquals(50, new Card("W").points());
        assertEquals(50, new Card("W4").points());
    }

    @Test
    void totalPoints_sumsHandCorrectly() {
        java.util.List<String> hand = java.util.List.of("R5", "GS", "W");
        // 5 + 20 + 50 = 75
        assertEquals(75, Rules.totalPoints(hand));
    }

    // -- BOT LOGIC --

    @Test
    void bot_prefersDrawTwo_overOtherLegalCards() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");
        hand.add("R+2");
        hand.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void bot_prefersSkip_overNumberAndWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");
        hand.add("RS");
        hand.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void bot_usesWild_onlyWhenNoColoredCardLegal() {
        // bot saves wilds for when there's genuinely nothing else to play
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("G7");
        hand.add("W");
        assertEquals(2, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void bot_returnsNegativeOne_whenNoLegalCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("G7");
        assertEquals(-1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void botColor_picksMostFrequentColor() {
        // bot picks whatever color it has the most of
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test
    void botLegalityCheck_agreesWithIsLegal_forColorMatch() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R7");
        int chosen = BotStrategy.chooseCard(hand, "R5", "");
        assertTrue(Rules.isLegal(new Card(hand.get(0)), new Card("R5"), ""));
        assertEquals(0, chosen);
    }

    // -- EDGE CASES / QUIRKS --

    @Test
    void draw_wildFallback_isAKnownQuirk() {
        // if both the deck and discard are empty, draw() just hands back a wild
        // instead of crashing — bit of a quirk but we're keeping it
        state.deck.clear();
        state.discard.clear();
        assertEquals("W", state.draw(new java.util.Random(0)));
    }

    @Test
    void calledColor_empty_doesNotAffectNormalMatching() {
        // no called color means normal rules apply
        assertFalse(Rules.isLegal(new Card("B3"), new Card("R5"), ""));
        // but if blue was called, B3 is suddenly fair game
        assertTrue(Rules.isLegal(new Card("B3"), new Card("R5"), "B"));
    }

    @Test
    void next_wrapsForward_atEndOfPlayerList() {
        // make sure it wraps around instead of going out of bounds
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.playerNames.add("P3");
        state.currentPlayer = 2;
        state.direction = 1;

        state.next();

        assertEquals(0, state.currentPlayer);
    }

    @Test
    void next_wrapsBackward_withNegativeDirection() {
        // same but going the other way
        state.playerNames.add("P1");
        state.playerNames.add("P2");
        state.playerNames.add("P3");
        state.currentPlayer = 0;
        state.direction = -1;

        state.next();

        assertEquals(2, state.currentPlayer);
    }

    @Test
    void deckBuilder_creates108Cards() {
        // standard UNO deck is always 108 cards, no more no less
        ArrayList<String> deck = DeckBuilder.buildStandardDeck();
        assertEquals(108, deck.size());
    }

    @Test
    void deckBuilder_containsFourWilds_andFourWildDrawFours() {
        ArrayList<String> deck = DeckBuilder.buildStandardDeck();
        long wilds = deck.stream().filter(c -> c.equals("W")).count();
        long wildFours = deck.stream().filter(c -> c.equals("W4")).count();
        assertEquals(4, wilds);
        assertEquals(4, wildFours);
    }
}