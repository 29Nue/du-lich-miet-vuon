package com.example.quanbadulichmietvuon;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.example.quanbadulichmietvuon.modules.LocaleHelper;
import com.example.quanbadulichmietvuon.until.Constant;
import com.example.quanbadulichmietvuon.views.AccommodationActivity;
import com.example.quanbadulichmietvuon.views.ContactActivity;
import com.example.quanbadulichmietvuon.views.CustomTourBookingActivity;
import com.example.quanbadulichmietvuon.views.FoodActivity;
import com.example.quanbadulichmietvuon.views.IntroductionActivity;
import com.example.quanbadulichmietvuon.views.MapActivity;
import com.example.quanbadulichmietvuon.views.MarqueeBannerView;
import com.example.quanbadulichmietvuon.views.NotificationActivity;
import com.example.quanbadulichmietvuon.views.ReviewActivity;
import com.example.quanbadulichmietvuon.views.SigninActivity;
import com.example.quanbadulichmietvuon.views.TourListActivity;
import com.example.quanbadulichmietvuon.views.TravelDestinationsActivity;
import com.example.quanbadulichmietvuon.views.UserActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

    private TextView txtBookTour,txtMonan,txtChoo;
    private ImageView notificationBell,imgTourList, imgRestaurant, imgHotel, imgMap, btnUser,imgGth,imgDDDL,imgHotel1,imgHotel2;
    private TextView notificationBadge;
    private String userEmail;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this); // đặt ngôn ngữ trước khi load layout
        setContentView(R.layout.activity_main);

        // Liên kết các View
        btnUser = findViewById(R.id.btnLogin);
        imgTourList = findViewById(R.id.imgTourList);
        imgRestaurant = findViewById(R.id.imgRestaurant);
        imgHotel = findViewById(R.id.imgHotel);
        imgMap = findViewById(R.id.imgMap);
        imgDDDL = findViewById(R.id.imgTravelDestinations);
        txtMonan = findViewById(R.id.txtMonan);
        txtChoo = findViewById(R.id.txtChoo);
        imgHotel1 = findViewById(R.id.imgHotel1);
        imgHotel2 = findViewById(R.id.imgHotel2);
        imgGth = findViewById(R.id.imgGth);
        viewPager = findViewById(R.id.viewPager); // Khởi tạo ViewPager
        txtBookTour = findViewById(R.id.txtBookTour);
        MarqueeBannerView marqueeBanner = findViewById(R.id.marqueeBanner);
        notificationBell = findViewById(R.id.notificationBell);
        notificationBadge = findViewById(R.id.notificationBadge); // thêm vào layout XML

        // cập nhật số lượng thông báo
        updateNotificationBadge();

        // sự kiện click mở màn hình thông báo
        notificationBell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

    // tạo hiệu ứng fade in
        Animation fadeIn = new AlphaAnimation(0.3f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setRepeatMode(Animation.REVERSE);
        fadeIn.setRepeatCount(Animation.INFINITE); // lặp vô hạn

// tạo hiệu ứng dịch chuyển lên xuống
        Animation slideUpDown = new TranslateAnimation(0, 0, 20, 0);
        slideUpDown.setDuration(1000);
        slideUpDown.setRepeatMode(Animation.REVERSE);
        slideUpDown.setRepeatCount(Animation.INFINITE); // lặp vô hạn

    // kết hợp hai hiệu ứng vào AnimationSet
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(slideUpDown);

// áp dụng hiệu ứng lên nút đặt tour ngay
        txtBookTour.startAnimation(animationSet);

        // Lấy email người dùng từ Intent
        userEmail = getIntent().getStringExtra("EMAIL");

        // Thiết lập ViewPager với các hình ảnh
        setupViewPager();

        // Xử lý các sự kiện click
        setupClickListeners();
        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(mp -> mp.setLooping(true)); // lặp lại video

    }
    // hàm cập nhật badge
    private void updateNotificationBadge() {
        DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                .getReference(Constant.Database.NOTIFICATION_NODE);

        // lấy toàn bộ thông báo từ firebase
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;

                // duyệt qua từng thông báo
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // kiểm tra nếu thông báo hợp lệ (vd: có id, hoặc theo điều kiện khác)
                    if (snapshot.child("id").exists()) {
                        count++;
                    }
                }

                // cập nhật giao diện
                if (count > 0) {
                    notificationBadge.setVisibility(View.VISIBLE);
                    notificationBadge.setText(String.valueOf(count));
                } else {
                    notificationBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Lỗi khi đọc thông báo: " + databaseError.getMessage());
            }
        });
    }
    private void setupViewPager() {
        // Tạo các ImageView và thêm vào ViewPager
        ImageView imageView1 = new ImageView(this);
        imageView1.setImageResource(R.drawable.a1);
        imageView1.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ImageView imageView2 = new ImageView(this);
        imageView2.setImageResource(R.drawable.a2);
        imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ImageView imageView3 = new ImageView(this);
        imageView3.setImageResource(R.drawable.a3);
        imageView3.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ImageView imageView4 = new ImageView(this);
        imageView4.setImageResource(R.drawable.a4);
        imageView4.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ImageView imageView5 = new ImageView(this);
        imageView5.setImageResource(R.drawable.a5);
        imageView5.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ImageView imageView6 = new ImageView(this);
        imageView6.setImageResource(R.drawable.a6);
        imageView6.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Tạo danh sách các ImageView
        ImageView[] imageViews = {imageView1, imageView2, imageView3, imageView4,imageView5,imageView6};

        // Thiết lập Adapter cho ViewPager
        viewPager.setAdapter(new androidx.viewpager.widget.PagerAdapter() {
            @Override
            public int getCount() {
                return imageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(imageViews[position]);
                return imageViews[position];
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        // Liên kết ViewPager với CircleIndicator
        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        // Tự động chuyển trang
        autoSlideImages();
    }

    private void autoSlideImages() {
        final int delayMillis = 3000; // 3 giây
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int currentPage = 0;

            @Override
            public void run() {
                if (viewPager != null && viewPager.getAdapter() != null) {
                    currentPage = (currentPage + 1) % viewPager.getAdapter().getCount();
                    viewPager.setCurrentItem(currentPage, true);
                    handler.postDelayed(this, delayMillis);
                }
            }
        };
        handler.postDelayed(runnable, delayMillis);
    }


    private void setupClickListeners() {
        imgTourList.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, TourListActivity.class);
                startActivity(intent);
            }, 200);
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            intent.putExtra("EMAIL", userEmail);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });


        imgGth.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, IntroductionActivity.class);
                startActivity(intent);
            }, 200);
        });

        imgRestaurant.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, FoodActivity.class);
                startActivity(intent);
            }, 200);
        });

        imgHotel.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, AccommodationActivity.class);
                startActivity(intent);
            }, 200);
        });

        imgMap.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }, 200);
        });
        imgDDDL.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this,TravelDestinationsActivity.class);
                startActivity(intent);
            }, 200);
        });
        txtChoo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccommodationActivity.class);
            startActivity(intent);
        });
        txtMonan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FoodActivity.class);
            startActivity(intent);
        });
        txtBookTour.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // đã đăng nhập -> mở trang đặt tour
                Intent intent = new Intent(MainActivity.this, CustomTourBookingActivity.class);
                startActivity(intent);
            } else {
                // chưa đăng nhập -> chuyển qua trang đăng nhập
                Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Bạn cần đăng nhập để đặt tour!", Toast.LENGTH_SHORT).show();
            }
        });

        imgHotel1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccommodationActivity.class);
            startActivity(intent);
        });
        imgHotel2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccommodationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
