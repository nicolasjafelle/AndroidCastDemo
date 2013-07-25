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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.cast.CastDevice;

/**
 * An activity which builds the start screen for the TicTacToe game.
 */
public class MainActivity extends Activity {
    private TextView mConnectedTextView;
    private DeviceSelectionDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mConnectedTextView = (TextView) findViewById(R.id.connected_device);
        setConnectedDeviceTextView(getResources().getString(R.string.no_device));
        setupButtons();
        mDialog = null;
    }

    /** 
     * Dismisses and removes the DeviceSelectionDialog object on application stop. 
     */
    @Override
    protected void onStop() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
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
     * Performs onClick setup on game buttons. 
     */
    private void setupButtons() {
        findViewById(R.id.start).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(true);
            }
        });

        findViewById(R.id.connect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDevice();
            }
        });
    }

    /** 
     * Starts the GameActivity that handles the TicTacToe game. startWithPlayer1 is not used. 
     */
    private void startGame(boolean startWithPlayer1) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }

    /** 
     * Creates a new DeviceSelectionDialog with an attached listener, which listens for device 
     * selection, sets the device in the top-level Application, and enables the game start button. 
     */
    private void selectDevice() {
        mDialog = new DeviceSelectionDialog(this);
        mDialog.setListener(new DeviceSelectionDialog.DeviceSelectionListener() {

            @Override
            public void onSelected(DeviceSelectionDialog dialog) {
                mDialog = null;
                CastDevice device = dialog.selectedDevice();
                if (device != null) {
                    setConnectedDeviceTextView(dialog.selectedDevice().getFriendlyName());
                    TicTacToeApplication.getInstance().setDevice(device);
                    findViewById(R.id.start).setEnabled(true);
                } else {
                    setConnectedDeviceTextView(
                            MainActivity.this.getResources().getString(R.string.no_device));
                    findViewById(R.id.start).setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DeviceSelectionDialog dialog) {
                mDialog = null;
            }
        });
        try {
            mDialog.show();
        } catch (IllegalStateException e) {
            showErrorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    /** 
     * Builds and displays the error dialog when an error is caught. 
     */
    private void showErrorDialog(String errorString) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(errorString)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /** 
     * Updates a label with the passed name of the currently connected device. 
     */
    private void setConnectedDeviceTextView(String deviceName) {
        mConnectedTextView.setText(
                String.format(getResources().getString(R.string.connected_to_text), deviceName));
    }
}
