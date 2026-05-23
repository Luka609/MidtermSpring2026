import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
            hand.add(state.draw(random));
            state.next();
            return false;
        }

        Card card = new Card(hand.get(chosen));

        if (!Rules.isLegal(card, new Card(state.upCard), state.calledColor)) {
            view.showPenalty(name, "tried illegal card " + card.getCode() + " and draws a penalty card.");
            hand.add(state.draw(random));
            state.next();
            return false;
        }

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card.getCode();
        state.calledColor = "";
        view.showPlay(name, card.getCode());

        if (card.isWild()) {
            state.calledColor = state.humanPlayers.get(state.currentPlayer)
                    ? view.askColor()
                    : BotStrategy.chooseColor(hand);
            view.showCalledColor(name, state.calledColor);
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
                state.next();
            }
            case "WILD_DRAW_FOUR" -> {
                state.next();
                for (int i = 0; i < 4; i++) {
                    state.hands.get(state.currentPlayer).add(state.draw(random));
                }
                view.showDrawFour(state.playerNames.get(state.currentPlayer));
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