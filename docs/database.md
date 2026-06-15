# Database & Persistence

The game records every finished session and can report statistics across past
games. This document covers the database, the ORM, the schema, how to run the
persistence tests, and how to view game history.

## Selected database

**H2** (embedded, pure Java).

- Real runs use a **file-based** database at `./data/uno.mv.db`, created
  automatically on first run (the `data/` directory is created if missing).
- Tests use a fresh **in-memory** database (`jdbc:h2:mem:...`) per test.

No database server needs to be installed, and nothing must be configured by
hand. H2 is started in-process by the application.

## Selected ORM / persistence framework

**Hibernate ORM 6 (Jakarta Persistence / JPA)**.

- Entities are annotated POJOs in the `persistence` package:
  `Player`, `Game`, `Round`, `Score`.
- The persistence unit is configured in
  [`src/main/resources/META-INF/persistence.xml`](../src/main/resources/META-INF/persistence.xml).
- All database access lives in [`GameRepository`](../src/main/java/persistence/GameRepository.java).
  Game logic contains no SQL/JPQL.

## Schema

Hibernate generates the schema automatically from the entity mappings
(`hibernate.hbm2ddl.auto=update`), so no manual schema step is required. A
reference copy of the generated tables is in [`schema.sql`](schema.sql).

Entities and relationships:

| Table     | Columns                                                        | Relationships |
|-----------|---------------------------------------------------------------|---------------|
| `players` | `id`, `name` (unique)                                         | referenced by games/rounds/scores |
| `games`   | `id`, `played_at`, `rounds_played`, `winner_id`              | `winner_id` → `players`; has many rounds & scores |
| `rounds`  | `id`, `game_id`, `round_no`, `winner_id`, `points`          | `game_id` → `games`; `winner_id` → `players` |
| `scores`  | `id`, `game_id`, `player_id`, `points`                      | `game_id` → `games`; `player_id` → `players` |

A program run is stored as one **game**; each `--games N` hand is stored as a
**round**. The per-player totals are stored as **scores**, and the player with
the highest total is the game's **winner**. Every game has a **timestamp**
(`played_at`).

## What gets persisted

After each session the application saves:

- player names (reused across games, never duplicated)
- the session timestamp
- the number of rounds played
- each round's winner and points
- each player's final score
- the overall winner

## Running the persistence tests

```bash
mvn test
```

This runs `persistence.GameRepositoryTest`, which exercises saving a game and
all three report queries against an isolated in-memory H2 database. The tests
create and drop their own schema and do not touch the file database or depend on
any machine-specific state.

## Viewing game history and statistics

Play a few games first (each run is recorded), then run a report:

```bash
java -jar target/uno-cli.jar --report recent    # most recent games
java -jar target/uno-cli.jar --report wins       # win count per player
java -jar target/uno-cli.jar --report scores     # highest scores
java -jar target/uno-cli.jar --report all        # all three
```

### Reports with Docker

The database lives inside the container, so mount a named volume to keep history
between runs:

```bash
docker run --rm -v uno-data:/app/data uno-cli --bots 3 --games 2
docker run --rm -v uno-data:/app/data uno-cli --report all
```

## Notes on credentials

The embedded H2 database uses the conventional `sa` user with an empty password.
These are not secrets and there is no external database to secure; no real
credentials are stored in source. There is nothing to preconfigure, the
database file and schema are created automatically.
