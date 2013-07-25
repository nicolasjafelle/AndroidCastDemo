/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.android.cast.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.android.cast.demo.GameView.State;
import com.android.cast.demo.GameView.ICellListener;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.SessionError;


import java.io.IOException;

/**
 * An activity which both presents a UI on the first screen and casts the TicTacToe game board to 
 * the selected Cast device and its attached second screen.
 */
public class GameActivity extends Activity {
    private static final String TAG = GameActivity.class.getSimpleName();

    private ApplicationSession mSession;
    private SessionListener mSessionListener;
    private TicTacToeStream mGameMessageStream;

    private GameView mGameView;
    private TextView mInfoView;
    private TextView mPlayerNameView;

    /** 
     * Called when the activity is first created. Initializes the game with necessary listeners 
     * for player interaction, and creates a new message stream.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.game);

        mGameView = (GameView) findViewById(R.id.game_view);
        mInfoView = (TextView) findViewById(R.id.info_turn);
        mPlayerNameView = (TextView) findViewById(R.id.player_name);

        mGameView.setFocusable(true);
        mGameView.setFocusableInTouchMode(true);
        mGameView.setCellListener(new CellListener());

        mSessionListener = new SessionListener();
        mGameMessageStream = new TicTacToeStream();
    }

    /**
     * Called on application start. Using the previously selected Cast device, attempts to begin a 
     * session using the application name TicTacToe.
     */
    @Override
    protected void onStart() {
        super.onStart();
        CastDevice selectedDevice = TicTacToeApplication.getInstance().getDevice();
        CastContext castContext =
                TicTacToeApplication.getInstance().getCastContext();

        mSession = new ApplicationSession(castContext, selectedDevice);
        mSession.setListener(mSessionListener);
        try {
            mSession.startSession("TicTacToe");
        } catch (IOException e) {
            Log.e(TAG, "Failed to open a session", e);
        }
    }

    /**
     * Removes the activity from memory when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    /**
     * Attempts to end the current game session when the activity stops.
     */
    @Override
    protected void onStop() {
        if (mSession != null) {
            if (mSession.hasChannel()) {
                mGameMessageStream.leave();
            }
            try {
                if (mSession.hasStarted()) {
                    mSession.endSession();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to end the session.", e);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Unable to end session.", e);
            }
        }
        mSession = null;
        super.onStop();
    }

    /**
     * Returns the screen configuration to portrait mode whenever changed.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Returns the string representation of a State object representing a player, or null if the 
     * passed player does not correspond to an X or O player.
     */

    private String convertGameStateToPlayer(State player) {
        if (player == State.PLAYER_X) {
            return GameMessageStream.PLAYER_X;
        }
        if (player == State.PLAYER_O) {
            return GameMessageStream.PLAYER_O;
        }
        return null;
    }

