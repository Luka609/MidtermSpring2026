package persistence;

/**
 * Plain carrier describing the outcome of one round, passed to the repository
 * when recording a finished game.
 */
public record RoundResult(int roundNo, String winnerName, int points) {
}
