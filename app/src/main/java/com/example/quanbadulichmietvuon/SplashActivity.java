package com.example.quanbadulichmietvuon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.views.SigninActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logo);
        ImageView arrow = findViewById(R.id.arrow);
        Button btnLogin = findViewById(R.id.btnLogin);

        // hiệu ứng bay lên cho logo
        Animation slideUp = new TranslateAnimation(0, 0, 200, 0);
        slideUp.setDuration(2000);
        slideUp.setFillAfter(true);
        logo.startAnimation(slideUp);

        // hiệu ứng fade-in cho nút đăng nhập
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(2000);
        fadeIn.setStartOffset(1500); // delay tí để mượt hơn
        btnLogin.startAnimation(fadeIn);

        // hiệu ứng nhấp nháy cho mũi tên
        Animation bounce = new TranslateAnimation(0, 0, 0, 20);
        bounce.setDuration(600);
        bounce.setRepeatMode(Animation.REVERSE);
        bounce.setRepeatCount(Animation.INFINITE);
        arrow.startAnimation(bounce);

        // chuyển sang trang chủ khi bấm vào mũi tên
        arrow.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        });

        // chuyển sang màn hình đăng nhập khi bấm nút đăng nhập
        btnLogin.setOnClickListener(v -> startActivity(new Intent(SplashActivity.this, SigninActivity.class)));
    }
}
