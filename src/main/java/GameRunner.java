import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Orchestrates a full UNO game session.
 * GameRunner is responsible for:
 * - dealing cards
 * - running the turn loop
 * - applying card effects
 * - handling win conditions
 * It has no dependency on console I/O directly.
 * All output goes through ConsoleView.
 */
public class GameRunner {

    private static final Logger LOG = Logger.getLogger(GameRunner.class.getName());

    private final GameState state;
    private final ConsoleView view;
    private final Random random;
    private final GameConfig config;

    public GameRunner(GameState state, ConsoleView view, Random random, GameConfig config) {
        this.state = state;
        this.view = view;
        this.random = random;
        this.config = config;
    }

    public void playGame() {
        setupPlayers(config.bots, config.human);
        state.deck.clear();
        state.deck.addAll(DeckBuilder.buildStandardDeck());
        Collections.shuffle(state.deck, random);
        state.discard.clear();
        dealCards();

        state.upCard = state.draw(random);
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = state.draw(random);
        }

        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = random.nextInt(state.playerNames.size());

        LOG.info("Game started with " + state.playerNames.size() + " players "
                + state.playerNames + "; first up card " + state.upCard
                + ", " + state.playerNames.get(state.currentPlayer) + " to start");

        runTurnLoop();
    }

    private void dealCards() {
        for (ArrayList<String> h : state.hands) {
            h.clear();
        }
        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                state.hands.get(i).add(state.draw(random));
            }
        }
    }

    private void runTurnLoop() {
        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = state.playerNames.get(state.currentPlayer);
            ArrayList<String> hand = state.hands.get(state.currentPlayer);

            view.showTurnInfo(state.upCard, state.calledColor, name, hand);
            LOG.info(name + "'s turn (" + hand.size() + " cards, up card " + state.upCard
                    + (state.calledColor.isEmpty() ? "" : " called " + state.calledColor) + ")");

            int chosen = pickCard(hand);
            chosen = handleDrawPhase(chosen, hand, name);

            if (chosen >= 0) {
                boolean gameOver = handlePlayPhase(chosen, hand, name);
                if (gameOver) return;
            } else {
                state.next();
            }
        }
        view.showSafetyLimit();
        LOG.warning("Game stopped at safety limit");
    }

    private int pickCard(ArrayList<String> hand) {
        if (state.humanPlayers.get(state.currentPlayer)) {
            return view.askHuman(hand, state.upCard, state.calledColor);
        }
        return BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
    }

    private int handleDrawPhase(int chosen, ArrayList<String> hand, String name) {
        if (chosen != -1) return chosen;

        Card drawn = new Card(state.draw(random));
        hand.add(drawn.getCode());
        view.showDraw(name, drawn.getCode());
        LOG.info(name + " drew " + drawn.getCode());

        if (Rules.isLegal(drawn, new Card(state.upCard), state.calledColor)) {
            if (!state.humanPlayers.get(state.currentPlayer)) {
                return hand.size() - 1;
            }
            if (view.askPlayDrawn(drawn.getCode())) {
                return hand.size() - 1;
            }
        }
        return -1;
    }

    private boolean handlePlayPhase(int chosen, ArrayList<String> hand, String name) {
        if (chosen >= hand.size()) {
            view.showPenalty(name, "selected an invalid index and draws a penalty card.");
            LOG.warning(name + " gave invalid input (index " + chosen + "); penalty draw");
            hand.add(state.draw(random));
            state.next();
            return false;
        }

        Card card = new Card(hand.get(chosen));

        if (!Rules.isLegal(card, new Card(state.upCard), state.calledColor)) {
            view.showPenalty(name, "tried illegal card " + card.getCode() + " and draws a penalty card.");
            LOG.warning(name + " gave invalid input (illegal card " + card.getCode() + "); penalty draw");
            hand.add(state.draw(random));
            state.next();
            return false;
        }

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card.getCode();
        state.calledColor = "";
        view.showPlay(name, card.getCode());
        LOG.info(name + " played " + card.getCode());

        if (card.isWild()) {
            state.calledColor = state.humanPlayers.get(state.currentPlayer)
                    ? view.askColor()
                    : BotStrategy.chooseColor(hand);
            view.showCalledColor(name, state.calledColor);
            LOG.info(name + " called color " + state.calledColor);
        }

        if (hand.size() == 1) {
            view.showUno(name);
        }

        if (hand.isEmpty()) {
            handleWin(name);
            return true;
        }

        applyCardEffect(card);
        return false;
    }

    private void handleWin(String name) {
        int points = 0;
        for (int i = 0; i < state.hands.size(); i++) {
            if (i != state.currentPlayer) {
                points += Rules.totalPoints(state.hands.get(i));
            }
        }
        state.scores[state.currentPlayer] += points;
        view.showWin(name, points);
        LOG.info("Round over: " + name + " won and scored " + points + " points");
    }

    private void applyCardEffect(Card card) {
        switch (card.rank()) {
            case "SKIP" -> {
                state.next();
                state.next();
            }
            case "REVERSE" -> {
                state.direction *= -1;
                if (state.playerNames.size() == 2) {
                    state.next();
                    state.next();
                } else {
                    state.next();
                }
            }
            case "DRAW_TWO" -> {
                state.next();
                state.hands.get(state.currentPlayer).add(state.draw(random));
                state.hands.get(state.currentPlayer).add(state.draw(random));
                view.showDrawTwo(state.playerNames.get(state.currentPlayer));
                LOG.info(state.playerNames.get(state.currentPlayer) + " drew 2 cards (Draw Two)");
                state.next();
            }
            case "WILD_DRAW_FOUR" -> {
                state.next();
                for (int i = 0; i < 4; i++) {
                    state.hands.get(state.currentPlayer).add(state.draw(random));
                }
                view.showDrawFour(state.playerNames.get(state.currentPlayer));
                LOG.info(state.playerNames.get(state.currentPlayer) + " drew 4 cards (Wild Draw Four)");
                state.next();
            }
            default -> state.next();
        }
    }

    private void setupPlayers(int bots, boolean human) {
        state.playerNames.clear();
        state.humanPlayers.clear();
        state.hands.clear();
        if (human) {
            state.playerNames.add("You");
            state.humanPlayers.add(Boolean.TRUE);
            state.hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            state.playerNames.add("Bot" + i);
            state.humanPlayers.add(Boolean.FALSE);
            state.hands.add(new ArrayList<>());
        }
    }
}
