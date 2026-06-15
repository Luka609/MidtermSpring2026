package persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single round (one hand) within a game, with the player who went out and
 * the points they scored that round.
 */
@Entity
@Table(name = "rounds")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;

    @Column(nullable = false)
    private int points;

    protected Round() {
    }

    public Round(int roundNo, Player winner, int points) {
        this.roundNo = roundNo;
        this.winner = winner;
        this.points = points;
    }

    void setGame(Game game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public Player getWinner() {
        return winner;
    }

    public int getPoints() {
        return points;
    }
}
