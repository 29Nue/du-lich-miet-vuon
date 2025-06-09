package com.example.quanbadulichmietvuon.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.LocaleHelper;
import com.example.quanbadulichmietvuon.modules.User;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText edtuser, edtemail, edtpassword, edtconfirmPassword, edtVerificationCode;
    private TextView txtsignin;
    private Button btnsignup, btnVerifyCode;
    private LinearLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refUser;

    private String verificationCode; // Mã xác nhận
    private boolean isCodeVerified = false; // Cờ kiểm tra xác minh mã
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase
        initFirebase();

        // Tìm các view
        FindViews();

        // Thiết lập đường viền cho các trường nhập
        setInputFieldBorders();

        // Thiết lập các listener
        setListeners();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        refUser = mDatabase.getReference().child(Constant.Database.USER_NODE);
    }

    private void FindViews() {
        edtuser = findViewById(R.id.edtUsername);
        edtemail = findViewById(R.id.edtEmail);
        edtpassword = findViewById(R.id.edtPassword);
        edtconfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtVerificationCode = findViewById(R.id.edtVerificationCode);

        btnsignup = findViewById(R.id.btnSignup);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        txtsignin = findViewById(R.id.txtSignin);

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailLayout = findViewById(R.id.emailLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        // Ẩn trường nhập mã xác nhận và nút xác minh ban đầu
        edtVerificationCode.setVisibility(View.GONE);
        btnVerifyCode.setVisibility(View.GONE);
    }

    private void setInputFieldBorders() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(2, Color.parseColor("#800080"));
        drawable.setCornerRadius(8);

        usernameLayout.setBackground(drawable);
        emailLayout.setBackground(drawable);
        passwordLayout.setBackground(drawable);
        confirmPasswordLayout.setBackground(drawable);
    }

    private void setListeners() {
        btnsignup.setOnClickListener(view -> {
            String email = edtemail.getText().toString().trim();
            String password = edtpassword.getText().toString().trim();
            String confirmPassword = edtconfirmPassword.getText().toString().trim();

            // Kiểm tra email
            if (email.isEmpty()) {
                edtemail.setError("Email không được để trống");
                return;
            }
            if (containsEmoji(email)) {
                edtemail.setError("Email không được chứa emoji");
                return;
            }
            if (email.contains(" ")) {
                edtemail.setError("Email không được chứa khoảng trắng");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtemail.setError("Email không hợp lệ");
                return;
            }

            // Kiểm tra mật khẩu
            if (password.isEmpty()) {
                edtpassword.setError("Mật khẩu không được để trống");
                return;
            }
            if (!isStrongPassword(password)) {
                edtpassword.setError("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
                return;
            }
            if (!password.equals(confirmPassword)) {
                edtconfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            // Kiểm tra email tồn tại
            refUser.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    edtemail.setError("Email đã được sử dụng");
                    Toast.makeText(SignupActivity.this, "Email đã tồn tại, vui lòng chọn email khác", Toast.LENGTH_SHORT).show();
                } else {
                    sendVerificationCodeToEmail(email);
                    edtVerificationCode.setVisibility(View.VISIBLE);
                    btnVerifyCode.setVisibility(View.VISIBLE);
                }
            });
        });

        btnVerifyCode.setOnClickListener(view -> {
            String enteredCode = edtVerificationCode.getText().toString().trim();

            if (enteredCode.isEmpty()) {
                edtVerificationCode.setError("Vui lòng nhập mã xác nhận");
                return;
            }

            if (verifyCode(enteredCode)) {
                registerUser();
            } else {
                Toast.makeText(SignupActivity.this, "Mã xác nhận không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

        txtsignin.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
        });
    }
    private boolean containsEmoji(String input) {
        for (int i = 0; i < input.length(); i++) {
            int type = Character.getType(input.charAt(i));
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return true;
            }
        }
        return false;
    }

    private boolean isStrongPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }

    private void sendVerificationCodeToEmail(String email) {
        // Tạo mã xác nhận ngẫu nhiên
        verificationCode = String.valueOf((int) (Math.random() * 9000) + 1000);

        // Thực tế cần gửi email qua SMTP hoặc dịch vụ như SendGrid. Ở đây ta chỉ giả lập:
        Toast.makeText(this, "Mã xác nhận đã được gửi đến: " + email + "\nMã: " + verificationCode, Toast.LENGTH_LONG).show();
    }

    private boolean verifyCode(String enteredCode) {
        return enteredCode.equals(verificationCode);
    }

    private void registerUser() {
        String email = edtemail.getText().toString().trim();
        String password = edtpassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String id = mAuth.getCurrentUser().getUid();
                        User user = new User(
                                id,
                                email,
                                edtuser.getText().toString(),
                                "",
                                ""
                        );
                        refUser.child(id).setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, SigninActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
