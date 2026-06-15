package persistence;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistence-layer tests. Each test runs against a fresh in-memory H2
 * database, so they do not depend on any developer machine state.
 */
class GameRepositoryTest {

    private EntityManagerFactory emf;
    private GameRepository repo;

    @BeforeEach
    void setUp() {
        emf = EmfProvider.createInMemory();
        repo = new GameRepository(emf);
    }

    @AfterEach
    void tearDown() {
        emf.close();
    }

    @Test
    void recordGame_persistsScoresRoundsAndWinner() {
        Long id = repo.recordGame(LocalDateTime.now(), 2,
                List.of("Alice", "Bob"), new int[]{30, 10},
                List.of(new RoundResult(1, "Alice", 20),
                        new RoundResult(2, "Alice", 10)),
                "Alice");

        assertNotNull(id);

        List<RecentGame> recent = repo.recentGames(10);
        assertEquals(1, recent.size());
        assertEquals("Alice", recent.get(0).winnerName());
        assertEquals(2, recent.get(0).roundsPlayed());
    }

    @Test
    void recentGames_areOrderedNewestFirst_andLimited() {
        repo.recordGame(LocalDateTime.of(2026, 1, 1, 10, 0), 1,
                List.of("Alice", "Bob"), new int[]{20, 0},
                List.of(new RoundResult(1, "Alice", 20)), "Alice");
        repo.recordGame(LocalDateTime.of(2026, 6, 1, 10, 0), 1,
                List.of("Alice", "Bob"), new int[]{0, 15},
                List.of(new RoundResult(1, "Bob", 15)), "Bob");

        List<RecentGame> recent = repo.recentGames(1);
        assertEquals(1, recent.size());
        assertEquals("Bob", recent.get(0).winnerName());
    }

    @Test
    void winCounts_aggregateAcrossGames() {
        recordSingleRoundWin("Alice", "Bob", 20);
        recordSingleRoundWin("Bob", "Alice", 15);
        recordSingleRoundWin("Alice", "Bob", 25);

        List<PlayerWins> wins = repo.winCounts();
        assertEquals("Alice", wins.get(0).playerName());
        assertEquals(2, wins.get(0).wins());
        assertEquals("Bob", wins.get(1).playerName());
        assertEquals(1, wins.get(1).wins());
    }

    @Test
    void highestScores_areOrderedDescending() {
        repo.recordGame(LocalDateTime.now(), 1, List.of("Alice", "Bob"),
                new int[]{30, 10}, List.of(new RoundResult(1, "Alice", 30)), "Alice");
        repo.recordGame(LocalDateTime.now(), 1, List.of("Carol", "Dave"),
                new int[]{99, 5}, List.of(new RoundResult(1, "Carol", 99)), "Carol");

        List<HighScore> top = repo.highestScores(3);
        assertEquals(99, top.get(0).points());
        assertEquals("Carol", top.get(0).playerName());
        assertTrue(top.get(0).points() >= top.get(1).points());
    }

    @Test
    void players_areReusedNotDuplicated() {
        recordSingleRoundWin("Alice", "Bob", 20);
        recordSingleRoundWin("Bob", "Alice", 10);

        assertEquals(2, repo.playerCount());
    }

    private void recordSingleRoundWin(String winner, String loser, int points) {
        repo.recordGame(LocalDateTime.now(), 1,
                List.of(winner, loser), new int[]{points, 0},
                List.of(new RoundResult(1, winner, points)), winner);
    }
}
