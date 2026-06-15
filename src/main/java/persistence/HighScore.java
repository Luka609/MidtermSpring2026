package persistence;

import java.time.LocalDateTime;

/**
 * Read model for the "highest scores" report.
 */
public record HighScore(String playerName, int points, LocalDateTime playedAt) {
}
