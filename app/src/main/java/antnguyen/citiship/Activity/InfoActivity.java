package antnguyen.citiship.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        init();

        checkOnShift();

        //checkGPS();

        showInfo();
    }

    private void init() {

        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        mTvName = findViewById(R.id.tv_name);
        mTvUsername = findViewById(R.id.tv_username);
        mBtnLogout = findViewById(R.id.btn_logout);
        mBtnInShift = findViewById(R.id.btn_in_shift);

        mBtnLogout.setOnClickListener(this);
        mBtnInShift.setOnClickListener(this);
    }

    private void checkOnShift() {
        boolean checkInShift = mPreferences.getBoolean(Constants.PRE_KEY_ON_SHIFT, false);

        if (checkInShift) {
            Intent intent = new Intent(this, OnShiftActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkGPS(){
        boolean status = mPreferences.getBoolean(Constants.PRE_KEY_STATUS_GPS, true);
        if (!status){
            mBtnInShift.setVisibility(View.INVISIBLE);
        }else{
            mBtnInShift.setVisibility(View.VISIBLE);
        }
    }

    private void showInfo() {
        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        String name = mPreferences.getString(Constants.PRE_KEY_NAME, "");
        String username = mPreferences.getString(Constants.PRE_KEY_USERNAME, "");

        mTvName.setText(" "+name);
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
