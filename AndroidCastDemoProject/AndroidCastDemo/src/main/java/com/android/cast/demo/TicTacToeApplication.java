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

import android.app.Application;

import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.Logger;

/**
 * An application that provides getter and setter methods for passing Cast-related objects between 
 * multiple activities, as well as a way to access its currently running instance.
 */
public class TicTacToeApplication extends Application {
    private static final String TAG = TicTacToeApplication.class.getSimpleName();

    private static Logger sLog = new Logger(TAG);
    private static TicTacToeApplication singleton;
    private CastContext mCastContext;
    private CastDevice mDevice;

    /**
     * Initializes the CastContext associated with this application's context, upon application
     * creation.
     */
    @Override
    public final void onCreate() {
        super.onCreate();
        singleton = this;

        try {
            mCastContext = new CastContext(getApplicationContext());
        } catch (IllegalArgumentException e) {
            sLog.e(e, "Unable to create CastContext");
        }
    }

    /**
     * Returns the instance of this class that is currently running.
     */
    public static TicTacToeApplication getInstance() {
        return singleton;
    }

    /**
     * Returns the CastContext associated with this application's context.
     */
    public CastContext getCastContext() {
        return mCastContext;
    }

    /**
     * Returns the currently selected device, or null if no device is selected.
     */
    public CastDevice getDevice() {
        return mDevice;
    }

    /**
     * Sets the currently selected device.
     */
    public void setDevice(CastDevice device) {
        mDevice = device;
    }
}
