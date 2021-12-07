package com.example.battleship;


import android.content.Context;
import android.os.Vibrator;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import GameArchitecture.BattleField;
import GameArchitecture.CellState;
import GameArchitecture.Player;
import GameArchitecture.Point;
import GameState.GameState;

public class Controller {
    private static final String LOGGING_TAG = "GameController";

    private MainActivity gameActivity;
    private final Random rng;

    private static final int SIZE = 10;
    private final BattleField humanField;
    private final BattleField aiField;
    private final int[] maxCountForSize = new int[] {0, 4, 3, 2, 1};

    private GameState nextState;
    private GameState state;

    private final ArrayList<View> views = new ArrayList<>();

    /**
     * Class which takes control of 2 battlefields
     * Checks if ship is destroyed
     * Checks if cell has adjacent cells
     * Starts next state
     */
    public Controller() {
        humanField = new BattleField(SIZE, SIZE);
        aiField = new BattleField(SIZE, SIZE);
        rng = new Random();
    }

    public Random getRNG() {
        return rng;
    }

    public void registerView(View view) {
        views.add(view);
    }

    public void setGameActivity(MainActivity activity) {
        this.gameActivity = activity;
    }

    public int getWidth() {
        return SIZE;
    }

    public int getHeight() {
        return SIZE;
    }

    /**
     *
     * @param player Player instance
     * @return field according to enum Player
     */
    public BattleField getField(Player player) {
        switch (player) {
            case HUMAN:
                return humanField;
            case AI:
                return aiField;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getMaxShipCount(int size) {
        if (size < 1)
            throw new IllegalArgumentException();
        if (size > 4)
            return 0;
        return maxCountForSize[size];
    }

    public boolean isTouching(int x1, int y1, int x2, int y2) {
        return (x1 == x2 && Math.abs(y2 - y1) == 1) ||
                (y1 == y2 && Math.abs(x2 - x1) == 1);
    }

    public void redrawUI() {
        for (View view : views)
            view.invalidate();
    }

    public void onTouchCellDown(Player player, int x, int y, int pointerId) {
        state.onTouchCellDown(player, x, y, pointerId);
    }

    public void onTouchCell(Player player, int x, int y, int pointerId) {
        state.onTouchCell(player, x, y, pointerId);
    }

    public void onTouchCellUp(Player player, int x, int y, int pointerId) {
        state.onTouchCellUp(player, x, y, pointerId);
    }

    public void onTouchCellUp(Player player, int pointerId) {
        state.onTouchCellUp(player, pointerId);
    }

    public void setNextState(GameState nextState) {
        this.nextState = nextState;
    }

    public void startNextState() {
        GameState oldState = this.state;
        GameState newState = this.nextState;
        if (gameActivity != null)
            gameActivity.onStateChange(oldState, newState);

        this.state = this.nextState;
        this.nextState = null;
        this.state.start();
    }

    public void vibrateForFailure() {
        if (gameActivity != null) {
            Vibrator vibrator = (Vibrator) gameActivity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(50);
        }
    }

    public boolean checkShipDestroyed(Player player, int x, int y, boolean mark) {
        if (getField(player).getCell(x, y) != CellState.HIT)
            return false;
        return checkShipUndamaged(player, x, y, mark) == null;
    }

    private Point checkShipUndamaged(Player player, int x, int y, boolean mark) {
        BattleField field = getField(player);
        if (field.getCell(x, y) != CellState.HIT)
            throw new IllegalArgumentException("Нет кораблей на " + x + " " + y);
        boolean[][] visited = new boolean[getHeight()][getWidth()];

        Point undamagedTile = checkShipDestroyedRecursive(field, x, y, visited);
        if (undamagedTile == null && mark) {
            visited = new boolean[getHeight()][getWidth()];
            markDestroyedShip(field, x, y, false, visited);
        }
        return undamagedTile;
    }

    private Point checkShipDestroyedRecursive(BattleField field, int x, int y, boolean[][] visited) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
            return null;
        if (visited[y][x])
            return null;

        visited[y][x] = true;
        CellState cell = field.getCell(x, y);
        if (cell == CellState.EMPTY || cell == CellState.MISSED)
            return null;

        if (cell == CellState.SHIP)
            return new Point(x, y);

        Point result = checkShipDestroyedRecursive(field, x - 1, y, visited);
        if (result != null)
            return result;
        result = checkShipDestroyedRecursive(field, x + 1, y, visited);
        if (result != null)
            return result;
        result = checkShipDestroyedRecursive(field, x, y - 1, visited);
        if (result != null)
            return result;
        return checkShipDestroyedRecursive(field, x, y + 1, visited);
    }

    private void markDestroyedShip(BattleField field, int x, int y, boolean justMark, boolean[][] visited) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
            return;
        if (visited[y][x])
            return;

        CellState cell = field.getCell(x, y);
        if (cell == CellState.EMPTY) {
            visited[y][x] = true;
            field.setCell(x, y, CellState.MISSED);
            //Log.d(LOGGING_TAG, "Marked " + x + " " + y + " as missed");
        }

        if (cell == CellState.HIT && !justMark) {
            visited[y][x] = true;

            markDestroyedShip(field, x - 1, y, false, visited);
            markDestroyedShip(field, x + 1, y, false, visited);
            markDestroyedShip(field, x, y - 1, false, visited);
            markDestroyedShip(field, x, y + 1, false, visited);

            markDestroyedShip(field, x - 1, y - 1, true, visited);
            markDestroyedShip(field, x + 1, y - 1, true, visited);
            markDestroyedShip(field, x + 1, y + 1, true, visited);
            markDestroyedShip(field, x - 1, y + 1, true, visited);
        }
    }

