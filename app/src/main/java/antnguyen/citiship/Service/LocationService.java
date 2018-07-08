package antnguyen.citiship.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import antnguyen.citiship.Activity.InfoActivity;
import antnguyen.citiship.Activity.OnShiftActivity;
import antnguyen.citiship.Model.DataEmit;
import antnguyen.citiship.Util.Constants;

import static antnguyen.citiship.Util.Constants.TAG;

public class LocationService extends Service {

    private static final long mPeriod = 3000; //Variable period to post data server
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    public Emitter.Listener onConnect = args -> Log.e(TAG, "connect");
    public Emitter.Listener onDisconnect = args -> Log.e(TAG, "disconnect");
    public Emitter.Listener onConnectError = args -> Log.e(TAG, "Connect error");
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private Socket mSocket;
    private Timer mTimer;
    private LocationManager mLocationManager = null;
    private String mLocation = "";

    private NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Citiship")
            .setContentText("Bạn đang trong ca làm!")
            .setOngoing(true);
    private NotificationManager mNotificationManager;

    {
        try {
            //mSocket = IO.socket("https://hoclamweb.club:8080");
            mSocket = IO.socket("https://nihonchannel.com:8081");
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.connect();

        mTimer = new Timer();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!mLocation.equals("")) {

                    mSocket.emit("send-location", (Object) mLocation.getBytes());

                    mSocket.on("send-location-callback", args -> {
                        String data = Arrays.toString(args);
                        Gson gson = new Gson();
                        DataEmit[] emits = gson.fromJson(data, DataEmit[].class);
                        Log.e(TAG, "call: " + emits[0].getData());
                        Log.e(TAG, "call: " + emits[0].isResult());
                    });
                } else {
                    Log.e(TAG, "mLocation.equals()");
                }
            }
        }, 0, mPeriod);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate Service");

        SharedPreferences mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        boolean statusNotify = mPreferences.getBoolean(Constants.PRE_KEY_STATUS_NOTIFY, false);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(this, OnShiftActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);


        if (statusNotify) {
            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Log.e(TAG, "Not push notify!");
        }


        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);

        SharedPreferences mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);
        boolean statusNotify = mPreferences.getBoolean(Constants.PRE_KEY_STATUS_NOTIFY, false);

        SharedPreferences.Editor editor = mPreferences.edit();

        if (statusNotify) {

            //Send broadcast
            Intent intentStartService = new Intent(Constants.INTENT_ACTION_START_SERVICE);
            sendBroadcast(intentStartService);
            Log.e(TAG, "Service send broadcast!");

        }else{
            editor.putBoolean(Constants.PRE_KEY_ON_SHIFT, false);
            editor.apply();
            Intent intent = new Intent(this, InfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        //Destroy service is out-shift and go to InfoActivity


        Log.e(TAG, "onDestroy Service");
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        mNotificationManager.cancel(0);
    }


    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            mLocation = mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude();
            Log.e(TAG, "Location: " + mLocation);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location.getLatitude() + " , " + location.getLongitude();
            Log.e(TAG, "onLocationChanged: " + mLocation);
            //mLocation = mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

}