package com.unarin.cordova.beacon;

import android.app.Application;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class CordovaRegionBootstrap extends Application {

	public static final String TAG = "CordovaRegionBootstrap";
	private RegionBootstrap regionBootstrap;

	@Override
	public void onCreate() {
		super.onCreate();

		BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

		Region region = new Region("buildfire", null, null, null);
		regionBootstrap = new RegionBootstrap(this, region);
	}

 @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "did enter region.");
        // Send a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "Sending notification.");
        sendNotification();
        if (monitoringActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have
            // seen on its display
            logToDisplay("I see a beacon again" );
        }
    }
}