package antnguyen.citiship.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import antnguyen.citiship.Service.LocationService;
import antnguyen.citiship.Util.Constants;

public class GpsLocationReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {

                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                SharedPreferences preferences = context.getSharedPreferences(Constants.PRE_NAME, Context.MODE_PRIVATE);
                boolean checkInShift = preferences.getBoolean(Constants.PRE_KEY_ON_SHIFT, false);

                Log.i(Constants.TAG, "changeGPS: " + isGpsEnabled + ", checkInShift: " + checkInShift);

                //SendBroadcast on all status GPS
                LocalBroadcastManager mLocalBroadcastManager;
                mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
                Intent intentBroad = new Intent(Constants.INTENT_ACTION_GPS);
                intentBroad.putExtra(Constants.INTENT_EXTRA_GPS, isGpsEnabled);
                mLocalBroadcastManager.sendBroadcast(intentBroad);

                //If app run background and GPS off
                if (!isGpsEnabled && checkInShift) {
                    context.stopService(new Intent(context, LocationService.class));
                }
            }
        }
    }
}
