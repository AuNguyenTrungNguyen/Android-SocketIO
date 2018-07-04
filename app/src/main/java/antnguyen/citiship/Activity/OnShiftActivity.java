package antnguyen.citiship.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import antnguyen.citiship.R;
import antnguyen.citiship.Util.Constants;

public class OnShiftActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog mProgress;
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

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Lấy dữ liệu!!!");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_out_shift:
                outShift();
                break;
        }
    }

    private void outShift() {

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constants.PRE_KEY_ON_SHIFT, false);
        editor.apply();

        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        finish();
    }
}
