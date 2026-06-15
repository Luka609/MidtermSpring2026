import java.util.ArrayList;

/**
 * Builds a standard UNO deck for this game variant.
 * Extracted from playGame() in Main. Deck composition is:
 * - One 0 per color
 * - Two of each 1-9 per color
 * - Two Skip per color
 * - Two Reverse per color
 * - Two Draw Two per color
 * - Four Wilds
 * - Four Wild Draw Fours
 * Total: 108 cards.
 */
public class DeckBuilder {

    public static ArrayList<String> buildStandardDeck() {
        ArrayList<String> deck = new ArrayList<>();
        String[] colors = {"R", "Y", "G", "B"};

        for (String color : colors) {
            deck.add(color + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(color + n);
                deck.add(color + n);
            }
            deck.add(color + "S");
            deck.add(color + "S");
            deck.add(color + "R");
            deck.add(color + "R");
            deck.add(color + "+2");
            deck.add(color + "+2");
        }

        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }

        return deck;
    }
}