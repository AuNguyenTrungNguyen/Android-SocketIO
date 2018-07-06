package antnguyen.citiship.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import antnguyen.citiship.Util.Constants;

import static antnguyen.citiship.Util.Constants.TAG;

public class LocationService extends Service implements LocationListener {

    public Emitter.Listener onConnect = args -> Log.e(TAG, "connect");
    public Emitter.Listener onDisconnect = args -> Log.e(TAG, "disconnect");
    public Emitter.Listener onConnectError = args -> Log.e(TAG, "Connect error");

    private Socket mSocket;
    private Timer mTimer;
    private static final long mPeriod = 3000; //Variable period to post data server

    {
        try {
            mSocket = IO.socket("https://hoclamweb.club:8080");
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
                String location = getLocation();

                Log.i(Constants.TAG, "Location: " + location);

                if (!location.equals("")) {

                    mSocket.emit("send-location", (Object) location.getBytes());

                    mSocket.on("send-location-callback", args -> {
                        String data = Arrays.toString(args);
                        Log.e(TAG, "call: " + data);
                    });
                }
            }
        }, 0, mPeriod);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate Service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        Log.i(TAG, "onDestroy Service");
    }

    //Function return current location with format: "latValue, lngValue"
    @SuppressLint("MissingPermission")
    public String getLocation() {

        final String[] locator = {""};

        try {

            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            if (locationManager != null) {

                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (isGPSEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            this,
                            Looper.getMainLooper());

                    Location myLocation = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (myLocation != null) {
                        locator[0] = myLocation.getLatitude() + ", " + myLocation.getLongitude();
                    }
                }
            }

        } catch (Exception e) {
            Log.i(Constants.TAG, e.getMessage());
        }

        return locator[0];
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
