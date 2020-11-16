package com.ibeaconbg.www;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.unarin.cordova.beacon.LocationManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.apache.cordova.CallbackContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private static Main sInstance = null;
    private CallbackContext _callbackContext;

    public static CallbackContext getCallbackContext() {
        return Main.getInstance()._callbackContext;
    }

    public static Main getInstance() {
        return Main.sInstance;
    } 

    public void setCallbackContext(CallbackContext callbackContext) {
        _callbackContext = callbackContext;
    }


    Set<String> regions;
    SharedPreferences sharedpreferences;


    private BeaconManager beaconManager;

    public void onCreate() {
        super.onCreate();

        sharedpreferences = getSharedPreferences("iBeacon_preferences", 0);
        sInstance = this;

        setScanningPreferences();
        reinitialize();

        // This reduces bluetooth power usage by about 60%
				// backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    private void setScanningPreferences() {
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setDebug(true);

        Notification.Builder builder = new Notification.Builder(this);
        Context context = getApplicationContext();
        String packageName = context.getPackageName();

        try {
            PackageManager manager = getPackageManager();
            Resources resources = manager.getResourcesForApplication(packageName);
            int resId = resources.getIdentifier("icon", "drawable", packageName);

            Drawable myDrawable = resources.getDrawable(resId);

            builder.setSmallIcon(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MainCordovaActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
    }

    private void reinitialize() {

        List<Region> regionList = new ArrayList<Region>();

        regions = sharedpreferences.getStringSet("regions", new HashSet<String>());

        Iterator value = regions.iterator();
        while (value.hasNext()) {
            String array1[]= value.next().toString().split(",");

            int index = 0;

            String uniqueIdentifier = "";
            Identifier identifier1 = null;
            Identifier identifier2 = null;
            Identifier identifier3 = null;

            for (String identifier: array1) {
                Log.w(TAG, identifier);
                switch (index) {
                    case 0: {
                        uniqueIdentifier = identifier;
                        break;
                    }
                    case 1: {
                        identifier1 = !identifier.equals("NULL") ? Identifier.parse(identifier) : null;
                        break;
                    }
                    case 2: {
                        identifier2 = !identifier.equals("NULL") ? Identifier.parse(identifier) : null;
                        break;
                    }
                    case 3: {
                        identifier3 = !identifier.equals("NULL") ? Identifier.parse(identifier) : null;
                        break;
                    }
                }
                index++;
            }
            Region newRegion = new Region(uniqueIdentifier + "_" + identifier1, identifier1, identifier2, identifier3);
            regionList.add(newRegion);
        }

        regionBootstrap = new RegionBootstrap(this, regionList);
    }

    public String parseRegion(Region region) {
        String entry = "";
        entry += (region.getUniqueId() + ",");
        entry += ((region.getId1() != null ? region.getId1().toString() : "NULL") + ",");
        entry += ((region.getId2() != null ? region.getId2().toString() : "NULL") + ",");
        entry += (region.getId3() != null ? region.getId3().toString() : "NULL");
        entry += "";

        return entry;
    }


    public void startMonitoringForRegion(Region region) {
        Log.e(TAG, String.valueOf(region));

        String entry = parseRegion(region);

        SharedPreferences.Editor editor = sharedpreferences.edit();

        regions = sharedpreferences.getStringSet("regions", new HashSet<String>());
        if (!regions.contains(entry)) {
            Log.e(TAG, "cached new region!");
            regions.add(entry);
        } else {
            Log.e(TAG, "region already cached!");
        }

        editor.clear();
        editor.putStringSet("regions", regions);
        editor.commit();

        if (regionBootstrap != null) {
            regionBootstrap.addRegion(region);
        } else {
            regionBootstrap = new RegionBootstrap(this, region);
        }
    }

    public void stopMonitoringForRegion(Region region) {

        SharedPreferences.Editor editor = sharedpreferences.edit();

        String entry = parseRegion(region);

        regions = sharedpreferences.getStringSet("regions", new HashSet<String>());
        if (regions.contains(entry)) {
            Log.e(TAG, "removed region!");
            regions.remove(entry);
        }

        editor.clear();
        editor.putStringSet("regions", regions);
        editor.commit();

        if (regionBootstrap != null) {
            regionBootstrap.removeRegion(region);
        }
    }

    @Override
    public void didEnterRegion(Region region) {

        MainCordovaActivity mainCordovaActivity =  MainCordovaActivity.getInstance();

        if (mainCordovaActivity != null && _callbackContext != null) {
            LocationManager.getInstance().dispatchMonitorState("didEnterRegion", 1, region, _callbackContext);
        } else {
            sendNotification("didEnterRegion", 1, region);
        }

    }

    @Override
    public void didExitRegion(Region region) {

        MainCordovaActivity mainCordovaActivity =  MainCordovaActivity.getInstance();

        if (mainCordovaActivity != null && _callbackContext != null) {
            LocationManager.getInstance().dispatchMonitorState("didExitRegion", 0, region, _callbackContext);
        }
    }


    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.w(TAG, "Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));

        MainCordovaActivity mainCordovaActivity =  MainCordovaActivity.getInstance();

        if (mainCordovaActivity != null && _callbackContext != null) {
            LocationManager.getInstance().dispatchMonitorState("didDetermineStateForRegion", state, region, _callbackContext);
        }
    }


    private void sendNotification(String eventType, int state, Region region) {

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Beacon Reference Notifications",
                    "Beacon Reference Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channel.getId());
        } else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        Context context = getApplicationContext();
        String pkgName  = context.getPackageName();

        Intent intent = context
                .getPackageManager()
                .getLaunchIntentForPackage(pkgName);

        intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_SINGLE_TOP);

        Log.w(TAG, String.valueOf(region));
        intent.putExtra("eventType", eventType);
        intent.putExtra("state", state);
        intent.putExtra("mUniqueId", region.getUniqueId());
        intent.putExtra("id1", region.getId1().toString());
        intent.putExtra("id2", region.getId2());
        intent.putExtra("id3", region.getId3());

        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        try {
            PackageManager manager = getPackageManager();
            Resources resources = manager.getResourcesForApplication(pkgName);
            int resId = resources.getIdentifier("icon", "drawable", pkgName);

            builder.setSmallIcon(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setContentTitle("I detect a beacon");
        builder.setContentText("Tap here to see details in the reference app");
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build());
    }
}
