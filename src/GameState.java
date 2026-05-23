import java.util.ArrayList;

public class GameState {

    public ArrayList<String> playerNames = new ArrayList<>();
    public ArrayList<Boolean> humanPlayers = new ArrayList<>();
    public ArrayList<ArrayList<String>> hands = new ArrayList<>();
    public ArrayList<String> deck = new ArrayList<>();
    public ArrayList<String> discard = new ArrayList<>();
    public int[] scores = new int[10];
    public int currentPlayer = 0;
    public int direction = 1;
    public String upCard = "";
    public String calledColor = "";

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