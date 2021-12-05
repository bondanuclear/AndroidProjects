package GameState;

import com.example.battleship.Controller;

import java.util.ArrayList;

import GameArchitecture.BattleField;
import GameArchitecture.CellState;
import GameArchitecture.Player;
import GameArchitecture.Point;

public class StateTurnsAI extends GameState {
    Point lastHitPoint;
    Point lastSinkPoint;
    Point missPoint;
    boolean hit;
    boolean won;
    int destroyedCount;

    public StateTurnsAI(Controller controller) {
        super(controller);
    }

    @Override
    public void start() {
        Controller controller = getController();
       BattleField humanField = controller.getField(Player.HUMAN);

        while (decideTurn(humanField, controller)) {
            if (won) {
                controller.setNextState(new StateEndGame(Player.AI, controller));
                controller.startNextState();
                return;
            }
        }
        controller.setNextState(new StateTurnHuman(controller));
        controller.startNextState();
    }

    private Point getRandomKnownShipCell(BattleField humanField, int x, int y) {
        ArrayList<Point> cells = new ArrayList<>(4);
        getKnownShipCells(humanField, x, y, cells);
        return cells.get(getController().getRNG().nextInt(cells.size()));
    }

    private void getKnownShipCells(BattleField humanField, int x, int y, ArrayList<Point> cells) {
        if (x < 0 || x >= humanField.getWidth() || y < 0 || y >= humanField.getHeight())
            return;

        if (humanField.getCell(x, y) == CellState.HIT) {
            Point point = new Point(x, y);
            if (cells.contains(point))
                return;

            cells.add(point);
            getKnownShipCells(humanField, x - 1, y, cells);
            getKnownShipCells(humanField, x + 1, y, cells);
            getKnownShipCells(humanField, x, y - 1, cells);
            getKnownShipCells(humanField, x, y + 1, cells);
        }
    }

    /**
     *
     * @param humanField field for human player
     * @param controller GameController instance
     * @return true if the AI wins or gets next turn, false if player gets next turn.
     */
    private boolean decideTurn(BattleField humanField, Controller controller) {
        int partiallyDamagedShipX = -1;
        int partiallyDamagedShipY = -1;
        searchLoop:
        for (int x = 0; x < controller.getWidth(); x++) {
            for (int y = 0; y < controller.getHeight(); y++) {
                if (humanField.getCellAsOpponent(x, y) == CellState.HIT &&
                        !controller.checkShipDestroyed(Player.HUMAN, x, y, false)) {
                    partiallyDamagedShipX = x;
                    partiallyDamagedShipY = y;
                    break searchLoop;
                }
            }
        }
        //pick random spot around ship
        if (partiallyDamagedShipX != -1) {
            while (true) {
                Point knownDamagedCell = getRandomKnownShipCell(humanField,
                        partiallyDamagedShipX, partiallyDamagedShipY);

                for (int attempt = 0; attempt < 100; attempt++) {
                    Point point = controller.getRandomTouchingPoint(knownDamagedCell);
                    if (humanField.getCellAsOpponent(point) == CellState.EMPTY)
                        return doTurn(humanField, controller, point);
                }
            }
        }
        //pick random spot entirely
        while (true) {
            Point point = controller.makeRandomPoint();
            if (humanField.getCellAsOpponent(point) == CellState.EMPTY)
                return doTurn(humanField, controller, point);
        }
    }

    private boolean doTurn(BattleField humanField, Controller controller, Point point) {
        switch (humanField.getCell(point)) {
            case EMPTY:
                humanField.setCell(point, CellState.MISSED);
                missPoint = point;
                return false;
            case SHIP:
                humanField.setCell(point, CellState.HIT);
                hit = true;
                if (controller.checkShipDestroyed(Player.HUMAN, point.x, point.y, true)) {
                    lastSinkPoint = point;
                    destroyedCount++;
                    won = controller.checkVictory(Player.AI);
                }
                lastHitPoint = point;
                return true;
            default:
                throw new IllegalStateException("AI hit invalid point!");
        }
    }

    public Point getLastHitPoint() {
        return lastHitPoint;
    }

    public Point getLastSinkPoint() {
        return lastSinkPoint;
    }

    public Point getMissPoint() {
        return missPoint;
    }

    public boolean hit() {
        return hit;
    }

    public int getDestroyedShipCount() {
        return destroyedCount;
    }
}
