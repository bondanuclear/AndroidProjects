package GameState;

import com.example.battleship.Controller;

import GameArchitecture.Player;

public class StateEndGame extends GameState {
    private final Player winner;

    public StateEndGame(Player winner, Controller controller) {
        super(controller);
        this.winner = winner;
    }

    public Player getWinner() {
        return winner;
    }
}
