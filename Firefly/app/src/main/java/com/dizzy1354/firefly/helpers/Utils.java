package com.dizzy1354.firefly.helpers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by akshat on 11/12/15.
 */
public class Utils {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static String toHexString(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int c = bytes[i] & 0xFF;
            chars[i * 2] = HEX[c >>> 4];
            chars[i * 2 + 1] = HEX[c & 0x0F];
        }
        return new String(chars).toLowerCase();
    }

    public static boolean isZeroed(byte[] bytes) {
        for (byte b : bytes) {
            if (b != 0x00) {
                return false;
            }
        }
        return true;
    }

    // method to start/stop bluetooth silently
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    /*
    returns current location
     */
    public static Location getMyLocation(Context context) {
        if ( Build.VERSION.SDK_INT < 23 || (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            // Get location from GPS if it's available
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // Location wasn't found, check the next most accurate place for the current location
            if (myLocation == null) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                // Finds a provider that matches the criteria
                String provider = lm.getBestProvider(criteria, true);
                // Use the provider to get the last known location
                myLocation = lm.getLastKnownLocation(provider);
            }

            return myLocation;

        } else {
            Toast.makeText(context, "permission for gps denied", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static String getLocalTimefromTimestampInSeconds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        /* date formatter in local timezone */
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(tz);

        /* print your timestamp and double check it's the date you expect */

        String localTime = sdf.format(new Date(timestamp * 1000));
        return localTime;
    }

    public static String getDisplayTimefromTimestampInSeconds(long timestamp) {
        String displayTime = "at " + getLocalTimefromTimestampInSeconds(timestamp);

        // check if within 1 hour, change format accordingly
        long currentTimestamp = System.currentTimeMillis()/1000;
        if (currentTimestamp - timestamp < 60*60 - 30){
            long seconds = (currentTimestamp - timestamp);
            long minutes = 0;
            if (seconds % 60 > 30) {
                minutes = seconds / 60 + 1;
            }
            else {
                minutes = seconds / 60;
            }
            if (minutes > 1) {
                displayTime = Long.toString(minutes) + " minutes ago";
            } else {
                displayTime = Long.toString(seconds) + " seconds ago";
            }
        }
        return displayTime;
    }

}
