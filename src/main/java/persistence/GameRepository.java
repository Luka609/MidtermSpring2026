package persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence access for finished games and the reporting queries.
 * All database access goes through here so game logic stays free of SQL/JPQL.
 */
public class GameRepository {

    private final EntityManagerFactory emf;

    public GameRepository() {
        this(EmfProvider.get());
    }

    public GameRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Records a finished game: players (reused if already known), per-player
     * final scores, each round's outcome, and the overall winner.
     *
     * @return the generated game id
     */
    public Long recordGame(LocalDateTime playedAt, int roundsPlayed,
                           List<String> playerNames, int[] totals,
                           List<RoundResult> rounds, String winnerName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Map<String, Player> players = new LinkedHashMap<>();
            for (String name : playerNames) {
                players.put(name, findOrCreatePlayer(em, name));
            }

            Game game = new Game(playedAt, roundsPlayed);
            if (winnerName != null && players.containsKey(winnerName)) {
                game.setWinner(players.get(winnerName));
            }
            for (int i = 0; i < playerNames.size(); i++) {
                game.addScore(new Score(players.get(playerNames.get(i)), totals[i]));
            }
            for (RoundResult round : rounds) {
                Player roundWinner = round.winnerName() == null
                        ? null : players.get(round.winnerName());
                game.addRound(new Round(round.roundNo(), roundWinner, round.points()));
            }
            em.persist(game);

            em.getTransaction().commit();
            return game.getId();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<RecentGame> recentGames(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT g.id, g.playedAt, g.roundsPlayed, w.name "
                            + "FROM Game g LEFT JOIN g.winner w "
                            + "ORDER BY g.playedAt DESC, g.id DESC", Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            List<RecentGame> result = new ArrayList<>();
            for (Object[] row : rows) {
                result.add(new RecentGame((Long) row[0], (LocalDateTime) row[1],
                        (int) row[2], (String) row[3]));
            }
            return result;
        } finally {
            em.close();
        }
    }

    public List<PlayerWins> winCounts() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT w.name, COUNT(g) "
                            + "FROM Game g JOIN g.winner w "
                            + "GROUP BY w.name "
                            + "ORDER BY COUNT(g) DESC, w.name ASC", Object[].class)
                    .getResultList();
            List<PlayerWins> result = new ArrayList<>();
            for (Object[] row : rows) {
                result.add(new PlayerWins((String) row[0], (long) row[1]));
            }
            return result;
        } finally {
            em.close();
        }
    }

    public List<HighScore> highestScores(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT p.name, s.points, g.playedAt "
                            + "FROM Score s JOIN s.player p JOIN s.game g "
                            + "ORDER BY s.points DESC, g.playedAt DESC", Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            List<HighScore> result = new ArrayList<>();
            for (Object[] row : rows) {
                result.add(new HighScore((String) row[0], (int) row[1],
                        (LocalDateTime) row[2]));
            }
            return result;
        } finally {
            em.close();
        }
    }

    public long playerCount() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Player p", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    private Player findOrCreatePlayer(EntityManager em, String name) {
        List<Player> found = em.createQuery(
                        "SELECT p FROM Player p WHERE p.name = :name", Player.class)
                .setParameter("name", name)
                .getResultList();
        if (!found.isEmpty()) {
            return found.get(0);
        }
        Player player = new Player(name);
        em.persist(player);
        return player;
    }
}
