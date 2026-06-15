package persistence;

import java.time.LocalDateTime;

/**
 * Read model for the "recent games" report.
 */
public record RecentGame(Long id, LocalDateTime playedAt, int roundsPlayed, String winnerName) {
}
