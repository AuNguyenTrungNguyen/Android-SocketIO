package antnguyen.citiship.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import antnguyen.citiship.Service.LocationService;
import antnguyen.citiship.Util.Constants;

import static antnguyen.citiship.Util.Constants.TAG;

public class StartServiceBroadcast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "StartServiceBroadcast listener!");
        SharedPreferences mPreferences = context.getSharedPreferences(Constants.PRE_NAME, context.MODE_PRIVATE);
        boolean statusNotify = mPreferences.getBoolean(Constants.PRE_KEY_STATUS_NOTIFY, true);
        Log.e(TAG, "statusNotify: " + statusNotify);

        if (intent != null
                && intent.getAction().equals(Constants.INTENT_ACTION_START_SERVICE)
                && statusNotify){
            context.startService(new Intent(context, LocationService.class));
            Log.e(TAG, "Broadcast start service!");
        }
    }
}