    public boolean checkVictory(Player player) {
        BattleField opponentField;
        if (player == Player.AI)
            opponentField = humanField;
        else
            opponentField = aiField;

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (opponentField.getCell(x, y) == CellState.SHIP)
                    return false;
            }
        }
        return true;
    }

    public Point makeRandomPoint() {
        return new Point(rng.nextInt(getWidth()), rng.nextInt(getHeight()));
    }

    public Point getRandomTouchingPoint(Point point) {
        randomLoop:
        while (true) {
            int direction = rng.nextInt(4);
            switch (direction) {
                case 0:
                    if (point.x > 0)
                        return new Point(point.x - 1, point.y);
                    continue randomLoop;
                case 1:
                    if (point.x < getWidth() - 1)
                        return new Point(point.x + 1, point.y);
                    continue randomLoop;
                case 2:
                    if (point.y > 0)
                        return new Point(point.x, point.y - 1);
                    continue randomLoop;
                case 3:
                    if (point.y < getHeight() - 1)
                        return new Point(point.x, point.y + 1);
                    continue randomLoop;
            }
        }
    }

    public boolean isValidField(BattleField field) {
        boolean[][] marked = new boolean[field.getHeight()][field.getWidth()];

        Map<Integer, Integer> shipCounts = new HashMap<>();
        for (int size = 1; size <= 4; size++)
            shipCounts.put(size, 0);

        for (int x = 0; x < field.getWidth(); x++) {
            for (int y = 0; y < field.getHeight(); y++) {
                CellState cell = field.getCell(x, y);

                if (cell == CellState.SHIP || cell == CellState.HIT) {
                    ArrayList<Point> currentShip = new ArrayList<>(4);
                    if (isInvalidShip(field, x, y, currentShip, marked))
                        return false;

                    if (!currentShip.isEmpty()) {
                        if (shipHasInvalidSurroundings(field, currentShip))
                            return false;
                        int shipCount = shipCounts.get(currentShip.size());
                        if (shipCount == getMaxShipCount(currentShip.size()))
                            return false;
                        shipCounts.put(currentShip.size(), shipCount + 1);
                    }
                }
            }
        }
        for (int size = 1; size <= 4; size++) {
            if (shipCounts.get(size) != getMaxShipCount(size))
                return false;
        }
        return true;
    }

    private static boolean isInvalidShip(BattleField field, int x, int y, ArrayList<Point> currentShip,
                                         boolean[][] marked) {
        if (x < 0 || x >= field.getWidth() || y < 0 || y >= field.getHeight())
            return false;

        if (marked[y][x])
            return false;
        marked[y][x] = true;

        CellState cell = field.getCell(x, y);
        if (cell != CellState.SHIP && cell != CellState.HIT)
            return false;

        currentShip.add(new Point(x, y));
        if (currentShip.size() > 4)
            return true;

        if (isInvalidShip(field, x - 1, y, currentShip, marked))
            return true;
        if (isInvalidShip(field, x + 1, y, currentShip, marked))
            return true;
        if (isInvalidShip(field, x, y - 1, currentShip, marked))
            return true;
        if (isInvalidShip(field, x, y + 1, currentShip, marked))
            return true;
        return false;
    }

    private static boolean shipHasInvalidSurroundings(BattleField field, ArrayList<Point> ship) {
        for (Point point : ship) {
            for (int x = Math.max(0, point.x - 1); x <= Math.min(field.getWidth() - 1, point.x + 1); x++) {
                for (int y = Math.max(0, point.y - 1); y <= Math.min(field.getHeight() - 1, point.y + 1); y++) {
                    Point neighbor = new Point(x, y);
                    if (!ship.contains(neighbor) && (field.getCell(neighbor) == CellState.SHIP ||
                            field.getCell(neighbor) == CellState.HIT))
                        return true;
                }
            }
        }
        return false;
    }
}
