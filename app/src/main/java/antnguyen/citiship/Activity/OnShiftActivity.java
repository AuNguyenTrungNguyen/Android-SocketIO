package antnguyen.citiship.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import antnguyen.citiship.R;
import antnguyen.citiship.Service.LocationService;
import antnguyen.citiship.Util.Constants;

public class OnShiftActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mPreferences;

    private Button mBtnOutShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_shift);

        init();

    }

    private void init() {

        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        mBtnOutShift = findViewById(R.id.btn_out_shift);
        mBtnOutShift.setOnClickListener(this);

        LocalBroadcastManager mManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mFilter = new IntentFilter(Constants.INTENT_ACTION_GPS);
        BroadcastReceiver gpsLocationReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Boolean statusGps = intent.getBooleanExtra(Constants.INTENT_EXTRA_GPS, false);
                    if (!statusGps) {
                        finish();
                    }
                }
            }
        };
        mManager.registerReceiver(gpsLocationReceive, mFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_out_shift:
                outShift();
                break;
        }
    }

    private void outShift() {

        stopService(new Intent(this, LocationService.class));
        finish();
    }

    public void minimizeApp(View v) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constants.PRE_KEY_STATUS_NOTIFY, true);
        editor.apply();
    }
}
