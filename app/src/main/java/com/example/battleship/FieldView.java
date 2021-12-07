package com.example.battleship;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import GameArchitecture.BattleField;
import GameArchitecture.CellState;
import GameArchitecture.Player;

/**
 * Class responsible for painting
 */
public class FieldView extends View {
    private static final String LOGGING_TAG = "GameFieldView";

    private Controller controller = null;
    private Player shownPlayer = Player.HUMAN;

    private Paint paintEmpty = new Paint();
    private Paint paintHit = new Paint();
    private Paint paintMissed = new Paint();
    private Paint paintShip = new Paint();

    private boolean revealOpponent = false;

    public FieldView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintEmpty.setColor(context.getResources().getColor(R.color.colorCellEmpty));
        paintHit.setColor(context.getResources().getColor(R.color.colorCellHit));
        paintMissed.setColor(context.getResources().getColor(R.color.colorCellMiss));
        paintShip.setColor(context.getResources().getColor(R.color.colorCellShip));
        if (isInEditMode()) {
            controller = new Controller();
            BattleField field = controller.getField(Player.HUMAN);
            for (int i = 0; i < 10; i++)
                field.setCell(i, i, CellState.SHIP);
        }
    }

    public void setGameController(Controller controller) {
        this.controller = controller;
    }

    public void showPlayer(Player player) {
        this.shownPlayer = player;
        invalidate();
    }

    public Player getShownPlayer() {
        return shownPlayer;
    }

    public void revealOpponent(boolean revealOpponent) {
        this.revealOpponent = revealOpponent;
    }

    private Paint getPaintForCell(CellState cell) {
        switch (cell) {
            case SHIP:
                return paintShip;
            case HIT:
                return paintHit;
            case MISSED:
                return paintMissed;
            default:
                return paintEmpty;
        }
    }

    private int getCellSize() {
        return Math.min(getWidth() / controller.getWidth(), getHeight() / controller.getHeight());
    }

    private int getCellBorderSize() {
        return getCellSize() / 8;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        BattleField field = controller.getField(shownPlayer);
        int cellSize = getCellSize();
        int cellBorderSize = getCellBorderSize();

        for (int x = 0; x < controller.getWidth(); x++) {
            for (int y = 0; y < controller.getHeight(); y++) {
                CellState cell;
                if (shownPlayer == Player.AI && !revealOpponent)
                    cell = field.getCellAsOpponent(x, y);
                else
                    cell = field.getCell(x, y);

                canvas.drawRect(x * cellSize + cellBorderSize, y * cellSize + cellBorderSize,
                        (x + 1) * cellSize - cellBorderSize, (y + 1) * cellSize - cellBorderSize,
                        getPaintForCell(cell));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int historySize = event.getHistorySize();
        int pointerCount = event.getPointerCount();

        for (int h = 0; h < historySize; h++) {
            for (int p = 0; p < pointerCount; p++) {
                onTouchEvent(event.getHistoricalX(p, h), event.getHistoricalY(p, h), event.getAction(), event.getPointerId(p));
            }
        }
        for (int p = 0; p < pointerCount; p++) {
            onTouchEvent(event.getX(p), event.getY(p), event.getAction(), event.getPointerId(p));
        }

        return true;
    }

    private void onTouchEvent(float x, float y, int action, int pointerId) {
        int cellX = (int) (x / getCellSize());
        int cellY = (int) (y / getCellSize());

        if (cellX < 0 || cellX >= controller.getWidth() ||
                cellY < 0 || cellY >= controller.getHeight()) {
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    controller.onTouchCellUp(shownPlayer, pointerId);
            }
            return;
        }

        //Log.d(LOGGING_TAG, "x: " + cellX + " y: " + cellY);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                controller.onTouchCellDown(shownPlayer, cellX, cellY, pointerId);
                return;
            case MotionEvent.ACTION_MOVE:
                controller.onTouchCell(shownPlayer, cellX, cellY, pointerId);
                return;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                controller.onTouchCellUp(shownPlayer, cellX, cellY, pointerId);
                return;
        }
    }
}