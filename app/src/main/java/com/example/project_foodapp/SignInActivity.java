package com.example.project_foodapp;

import android.os.Bundle;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends BaseActivity {
    private EditText email, password;
    private FirebaseAuth auth;
    private boolean busy;

    @Override protected int getLayoutId() { return R.layout.activity_signin; }
    @Override protected void initViews() {
        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPassword);
        auth = FirebaseAuth.getInstance();
    }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) openForUser(current);
    }
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
    private void setBusy(boolean value) {
        busy = value;
        findViewById(R.id.btnSignIn).setEnabled(!value);
        findViewById(R.id.btnSignUp).setEnabled(!value);
    }
    private void authenticate(boolean register) {
        if (busy) return;
        String address = email.getText().toString().trim(), pass = password.getText().toString();
        if (address.isEmpty() || pass.isEmpty()) { showToast("Vui lòng nhập email và mật khẩu"); return; }
        setBusy(true);
        (register ? auth.createUserWithEmailAndPassword(address, pass) : auth.signInWithEmailAndPassword(address, pass))
            .addOnSuccessListener(result -> openForUser(result.getUser()))
            .addOnFailureListener(e -> { setBusy(false); showToast("Đăng nhập hoặc đăng ký thất bại: " + e.getLocalizedMessage()); });
    }
    private void openForUser(FirebaseUser user) {
        if (user == null) { setBusy(false); return; }
        setBusy(true);
        // Firebase reuses a valid cached token; forcing a refresh adds a network round trip.
        user.getIdToken(false).addOnSuccessListener(token -> {
            if (isFinishing() || isDestroyed()) return;
            openActivity(Boolean.TRUE.equals(token.getClaims().get("shipper")) ? DriverActivity.class : MainActivity.class);
            finish();
        }).addOnFailureListener(e -> {
            if (isFinishing() || isDestroyed()) return;
            setBusy(false);
            showToast("Không thể kiểm tra tài khoản. Vui lòng kiểm tra kết nối và thử lại.");
        });
    }
}
