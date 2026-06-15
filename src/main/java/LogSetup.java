import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures java.util.logging to write game events to logs/uno.log.
 * Logs are kept off the console so the player-facing CLI stays readable.
 */
public final class LogSetup {

    private static boolean done = false;

    private LogSetup() {
    }

    public static synchronized void init() {
        if (done) {
            return;
        }
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT [%4$s] %5$s%n");

        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }

        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler fileHandler = new FileHandler("logs/uno.log", false);
            fileHandler.setFormatter(new SimpleFormatter());
            root.addHandler(fileHandler);
            root.setLevel(Level.INFO);
        } catch (IOException e) {
            // logging should never stop the game from running
            root.setLevel(Level.OFF);
        }
        done = true;
    }
}
