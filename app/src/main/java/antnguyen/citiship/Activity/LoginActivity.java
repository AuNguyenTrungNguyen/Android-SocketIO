package antnguyen.citiship.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import antnguyen.citiship.Model.LoginModel;
import antnguyen.citiship.R;
import antnguyen.citiship.Service.CitishipApi;
import antnguyen.citiship.Util.Constants;
import antnguyen.citiship.Util.RetrofitConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEdtUsername;
    private EditText mEdtPassword;
    private Button mBtnLogin;
    private ProgressDialog mProgress;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        checkLogin();
    }

    private void init() {

        mPreferences = this.getSharedPreferences(Constants.PRE_NAME, MODE_PRIVATE);

        mEdtUsername = findViewById(R.id.edt_username);
        mEdtPassword = findViewById(R.id.edt_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(this);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Đang xác thực tài khoản");
    }

    private void checkLogin() {

        String token = mPreferences.getString(Constants.PRE_KEY_TOKEN, "");

        if (!TextUtils.equals(token, "")) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void sendLogin() {

        mProgress.show();

        String username = mEdtUsername.getText().toString();
        String password = mEdtPassword.getText().toString();

        if (TextUtils.equals(username, "") || TextUtils.equals(password, "")) {
            showError("Tài khoản hoặc mật khẩu rỗng!");
            mProgress.dismiss();
        } else {
            CitishipApi cityShipApi = RetrofitConfig.getRetrofitInstance().create(CitishipApi.class);
            Call<LoginModel> call = cityShipApi.sendLogin(username, password);
            call.enqueue(new Callback<LoginModel>() {
                @Override
                public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                    LoginModel loginModel = response.body();
                    if (response.isSuccessful()) {

                        if (loginModel != null && loginModel.getResult()) {
                            handleDataLogin(loginModel);
                        } else {
                            showError(loginModel.getMessage());
                            mProgress.dismiss();
                        }
                    } else {
                        showError("Có lỗi khi đăng nhập!");
                        mProgress.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<LoginModel> call, Throwable t) {
                    showError("Có lỗi khi đăng nhập!");
                    mProgress.dismiss();
                }
            });
        }
    }

    private void handleDataLogin(LoginModel loginModel) {
        SharedPreferences.Editor editor = mPreferences.edit();

        String token = loginModel.getData().getToken();
        String name = loginModel.getData().getName();
        String username = loginModel.getData().getUsername();

        editor.putString(Constants.PRE_KEY_TOKEN, token);
        editor.putString(Constants.PRE_KEY_NAME, name);
        editor.putString(Constants.PRE_KEY_USERNAME, username);
        editor.apply();
        mProgress.dismiss();

        Toast.makeText(this, loginModel.getMessage(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                sendLogin();
                break;
        }
    }
}
