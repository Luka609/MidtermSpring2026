package persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A persisted game session (one run of the program), made up of one or more
 * rounds. Holds the final per-player scores and the overall winner.
 */
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "rounds_played", nullable = false)
    private int roundsPlayed;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Score> scores = new ArrayList<>();

    protected Game() {
    }

    public Game(LocalDateTime playedAt, int roundsPlayed) {
        this.playedAt = playedAt;
        this.roundsPlayed = roundsPlayed;
    }

    public void addRound(Round round) {
        round.setGame(this);
        rounds.add(round);
    }

    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public List<Score> getScores() {
        return scores;
    }
}
