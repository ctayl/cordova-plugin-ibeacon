/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.ibeaconbg.www;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import java.util.UUID;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.unarin.cordova.beacon.LocationManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.*;

import java.lang.ref.WeakReference;

import de.appplant.cordova.plugin.localnotification.LocalNotification;

public class MainCordovaActivity extends CordovaActivity
{
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static WeakReference<CordovaWebView> webView = null;
    private static MainCordovaActivity sInstance;

    private static String eventType;
    private static int state;
    private static Region parsedRegion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sInstance = this;

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        Log.w("MainCordovaActivity", String.valueOf(extras));
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        if (extras != null) {
            eventType = extras.getString("eventType");
            state = extras.getInt("state");

            Log.w(TAG, String.valueOf(state));

            String mUniqueId = extras.getString("mUniqueId");
            Identifier uuid = extras.getString("id1") instanceof String ? Identifier.parse(extras.getString("id1")) : null;
            Identifier id2 = extras.getString("id2") instanceof String ? Identifier.parse(extras.getString("id2")) : null;
            Identifier id3 = extras.getString("id3") instanceof String ? Identifier.parse(extras.getString("id3")) : null;

            parsedRegion = new Region(mUniqueId, uuid, id2, id3);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//
//            } else {
//                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            PERMISSION_REQUEST_FINE_LOCATION);
//                }
//                else {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("Functionality limited");
//                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                        }
//
//                    });
//                    builder.show();
//                }
//
//            }
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    public static MainCordovaActivity getInstance() {
        return MainCordovaActivity.sInstance;
    }

    public static void onInit() {
        if (eventType != null && parsedRegion != null) {
            Log.e(TAG, "CALLING ON INIT");
            Main.getInstance().didDetermineStateForRegion(state, parsedRegion);
        }
    }
}
