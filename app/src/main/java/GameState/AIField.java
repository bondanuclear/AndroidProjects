package GameState;

import com.example.battleship.Controller;

import GameArchitecture.BattleField;
import GameArchitecture.CellState;
import GameArchitecture.Player;
import GameArchitecture.Point;

/**
 * Class that controls AI
 */
public class AIField extends GameState {
    public AIField(Controller controller) {
        super(controller);
    }

    @Override
    public void start() {
        Controller controller = getController();
        BattleField aiField = controller.getField(Player.AI);
        for (int size = 4; size >= 1; size--) {
            for (int j = 0; j < controller.getMaxShipCount(size); j++)
                placeRandomPiece(size, aiField);
        }
        controller.setNextState(new StateTurnHuman(controller));
        controller.startNextState();
    }

    /**
     * Method which
     * @param size - size of the ship
     * @param aiField - atrificial intelligence. Battlefield instance
     */
    public void placeRandomPiece(int size, BattleField aiField) {
        Point[] figurePoints = new Point[size];

        randomLoop:
        while (true) {
            Point lastPoint = getController().makeRandomPoint();
            if (!aiField.isValidCellForShip(lastPoint))
                continue;

            figurePoints[0] = lastPoint;

            for (int i = 1; i < size; i++) {
                Point point = getController().getRandomTouchingPoint(lastPoint);
                for (int j = 0; j < i; j++) {
                    if (figurePoints[j].equals(point))
                        continue randomLoop;
                }
                if (!aiField.isValidCellForShip(point))
                    continue randomLoop;
                figurePoints[i] = point;
            }
            break;
        }
        for (int i = 0; i < size; i++)
            aiField.setCell(figurePoints[i].x, figurePoints[i].y, CellState.SHIP);
    }
}
