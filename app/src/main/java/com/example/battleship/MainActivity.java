package com.example.battleship;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import GameArchitecture.Player;
import GameArchitecture.Point;
import GameState.CreateHumanField;
import GameState.GameState;
import GameState.StateEndGame;
import GameState.StateTurnHuman;
import GameState.StateTurnsAI;
public class MainActivity extends AppCompatActivity {

    private static final String LOGGING_TAG = "MainActivity";
    private Controller controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final FieldView fieldView = findViewById(R.id.game_field);
        final Button switchButton = findViewById(R.id.switch_perspective);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldView.getShownPlayer() == Player.HUMAN) {
                    focusOnAI();
                } else {
                    focusOnHuman();
                }
            }
        });

        Button restartButton = findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        startNewGame();
    }

    private void startNewGame() {
        FieldView fieldView = findViewById(R.id.game_field);
        controller = new Controller();
        controller.registerView(fieldView);
        controller.setGameActivity(this);
        fieldView.setGameController(controller);

        controller.setNextState(new CreateHumanField(controller));
        controller.startNextState();

        focusOnHuman();
        fieldView.revealOpponent(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void focusOnAI() {
        FieldView gameField = findViewById(R.id.game_field);
        gameField.showPlayer(Player.AI);
        Button switchButton = findViewById(R.id.switch_perspective);
        switchButton.setText(R.string.switch_to_player);
    }

    private void focusOnHuman() {
        FieldView gameField = findViewById(R.id.game_field);
        gameField.showPlayer(Player.HUMAN);
        Button switchButton = findViewById(R.id.switch_perspective);
        switchButton.setText(R.string.switch_to_ai);
    }

    public void onStateChange(GameState oldState, GameState newState) {
        Log.d(LOGGING_TAG, "Old: " + oldState);
        Log.d(LOGGING_TAG, "New: " + newState);

        if (newState instanceof CreateHumanField)
            findViewById(R.id.switch_perspective).setVisibility(View.GONE);
        else
            findViewById(R.id.switch_perspective).setVisibility(View.VISIBLE);

        if (newState instanceof StateEndGame)
            findViewById(R.id.restart).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.restart).setVisibility(View.GONE);


        TextView gameStateLabel = findViewById(R.id.game_state_label);

        if (newState instanceof CreateHumanField) {
            gameStateLabel.setText(R.string.draw_your_field);
        } else if (newState instanceof StateEndGame) {
            FieldView fieldView = findViewById(R.id.game_field);
            fieldView.revealOpponent(true);

            if (((StateEndGame) newState).getWinner() == Player.HUMAN) {
                gameStateLabel.setText(R.string.you_win);
                focusOnAI();
            } else {
                gameStateLabel.setText(R.string.you_lose);
                focusOnHuman();
            }
        } else if (oldState instanceof StateTurnsAI) {
            if (((StateTurnsAI) oldState).getDestroyedShipCount() == 1) {
                Point lastSinkPoint = ((StateTurnsAI) oldState).getLastSinkPoint();
                gameStateLabel.setText(getString(R.string.ai_sunk, lastSinkPoint));

            } else if (((StateTurnsAI) oldState).getDestroyedShipCount() > 1) {
                int destroyedCount = ((StateTurnsAI) oldState).getDestroyedShipCount();
                gameStateLabel.setText(getResources().getQuantityString(R.plurals.ai_sunk_multiple,
                        destroyedCount, destroyedCount));

            } else if (((StateTurnsAI) oldState).hit()) {
                Point lastHitPoint = ((StateTurnsAI) oldState).getLastHitPoint();
                gameStateLabel.setText(getString(R.string.ai_hit, lastHitPoint));

            } else {
                Point missPoint = ((StateTurnsAI) oldState).getMissPoint();
                gameStateLabel.setText(getString(R.string.ai_missed, missPoint));
            }
            focusOnHuman();
        } else if (oldState instanceof StateTurnHuman) {
            if (((StateTurnHuman) oldState).destroyed())
                gameStateLabel.setText(R.string.you_sunk);
            else if (((StateTurnHuman) oldState).hit())
                gameStateLabel.setText(R.string.you_hit);
            else
                gameStateLabel.setText(R.string.you_missed);
        } else if (newState instanceof StateTurnHuman) {
            gameStateLabel.setText(R.string.your_turn);
        }
    }


}