package antnguyen.citiship.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import antnguyen.citiship.R;
import antnguyen.citiship.Service.LocationService;
import antnguyen.citiship.Util.Constants;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvName;
    private TextView mTvUsername;
    private Button mBtnLogout;
    private Button mBtnInShift;
    private TextView mTvStatusGps;

    private SharedPreferences mPreferences;

    private IntentFilter mFilter = new IntentFilter(Constants.INTENT_ACTION_GPS);
    private LocalBroadcastManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        init();

        checkOnShift();

        showInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGps();
    }

    private void init() {

        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        mTvName = findViewById(R.id.tv_name);
        mTvUsername = findViewById(R.id.tv_username);
        mBtnLogout = findViewById(R.id.btn_logout);
        mBtnInShift = findViewById(R.id.btn_in_shift);
        mTvStatusGps = findViewById(R.id.tv_status_gps);

        mBtnLogout.setOnClickListener(this);
        mBtnInShift.setOnClickListener(this);
    }

    private void checkGps() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mBtnInShift.setVisibility(View.INVISIBLE);
            mTvStatusGps.setVisibility(View.VISIBLE);
        }else{
            mBtnInShift.setVisibility(View.VISIBLE);
            mTvStatusGps.setVisibility(View.INVISIBLE);
        }
    }

    private void checkOnShift() {

        boolean checkInShift = mPreferences.getBoolean(Constants.PRE_KEY_ON_SHIFT, false);
        mManager = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver gpsLocationReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Boolean statusGps = intent.getBooleanExtra(Constants.INTENT_EXTRA_GPS, false);
                    if (!statusGps) {
                        mBtnInShift.setVisibility(View.INVISIBLE);
                        mTvStatusGps.setVisibility(View.VISIBLE);
                    } else {
                        mBtnInShift.setVisibility(View.VISIBLE);
                        mTvStatusGps.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };
        mManager.registerReceiver(gpsLocationReceive, mFilter);

        if (checkInShift) {
            Intent intent = new Intent(this, OnShiftActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showInfo() {
        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        String name = mPreferences.getString(Constants.PRE_KEY_NAME, "");
        String username = mPreferences.getString(Constants.PRE_KEY_USERNAME, "");

        mTvName.setText(" " + name);
        mTvUsername.setText(username);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout:
                logout();
                break;

            case R.id.btn_in_shift:
                inShift();
                break;
        }
    }

    private void inShift() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constants.PRE_KEY_ON_SHIFT, true);
        editor.putBoolean(Constants.PRE_KEY_STATUS_NOTIFY, false);
        editor.apply();

        startService(new Intent(this, LocationService.class));

        Intent intent = new Intent(this, OnShiftActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(Constants.PRE_KEY_TOKEN, "");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
