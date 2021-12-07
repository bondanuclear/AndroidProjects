package GameState;

import com.example.battleship.Controller;

import GameArchitecture.Player;

public class StateEndGame extends GameState {
    private final Player winner;

    /**
     * Method shows the screen of a winner and states who won
     * @param winner - Player class instance
     * @param controller - Controller class instance
     */
    public StateEndGame(Player winner, Controller controller) {
        super(controller);
        this.winner = winner;
    }

    public Player getWinner() {
        return winner;
    }
}
