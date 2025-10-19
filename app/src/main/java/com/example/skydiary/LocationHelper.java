package com.example.skydiary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationHelper {

    public interface LocationCallback {
        void onLocationReceived(String location);
        void onLocationError(String error);
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode);
    }

    @SuppressLint("DefaultLocale")
    public static String formatCoordinates(double latitude, double longitude) {
        // Use simpler format without directions for better accuracy
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    // Remove auto-formatting methods since we want to allow both coordinates and place names
    public static boolean isValidLocationInput(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        // Allow any non-empty input - could be coordinates or place name
        return !input.trim().isEmpty();
    }
}
