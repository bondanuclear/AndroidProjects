package GameState;

import android.util.Log;

import androidx.collection.ArraySet;

import com.example.battleship.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import GameArchitecture.BattleField;
import GameArchitecture.CellState;
import GameArchitecture.Player;
import GameArchitecture.Point;

public class CreateHumanField extends GameState {
    private static final String LOGGING_TAG = "CreatePlayerField";

    private static final int NOT_DRAWING = Integer.MIN_VALUE;
    private int currentDrawingPointerId = NOT_DRAWING;
    private int lastDrawX = NOT_DRAWING;
    private int lastDrawY = NOT_DRAWING;

    private final Set<Point> currentShip = new ArraySet<>(4);
    private final Map<Integer, Integer> shipCountBySize = new HashMap<>(4);

    public CreateHumanField(Controller controller) {
        super(controller);

        for (int i = 1; i <= 4; i++)
            shipCountBySize.put(i, 0);
    }

    private int getShipCount(int size) {
        if (size < 1)
            throw new IllegalArgumentException();
        if (size > 4)
            return 0;
        return shipCountBySize.get(size);
    }

    private int getLargestAvailableShipSize() {
        int maxAvailableShipSize = 0;
        for (int size = 1; size <= 4; size++) {
            if (getShipCount(size) < getController().getMaxShipCount(size))
                maxAvailableShipSize = size;
        }
        return maxAvailableShipSize;
    }

    private boolean isValidCoordinate(int x, int y) {
        if (x == NOT_DRAWING) {
            if (y == NOT_DRAWING)
                return false;
            throw new IllegalArgumentException("x is invalid but y is valid");
        }
        if (y == NOT_DRAWING)
            throw new IllegalArgumentException("x is valid but y is invalid");
        return true;
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        if (!isValidCoordinate(x1, y1) || !isValidCoordinate(x2, y2))
            return true;
        return Math.abs(x2 - x1) <= 1 && Math.abs(y2 - y1) <= 1;
    }

    private boolean isTouching(int x1, int y1, int x2, int y2) {
        if (!isValidCoordinate(x1, y1) || !isValidCoordinate(x2, y2))
            return true;
        return (x1 == x2 && Math.abs(y2 - y1) == 1) ||
                (y1 == y2 && Math.abs(x2 - x1) == 1);
    }

    private boolean drawPlayerCell(int x, int y) {
        if (isValidCoordinate(lastDrawX, lastDrawY) &&
                !isTouching(x, y, lastDrawX, lastDrawY))
            return false;

        Point point = new Point(x, y);
        if (currentShip.contains(point)) {
            lastDrawX = x;
            lastDrawY = y;
            return false;
        }

        BattleField humanField = getController().getField(Player.HUMAN);
        int size = currentShip.size();
        if (size + 1 <= getLargestAvailableShipSize() &&
                humanField.isValidCellForShip(x, y, currentShip)) {
            humanField.setCell(x, y, CellState.SHIP);
            currentShip.add(point);
            lastDrawX = x;
            lastDrawY = y;

            getController().redrawUI();
            return true;
        } else {
            getController().vibrateForFailure();
            return false;
        }
    }

    @Override
    public void onTouchCellDown(Player player, int x, int y, int pointerId) {
        if (player == Player.HUMAN && currentDrawingPointerId == NOT_DRAWING) {
            BattleField humanField = getController().getField(Player.HUMAN);
            if (humanField.isValidCellForShip(x, y))
                currentDrawingPointerId = pointerId;
            else
                getController().vibrateForFailure();
        }
    }

    @Override
    public void onTouchCell(Player player, int x, int y, int pointerId) {
        if (player == Player.HUMAN && currentDrawingPointerId == pointerId) {
            drawPlayerCell(x, y);
        }
    }

    @Override
    public void onTouchCellUp(Player player, int x, int y, int pointerId) {
        if (player == Player.HUMAN && currentDrawingPointerId == pointerId) {
            drawPlayerCell(x, y);
        }
        onTouchCellUp(player, pointerId);
    }

    @Override
    public void onTouchCellUp(Player player, int pointerId) {
        if (player == Player.HUMAN && currentDrawingPointerId == pointerId) {
            currentDrawingPointerId = NOT_DRAWING;
            lastDrawX = NOT_DRAWING;
            lastDrawY = NOT_DRAWING;

            int shipSize = currentShip.size();

            if (shipSize > 0) {
                if (getShipCount(shipSize) >= getController().getMaxShipCount(shipSize)) {
                    Log.d(LOGGING_TAG, "Cancel ship");
                    //Cancel ship
                    BattleField humanField = getController().getField(Player.HUMAN);
                    for (Point point : currentShip)
                        humanField.setCell(point.x, point.y, CellState.EMPTY);
                    getController().vibrateForFailure();
                    getController().redrawUI();
                } else {
                    Log.d(LOGGING_TAG, "Ship count for size " + shipSize +
                            " is now " + (shipCountBySize.get(shipSize) + 1));
                    shipCountBySize.put(shipSize, shipCountBySize.get(shipSize) + 1);
                }
                currentShip.clear();
            }

            if (getLargestAvailableShipSize() == 0) {
                getController().setNextState(new AIField(getController()));
                getController().startNextState();
            }
        }
    }
}
