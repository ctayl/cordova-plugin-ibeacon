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

import android.os.Bundle;
import android.util.Log;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.apache.cordova.*;

public class MainCordovaActivity extends CordovaActivity
{
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
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }


        try {
            if (extras != null) {
                eventType = extras.getString("eventType");
                state = extras.getInt("state");

                Log.e(TAG, String.valueOf(state));

                String mUniqueId = extras.getString("mUniqueId") instanceof String ? extras.getString("mUniqueId") : null;
                Identifier uuid = extras.getString("id1") != null ? Identifier.parse(extras.getString("id1")) : null;
                Identifier id2 = extras.getString("id2") != null ? Identifier.parse(extras.getString("id2")) : null;
                Identifier id3 = extras.getString("id3") != null ? Identifier.parse(extras.getString("id3")) : null;

                if (mUniqueId != null) {
                    parsedRegion = new Region(mUniqueId, uuid, id2, id3);
                } else {
                    Log.e(TAG, "Invalid Region! Ignoring.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Set by <content src="index.html" /> in config.xml
            loadUrl(launchUrl);
        }
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
            Main.getInstance().didDetermineStateForRegion(state, parsedRegion);
        }
    }
}
