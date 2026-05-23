import java.util.Random;
import java.util.Scanner;

public class Main {

    static GameState state = new GameState();

    public static void main(String[] args) {
        GameConfig config = GameConfig.parse(args);

        if (config.help) {
            System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
            return;
        }

        GameState state = new GameState();
        Random random = new Random(config.seed);
        ConsoleView view = new ConsoleView(new Scanner(System.in), config.quiet);
        GameRunner runner = new GameRunner(state, view, random, config);

        if (state.playerNames.size() < 2 || state.playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= config.games; g++) {
            view.showGameHeader(g);
            runner.playGame();
        }

        view.showFinalScores(state.playerNames, state.scores);
    }
}