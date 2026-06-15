import java.util.ArrayList;

public class GameState {

    ArrayList<String> playerNames = new ArrayList<>();
    ArrayList<Boolean> humanPlayers = new ArrayList<>();
    ArrayList<ArrayList<String>> hands = new ArrayList<>();
    ArrayList<String> deck = new ArrayList<>();
    ArrayList<String> discard = new ArrayList<>();
    int[] scores = new int[10];
    int currentPlayer = 0;
    int direction = 1;
    String upCard = "";
    String calledColor = "";

    //Advances currentPlayer by direction, wrapping around.
    public void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    /**
     * Draws the top card from the deck.
     * If the deck is empty, shuffles the discard pile back in.
     * If both are empty, returns "W" as a safety fallback.
     */
    public String draw(java.util.Random random) {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            java.util.Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) {
            return "W";
        }
        return deck.remove(0);
    }
}