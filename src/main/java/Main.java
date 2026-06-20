import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import persistence.EmfProvider;
import persistence.GameRepository;
import persistence.HighScore;
import persistence.PlayerWins;
import persistence.RecentGame;
import persistence.RoundResult;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        LogSetup.init();
        GameConfig config = GameConfig.parse(args);

        if (config.help) {
            System.out.println("Usage: java -jar uno-cli.jar [--bots N] [--games N] [--target N] "
                    + "[--human] [--quiet] [--seed N] [--report recent|wins|scores|all]");
            return;
        }

        if (config.report != null) {
            runReport(config.report);
            return;
        }

        GameState state = new GameState();
        Random random = new Random(config.seed);
        ConsoleView view = new ConsoleView(new Scanner(System.in), config.quiet);
        GameRunner runner = new GameRunner(state, view, random, config);
        int playerCount = config.bots + (config.human ? 1 : 0);
        if (playerCount < 2 || playerCount > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        LOG.info("UNO session starting: games=" + config.games + ", target=" + config.target
                + ", bots=" + config.bots + ", human=" + config.human
                + ", seed=" + config.seed);

        List<RoundResult> rounds = playSession(config, state, view, runner);

        view.showFinalScores(state.playerNames, state.scores);
        if (config.target > 0) {
            view.showOverallWinner(state.playerNames.get(
                    Rules.leader(state.scores, state.playerNames.size())));
        }
        LOG.info("UNO session finished");

        persistSession(state, rounds);
    }

    private static List<RoundResult> playSession(GameConfig config, GameState state,
                                                 ConsoleView view, GameRunner runner) {
        List<RoundResult> rounds = new ArrayList<>();
        int round = 0;
        while (true) {
            round++;
            view.showGameHeader(round);
            RoundOutcome outcome = runner.playGame();
            if (outcome != null) {
                rounds.add(new RoundResult(round, outcome.winnerName(), outcome.points()));
            }
            if (sessionOver(config, state, round)) {
                return rounds;
            }
        }
    }

    private static boolean sessionOver(GameConfig config, GameState state, int round) {
        if (config.target > 0) {
            return Rules.reachedTarget(state.scores, state.playerNames.size(), config.target)
                    || round >= 1000;
        }
        return round >= config.games;
    }

    private static void persistSession(GameState state, List<RoundResult> rounds) {
        try {
            GameRepository repo = new GameRepository();
            Long id = repo.recordGame(LocalDateTime.now(), rounds.size(),
                    state.playerNames, state.scores, rounds, winnerName(state));
            LOG.info("Persisted game session id=" + id);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Could not persist game session", e);
        } finally {
            EmfProvider.close();
        }
    }

    private static String winnerName(GameState state) {
        if (state.playerNames.isEmpty()) {
            return null;
        }
        return state.playerNames.get(Rules.leader(state.scores, state.playerNames.size()));
    }

    private static void runReport(String type) {
        GameRepository repo = new GameRepository();
        try {
            switch (type) {
                case "recent" -> printRecent(repo);
                case "wins" -> printWins(repo);
                case "scores" -> printScores(repo);
                case "all" -> {
                    printRecent(repo);
                    System.out.println();
                    printWins(repo);
                    System.out.println();
                    printScores(repo);
                }
                default -> System.out.println("Unknown report '" + type
                        + "'. Use: recent | wins | scores | all");
            }
        } finally {
            EmfProvider.close();
        }
    }

    private static void printRecent(GameRepository repo) {
        List<RecentGame> games = repo.recentGames(10);
        System.out.println("Recent games:");
        if (games.isEmpty()) {
            System.out.println("  (none yet)");
            return;
        }
        for (RecentGame g : games) {
            System.out.printf("  #%-4d %s  rounds=%d  winner=%s%n",
                    g.id(), g.playedAt().format(STAMP), g.roundsPlayed(),
                    g.winnerName() == null ? "-" : g.winnerName());
        }
    }

    private static void printWins(GameRepository repo) {
        List<PlayerWins> wins = repo.winCounts();
        System.out.println("Player win counts:");
        if (wins.isEmpty()) {
            System.out.println("  (none yet)");
            return;
        }
        for (PlayerWins w : wins) {
            System.out.printf("  %-10s %d%n", w.playerName(), w.wins());
        }
    }

    private static void printScores(GameRepository repo) {
        List<HighScore> scores = repo.highestScores(10);
        System.out.println("Highest scores:");
        if (scores.isEmpty()) {
            System.out.println("  (none yet)");
            return;
        }
        for (HighScore s : scores) {
            System.out.printf("  %-5d %-10s %s%n",
                    s.points(), s.playerName(), s.playedAt().format(STAMP));
        }
    }
}
