package com.example.quanbadulichmietvuon.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.AdminActivity;
import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.LocaleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private TextView txtSignup;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Kết nối các view từ layout
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginButton);
        txtSignup = findViewById(R.id.txtsignup);
        LinearLayout emailLayout = findViewById(R.id.emailLayout);
        LinearLayout passwordLayout = findViewById(R.id.passwordLayout);

        // Tạo đường viền cho trường nhập Email
        GradientDrawable emailDrawable = new GradientDrawable();
        emailDrawable.setShape(GradientDrawable.RECTANGLE);
        emailDrawable.setColor(Color.TRANSPARENT); // Màu nền trong suốt
        emailDrawable.setStroke(2, Color.parseColor("#800080")); // Màu viền tím
        emailDrawable.setCornerRadius(8); // Đường viền góc tròn

        // Tạo đường viền cho trường nhập Password
        GradientDrawable passwordDrawable = new GradientDrawable();
        passwordDrawable.setShape(GradientDrawable.RECTANGLE);
        passwordDrawable.setColor(Color.TRANSPARENT); // Màu nền trong suốt
        passwordDrawable.setStroke(2, Color.parseColor("#800080")); // Màu viền tím
        passwordDrawable.setCornerRadius(8); // Đường viền góc tròn

        // Áp dụng đường viền cho các layout
        emailLayout.setBackground(emailDrawable);
        passwordLayout.setBackground(passwordDrawable);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý sự kiện khi nhấn nút Đăng Nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Email không được để trống");
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Mật khẩu không được để trống");
                return;
            }

            // Đăng nhập với Firebase Authentication
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userEmail = user.getEmail();
                                goToUserActivity(userEmail);
                            }
                        } else {
                            // Đăng nhập thất bại
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại";
                            Toast.makeText(SigninActivity.this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("LoginError", "Đăng nhập thất bại: " + errorMessage);
                        }
                    });
        });

        txtSignup.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void goToUserActivity(String email) {
        if (email.equals("nhi@gmail.com")) { // Kiểm tra nếu là tài khoản admin
            Intent intent = new Intent(SigninActivity.this, AdminActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
        }
        finish(); // Đóng trang đăng nhập
    }
}
