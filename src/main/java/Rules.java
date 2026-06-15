/**
 * Stateless rule logic for the UNO variant implemented by this project.
 *
 * This class has no dependency on console I/O or game state.
 * All methods are pure functions of their inputs, making them
 * independently testable without running the full CLI game.
 *
 * Quirks preserved from the original implementation:
 * - Wilds are always legal regardless of called color
 * - calledColor overrides the up card color after a wild is played
 * - Number matching uses integer comparison via Card.number()
 */
public class Rules {

  /**
   * Returns true if the given card is legal to play
   * given the current up card and called color.
   */
  public static boolean isLegal(Card card, Card upCard, String calledColor) {
    if (card.isWild()) {
      return true;
    }
    if (card.color().equals(upCard.color())) {
      return true;
    }
    if (!calledColor.isEmpty() && card.color().equals(calledColor)) {
      return true;
    }
    if (card.rank().equals(upCard.rank()) && !card.rank().equals("NUMBER")) {
      return true;
    }
    return card.rank().equals("NUMBER") && upCard.rank().equals("NUMBER")
        && card.number() == upCard.number();
  }

  /**
   * Calculates total points from a collection of cards.
   * Used at game end to score the winner.
   */
  public static int totalPoints(java.util.List<String> hand) {
    int total = 0;
    for (String code : hand) {
      total += new Card(code).points();
    }
    return total;
  }
}