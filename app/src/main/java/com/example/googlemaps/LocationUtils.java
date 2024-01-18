package com.example.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Handler;

public class LocationUtils {

    private static final int CHECK_INTERVAL = 1000; // 1 second interval for checking location status

    public static void checkLocationStatus(final Context context, final OnLocationEnabledListener listener) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location is already enabled
            listener.onLocationEnabled();
        } else {
            // Location is not enabled, prompt the user to enable it
            listener.onLocationDisabled();
            // Open location settings
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            // Check location status continuously
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        // Location is enabled now
                        listener.onLocationEnabled();
                    } else {
                        // Continue checking location status
                        handler.postDelayed(this, CHECK_INTERVAL);
                    }
                }
            }, CHECK_INTERVAL);
        }
    }

    public interface OnLocationEnabledListener {
        void onLocationEnabled();
        void onLocationDisabled();
    }
}

