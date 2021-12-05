package GameState;

import com.example.battleship.Controller;

import GameArchitecture.Player;

public class GameState {
    private Controller controller;

    public GameState(Controller controller) {
        this.controller = controller;
    }

    public void start() {}

    protected Controller getController() {
        return controller;
    }

    public void onTouchCellDown(Player player, int x, int y, int pointerId) {}

    public void onTouchCell(Player player, int x, int y, int pointerId) {}

    public void onTouchCellUp(Player player, int x, int y, int pointerId) {
        onTouchCellUp(player, pointerId);
    }

    public void onTouchCellUp(Player player, int pointerId) {}
}

