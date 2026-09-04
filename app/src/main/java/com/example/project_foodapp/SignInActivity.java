package com.example.project_foodapp;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private EditText txtEmail, txtPassword;
    private Button btnForgot, btnSignIn, btnSignUp;
    private TextView btnSignInFacebook, btnSignInGoogle;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_signin;
    }

    @Override
    protected void initViews() {
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnForgot = findViewById(R.id.btnForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignInFacebook = findViewById(R.id.btnSignInFacebook);
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle);
    }

    @Override
    protected void initEvents() {
        btnForgot.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnSignInFacebook.setOnClickListener(this);
        btnSignInGoogle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSignIn) {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Vui lòng nhập Email và Password");
            } else {
                openActivity(MainActivity.class);
                finish();
            }
        } else if (id == R.id.btnSignUp) {
            showToast("Chức năng Đăng ký");
        } else if (id == R.id.btnForgot) {
            showToast("Chức năng Quên mật khẩu");
        } else if (id == R.id.btnSignInFacebook) {
            showToast("Đăng nhập Facebook");
        } else if (id == R.id.btnSignInGoogle) {
            showToast("Đăng nhập Google");
        }
    }
}