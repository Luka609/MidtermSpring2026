/**
 * Immutable value object representing a single UNO card.
 * <p>
 * Cards are still stored as their original string codes (e.g. "R5", "W4", "BS") to preserve all
 * existing behavior. This class centralizes the parsing that was previously scattered across static
 * methods in Main.
 */
public class Card {

  private final String code;

  public Card(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String color() {
    if (code.startsWith("R")) {
      return "R";
    }
    if (code.startsWith("Y")) {
      return "Y";
    }
    if (code.startsWith("G")) {
      return "G";
    }
    if (code.startsWith("B")) {
      return "B";
    }
    return "";
  }

  public String rank() {
    if (code.equals("W")) {
      return "WILD";
    }
    if (code.equals("W4")) {
      return "WILD_DRAW_FOUR";
    }
    if (code.endsWith("S")) {
      return "SKIP";
    }
    if (code.endsWith("R")) {
      return "REVERSE";
    }
    if (code.endsWith("+2")) {
      return "DRAW_TWO";
    }
    return "NUMBER";
  }

  public int number() {
    if (rank().equals("NUMBER")) {
      return Integer.parseInt(code.substring(1));
    }
    return -1;
  }

  public int points() {
    String r = rank();
    return switch (r) {
      case "NUMBER" -> number();
      case "SKIP", "REVERSE", "DRAW_TWO" -> 20;
      case "WILD", "WILD_DRAW_FOUR" -> 50;
      default -> 0;
    };
  }

  public boolean isWild() {
    return code.startsWith("W");
  }

  @Override
  public String toString() {
    return code;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Card)) {
      return false;
    }
    return code.equals(((Card) obj).code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }
}