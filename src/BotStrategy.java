import java.util.ArrayList;

/**
 * Encapsulates bot decision logic for the UNO CLI game.
 * Bot decisions depend only on the hand, up card, and called color.
 * This class has no dependency on console I/O or global state,
 * making bot behavior independently testable.
 * Bot card priority (preserved from original implementation):
 * 1. Draw Two (aggressive)
 * 2. Skip (aggressive)
 * 3. Number cards (safe)
 * 4. Wild (last resort)
 */
public class BotStrategy {

    /**
     * Chooses the best card index to play from the bot's hand.
     * Returns -1 if no legal card exists (bot must draw).
     */
    public static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        // Priority 1: play Draw Two if legal
        for (int i = 0; i < hand.size(); i++) {
            Card card = new Card(hand.get(i));
            if (card.rank().equals("DRAW_TWO")
                    && Rules.isLegal(card, new Card(upCard), calledColor)) {
                return i;
            }
        }
        // Priority 2: play Skip if legal
        for (int i = 0; i < hand.size(); i++) {
            Card card = new Card(hand.get(i));
            if (card.rank().equals("SKIP")
                    && Rules.isLegal(card, new Card(upCard), calledColor)) {
                return i;
            }
        }
        // Priority 3: play a Number card if legal
        for (int i = 0; i < hand.size(); i++) {
            Card card = new Card(hand.get(i));
            if (card.rank().equals("NUMBER")
                    && Rules.isLegal(card, new Card(upCard), calledColor)) {
                return i;
            }
        }
        // Priority 4: use a Wild as last resort
        for (int i = 0; i < hand.size(); i++) {
            if (new Card(hand.get(i)).isWild()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Chooses the best color to call after playing a wild.
     * Picks the color the bot holds the most of.
     * Preserved from original implementation.
     */
    public static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (String code : hand) {
            String c = new Card(code).color();
            switch (c) {
                case "R" -> r++;
                case "Y" -> y++;
                case "G" -> g++;
                case "B" -> b++;
            }
        }
        if (r >= y && r >= g && r >= b) return "R";
        else if (y >= r && y >= g && y >= b) return "Y";
        else if (g >= r && g >= y && g >= b) return "G";
        else return "B";
    }
}