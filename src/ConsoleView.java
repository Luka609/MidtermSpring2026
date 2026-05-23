import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles all console input and output
 * Game logic classes have no dependency on this class
 * Quirks preserved from original implementation:
 * - All hands are printed on every turn (not hidden from human)
 * - Human may type "draw" even when holding a legal card
 * - Card input by index or code both supported
 */
public class ConsoleView {

    private final Scanner scanner;
    private final boolean quiet;

    public ConsoleView(Scanner scanner, boolean quiet) {
        this.scanner = scanner;
        this.quiet = quiet;
    }

    public void showGameHeader(int gameNumber) {
        if (!quiet) {
            System.out.println("\n=== Game " + gameNumber + " ===");
        }
    }

    public void showTurnInfo(String upCard, String calledColor,
                             String playerName, ArrayList<String> hand) {
        if (!quiet) {
            System.out.println("\nUp card: " + upCard +
                    (calledColor.isEmpty() ? "" : " called " + calledColor));
            System.out.println(playerName + " hand: " + join(hand));
        }
    }

    public void showDraw(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " draws " + card);
    }

    public void showPlay(String playerName, String card) {
        if (!quiet) System.out.println(playerName + " plays " + card);
    }

    public void showCalledColor(String playerName, String color) {
        if (!quiet) System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (!quiet) System.out.println(playerName + " says UNO!");
    }

    public void showWin(String playerName, int points) {
        if (!quiet) System.out.println(playerName + " wins and scores " + points);
    }

    public void showPenalty(String playerName, String reason) {
        if (!quiet) System.out.println(playerName + " " + reason);
    }

    public void showDrawTwo(String playerName) {
        if (!quiet) System.out.println(playerName + " draws two.");
    }

    public void showDrawFour(String playerName) {
        if (!quiet) System.out.println(playerName + " draws four.");
    }

    public void showSafetyLimit() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    public void showFinalScores(ArrayList<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    /**
     * Asks the human player to choose a card.
     * Preserves quirk: human may type draw even with legal cards.
     * Preserves quirk: card code input validates legality,
     * but index input does NOT — invalid index causes penalty.
     */
    public int askHuman(ArrayList<String> hand, String upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    // Use Rules directly — ConsoleView should not depend on Main
                    if (Rules.isLegal(new Card(hand.get(i)), new Card(upCard), calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    public boolean askPlayDrawn(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y") ||
                    input.equals("G") || input.equals("B")) {
                return input;
            }
            System.out.println("Bad color.");
        }
    }

    private String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) out += " ";
        }
        return out;
    }
}