package antnguyen.citiship.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import antnguyen.citiship.Activity.OnShiftActivity;
import antnguyen.citiship.Model.DataEmit;
import antnguyen.citiship.Util.Constants;

import static antnguyen.citiship.Util.Constants.TAG;

public class LocationService extends Service {

    private static final long mPeriod = 1000; //Variable period to post data server
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private Socket mSocket;
    private Timer mTimer;

    public Emitter.Listener onConnect = args -> Log.e(TAG, "connect");
    public Emitter.Listener onDisconnect = args -> Log.e(TAG, "disconnect");
    public Emitter.Listener onConnectError = args -> Log.e(TAG, "Connect error");

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private LocationManager mLocationManager = null;
    private String mLocation = "";

    {
        try {
//            mSocket = IO.socket("https://nihonchannel.com:8081");
            mSocket = IO.socket("http://192.168.1.14:3000");
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand Service!");

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.connect();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!mLocation.equals("")) {
                    Log.e(TAG, "mLocation OKE!");
                    mTimer.cancel();

                    mSocket.emit("send-location", (Object) mLocation.getBytes());
                    mSocket.on("send-location-callback", args -> {
                        String data = Arrays.toString(args);
                        Gson gson = new Gson();
                        DataEmit[] emits = gson.fromJson(data, DataEmit[].class);
                        Log.e(TAG, "call: " + emits[0].getData());
                        Log.e(TAG, "call: " + emits[0].isResult());

                        try {
                            Log.e(TAG, "Wait 3s...");
                            Thread.sleep(3000);
                            mSocket.emit("send-location", (Object) mLocation.getBytes());
                            Log.e(TAG, "Continue send request");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (WebsocketNotConnectedException e){
                            Log.e(TAG, "Server DIE!");
                        }
                    });

                } else {
                    Log.e(TAG, "mLocation.equals()");
                }
            }
        }, 3000, 1000);


        Notification notify;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, OnShiftActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = "Channel Partner";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Create a notification and set the notification channel.
            notify = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("Bạn đang trong ca làm")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .build();
            if (manager != null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Partner", importance));
            }
        } else {
            notify = new Notification.Builder(getBaseContext())
                    .setContentTitle("Bạn đang trong ca làm")
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_notification_overlay, "View", pendingIntent)
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setAutoCancel(true)
                    .build();
        }

        startForeground(10, notify);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate Service");

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
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.close();

        SharedPreferences mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constants.PRE_KEY_ON_SHIFT, false);
        editor.apply();

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
            Log.e(TAG, "Location: " + mLocation);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLocation = location.getLatitude() + " , " + location.getLongitude();
            //Log.e(TAG, "onLocationChanged: " + mLocation);
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