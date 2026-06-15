package persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Builds and caches the {@link EntityManagerFactory} for the application.
 * The default factory uses the file-based H2 database under ./data; tests can
 * build an isolated in-memory factory with {@link #createInMemory()}.
 */
public final class EmfProvider {

    private static EntityManagerFactory emf;

    private EmfProvider() {
    }

    public static synchronized EntityManagerFactory get() {
        if (emf == null) {
            try {
                Files.createDirectories(Path.of("data"));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create data directory", e);
            }
            emf = Persistence.createEntityManagerFactory("uno");
        }
        return emf;
    }

    public static EntityManagerFactory createInMemory() {
        Map<String, String> overrides = Map.of(
                "jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:uno_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1",
                "hibernate.hbm2ddl.auto", "create-drop");
        return Persistence.createEntityManagerFactory("uno", overrides);
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
