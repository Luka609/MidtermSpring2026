package persistence;

/**
 * Read model for the "player win count" report.
 */
public record PlayerWins(String playerName, long wins) {
}
