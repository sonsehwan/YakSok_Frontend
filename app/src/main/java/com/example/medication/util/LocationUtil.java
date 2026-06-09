package com.example.medication.util;

import android.location.Location;

import java.util.Locale;

public class LocationUtil {
    public static float calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0];
    }

    public static String formatDistance(float distanceInMeters){
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%dm", (int) distanceInMeters);
        } else {
            double distanceInKm = distanceInMeters / 1000.0;
            return String.format(Locale.getDefault(), "%.1fkm", distanceInKm);
        }
    }
}
