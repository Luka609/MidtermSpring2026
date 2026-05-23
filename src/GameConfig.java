public class GameConfig {
    public final int bots;
    public final int games;
    public final boolean human;
    public final boolean quiet;
    public final long seed;
    public final boolean help;


    private GameConfig(int bots, int games, boolean human, boolean quiet, long seed, boolean help) {
        this.bots = bots;
        this.games = games;
        this.human = human;
        this.quiet = quiet;
        this.seed = seed;
        this.help = help;
    }

    public static GameConfig parse(String[] args) {
        int bots = 3, games = 1;
        boolean human = false, quiet = false, help = false;
        long seed = 10;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--bots":  bots  = Integer.parseInt(args[++i]); break;
                case "--games": games = Integer.parseInt(args[++i]); break;
                case "--human": human = true; break;
                case "--quiet": quiet = true; break;
                case "--seed":  seed  = Long.parseLong(args[++i]); break;
                case "--help":  help  = true; break;
            }
        }

        return new GameConfig(bots, games, human, quiet, seed, help);
    }
}