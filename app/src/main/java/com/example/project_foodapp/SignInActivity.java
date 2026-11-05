package com.example.project_foodapp;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends BaseActivity {
    private EditText email, password;
    private FirebaseAuth auth;
    @Override protected int getLayoutId() { return R.layout.activity_signin; }
    @Override protected void initViews() { email = findViewById(R.id.txtEmail); password = findViewById(R.id.txtPassword); auth = FirebaseAuth.getInstance(); }
    @Override protected void initEvents() {
        findViewById(R.id.btnSignIn).setOnClickListener(v -> authenticate(false));
        findViewById(R.id.btnSignUp).setOnClickListener(v -> authenticate(true));
        findViewById(R.id.btnForgot).setOnClickListener(v -> {
            String value = email.getText().toString().trim();
            if (value.isEmpty()) { showToast("Nhập email để đặt lại mật khẩu"); return; }
            auth.sendPasswordResetEmail(value).addOnSuccessListener(unused -> showToast("Kiểm tra email đặt lại mật khẩu"))
                .addOnFailureListener(e -> showToast("Không thể gửi email đặt lại mật khẩu"));
        });
        findViewById(R.id.btnSignInFacebook).setOnClickListener(v -> showToast("Đăng nhập Facebook chưa được cấu hình"));
        findViewById(R.id.btnSignInGoogle).setOnClickListener(v -> showToast("Đăng nhập Google chưa được cấu hình"));
    }
    private void authenticate(boolean register) {
        String address = email.getText().toString().trim(), pass = password.getText().toString();
        if (address.isEmpty() || pass.isEmpty()) { showToast("Vui lòng nhập email và mật khẩu"); return; }
        (register ? auth.createUserWithEmailAndPassword(address, pass) : auth.signInWithEmailAndPassword(address, pass))
            .addOnSuccessListener(result -> result.getUser().getIdToken(true).addOnSuccessListener(token -> {
                openActivity(Boolean.TRUE.equals(token.getClaims().get("shipper")) ? DriverActivity.class : MainActivity.class); finish();
            }).addOnFailureListener(e -> showToast("Không thể kiểm tra tài khoản")))
            .addOnFailureListener(e -> showToast("Đăng nhập hoặc đăng ký thất bại: " + e.getLocalizedMessage()));
    }
}
