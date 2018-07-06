package antnguyen.citiship.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import antnguyen.citiship.Activity.InfoActivity;
import antnguyen.citiship.Util.Constants;

import static antnguyen.citiship.Util.Constants.TAG;

public class LocationService extends Service {

    public Emitter.Listener onConnect = args -> Log.e(TAG, "connect");
    public Emitter.Listener onDisconnect = args -> Log.e(TAG, "disconnect");
    public Emitter.Listener onConnectError = args -> Log.e(TAG, "Connect error");

    private Socket mSocket;
    private Timer mTimer;
    private FusedLocationProviderClient mFusedLocationClient;
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String location = getLocation();

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

        //Destroy service is out-shift and go to InfoActivity
        SharedPreferences mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constants.PRE_KEY_ON_SHIFT, false);
        editor.apply();

        Intent intent = new Intent(this, InfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        Log.i(TAG, "onDestroy Service");
    }

    //Function return current location with format: "latValue, lngValue"
    @SuppressLint("MissingPermission")
    public String getLocation() {

        final String[] locator = {""};

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        locator[0] = location.getLatitude() + ", " + location.getLongitude();
                        Log.i(Constants.TAG, "Location: " + locator[0]);

                    }

                });

        return locator[0];
    }
}
