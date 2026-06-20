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

  /**
   * Returns true if a player should be penalized for not calling UNO.
   * Timing rule: this is checked the moment a player is reduced to a single
   * card by playing; if UNO was not called at that point, the penalty applies.
   */
  public static boolean missedUno(int handSize, boolean calledUno) {
    return handSize == 1 && !calledUno;
  }

  /**
   * Index of the highest-scoring player among the first {@code playerCount}
   * entries. Ties resolve to the lowest index.
   */
  public static int leader(int[] scores, int playerCount) {
    int best = 0;
    for (int i = 1; i < playerCount; i++) {
      if (scores[i] > scores[best]) {
        best = i;
      }
    }
    return best;
  }

  /**
   * True once any of the first {@code playerCount} players has reached or
   * exceeded the target score, ending a multi-round game.
   */
  public static boolean reachedTarget(int[] scores, int playerCount, int target) {
    for (int i = 0; i < playerCount; i++) {
      if (scores[i] >= target) {
        return true;
      }
    }
    return false;
  }
}