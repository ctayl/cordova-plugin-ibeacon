package com.unarin.cordova.beacon;

import android.app.Application;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

public class CordovaRegionBootstrap extends Application implements BootstrapNotifier {
    public static final String TAG = "CordovaRegionBootstrap";
    private RegionBootstrap regionBootstrap;
    @Override
    public void onCreate() {
        super.onCreate();

				BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

				Region region = new Region("buildfire",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }
}