    /**
     * Builds and displays a dialog indicating the completion of the game, whether by forfeit or 
     * by one player winning.
     */
    private void setFinished(
            State player, int row, int column, int diagonal, boolean wasAbandoned) {
        String text;
        if (wasAbandoned) {
            if (mGameView.getAssignedPlayer() == State.EMPTY) {
                text = getString(R.string.other_players_abandoned);
            } else {
                text = getString(R.string.abandoned);
            }
        } else if (player == State.EMPTY) {
            text = getString(R.string.tie);
        } else {
            text = String.format(getResources().getString(R.string.player_wins),
                    convertGameStateToPlayer(player));
        }
        mGameView.setFinished(row, column, diagonal);

        new AlertDialog.Builder(GameActivity.this)
                .setTitle(R.string.game_over)
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mPlayerNameView.setText(null);
                        mInfoView.setText(R.string.waiting_for_player_assignment);
                        mGameMessageStream.join("MyName");
                    }
                })
                .setNegativeButton(R.string.leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * A class which listens for the selection of a certain cell and attempts to place a mark in 
     * that cell.
     */
    private class CellListener implements ICellListener {
        @Override
        public void onCellSelected(int row, int column) {
            mGameMessageStream.move(row, column);
        }
    }

    /**
     * A class which listens to session start events. On detection, it attaches the game's message
     * stream and joins a player to the game.
     */
    private class SessionListener implements ApplicationSession.Listener {
        @Override
        public void onSessionStarted(ApplicationMetadata appMetadata) {
            mInfoView.setText(R.string.waiting_for_player_assignment);
            ApplicationChannel channel = mSession.getChannel();
            if (channel == null) {
                Log.w(TAG, "onStarted: channel is null");
                return;
            }
            channel.attachMessageStream(mGameMessageStream);
            mGameMessageStream.join("MyName");
        }

        @Override
        public void onSessionStartFailed(SessionError error) {
            Log.d(TAG, "start session failed: " + error.toString());
        }

        @Override
        public void onSessionEnded(SessionError error) {
            Log.d(TAG, "session ended: " + ((error == null) ? "OK" : error.toString()));
        }
    }

    /**
     * An extension of the GameMessageStream specifically for the TicTacToe game.
     */
    private class TicTacToeStream extends GameMessageStream {
        /**
         * Sets displays accordingly when a new player joins the game.
         * 
         * @param playerSymbol either X or O
         * @param opponentName the name of the player who just joined an existing game
         */
        @Override
        protected void onGameJoined(String playerSymbol, String opponentName) {
            State newPlayer = State.EMPTY;
            if (GameMessageStream.PLAYER_X.equals(playerSymbol)) {
                newPlayer = State.PLAYER_X;
            } else if (GameMessageStream.PLAYER_O.equals(playerSymbol)) {
                newPlayer = State.PLAYER_O;
            }

            mGameView.setAssignedPlayer(newPlayer);
            mPlayerNameView.setText(
                    String.format(getResources().getString(R.string.player_name), playerSymbol));
            mInfoView.setText(String.format(
                    getResources().getString(R.string.player_turn), GameMessageStream.PLAYER_X));
        }

        /**
         * Updates the game display upon a move.
         */
        @Override
        protected void onGameMove(String playerSymbol, int row, int column, boolean isGameOver) {
            State player = State.PLAYER_O;
            String otherPlayerName = GameMessageStream.PLAYER_X;
            if (GameMessageStream.PLAYER_X.equals(playerSymbol)) {
                player = State.PLAYER_X;
                otherPlayerName = GameMessageStream.PLAYER_O;
            }

            mGameView.setCell(row, column, player);
            mInfoView.setText(
                    String.format(getResources().getString(R.string.player_turn), otherPlayerName));
        }

        /**
         * At the end of the game, obtains the winning player or whether the game was forfeited, and
         * if a player won, which board position was the winning cell. Passes this information to
         * {@code setFinished()}.
         */
        @Override
        protected void onGameEnd(String endState, int location) {
            State winningPlayer;
            boolean wasGameAbandoned = false;
            if (END_STATE_X_WON.equals(endState)) {
                winningPlayer = State.PLAYER_X;
            } else if (END_STATE_O_WON.equals(endState)) {
                winningPlayer = State.PLAYER_O;
            } else if (END_STATE_ABANDONED.equals(endState)) {
                winningPlayer = mGameView.getAssignedPlayer();
                wasGameAbandoned = true;
            } else {
                winningPlayer = State.EMPTY;
            }

            int winningRow = -1;
            int winningColumn = -1;
            int winningDiagonal = -1;
            if ((location >= WinningLocation.ROW_0.getValue())
                    && (location <= WinningLocation.ROW_2.getValue())) {
                winningRow = location;
            } else if ((location >= WinningLocation.COL_0.getValue())
                    && (location <= WinningLocation.COL_2.getValue())) {
                winningColumn = location - WinningLocation.COL_0.getValue();
            } else if (location == WinningLocation.DIAGONAL_TOPLEFT.getValue()) {
                winningDiagonal = 0;
            } else if (location == WinningLocation.DIAGONAL_BOTTOMLEFT.getValue()) {
                winningDiagonal = 1;
            }

            setFinished(
                    winningPlayer, winningRow, winningColumn, winningDiagonal, wasGameAbandoned);
        }

        /**
         * Updates the game board's layout based on a passed 2-D int array.
         */
        @Override
        protected void onGameBoardLayout(int[][] boardLayout) {
            mGameView.updateBoard(boardLayout);
        }

        /**
         * Clears the game board upon a game error being detected, and displays an error dialog.
         */
        @Override
        protected void onGameError(String errorMessage) {
            if (getResources().getString(R.string.full_game).equals(errorMessage)) {
                mPlayerNameView.setText(R.string.full_game);
                mInfoView.setText(R.string.observing);
                mGameView.clearBoard();
                mGameView.setAssignedPlayer(State.EMPTY);
                mGameMessageStream.requestBoardLayout();
            }

            new AlertDialog.Builder(GameActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }
}
