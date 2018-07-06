package antnguyen.citiship.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
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
                Log.i(Constants.TAG, "Change provider with GPS: " + isGpsEnabled);

                //If app run background and GPS off
                if (!isGpsEnabled) {
                    context.stopService(new Intent(context, LocationService.class));
                }
            }
        }
    }
}
