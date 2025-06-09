package com.example.quanbadulichmietvuon.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.quanbadulichmietvuon.Adapter.ImageSliderAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import java.util.List;

public class TravelDetailActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TextView txtName, txtDescription, txtAddress, txtStatus, tvRating, tvReviewCount;
    private ImageSliderAdapter adapter;
    private LinearLayout dotLayout;
    private ImageView dot1, dot2;

    private Handler handler;
    private Runnable autoScrollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_detail);

        // Ánh xạ các view
        viewPager = findViewById(R.id.viewPagerImages);
        txtName = findViewById(R.id.tvDestinationName);
        txtDescription = findViewById(R.id.tvDestinationDescription);
        txtAddress = findViewById(R.id.tvDestinationAddress);
        txtStatus = findViewById(R.id.tvStatus);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        dotLayout = findViewById(R.id.indicatorLayout);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);

        // Lấy đối tượng TravelDestination từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("destinations")) {
            TravelDestination destination = (TravelDestination) intent.getSerializableExtra("destinations");

            // Cập nhật thông tin
            txtName.setText(destination.getName());
            txtDescription.setText(destination.getDescription());
            txtAddress.setText(destination.getAddress());

            // Cập nhật trạng thái hoạt động
            if (destination.isActive()) {
                txtStatus.setText("Đang Hoạt Động");
                txtStatus.setTextColor(getResources().getColor(R.color.green));
            } else {
                txtStatus.setText("Ngừng Hoạt Động");
                txtStatus.setTextColor(getResources().getColor(R.color.red));
            }
            // Cập nhật sự kiện click vào địa chỉ để mở Google Maps
            txtAddress.setOnClickListener(v -> {
                String address = destination.getAddress();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            });
            // Cập nhật thông tin đánh giá
            tvRating.setText(String.valueOf(destination.getRating()));
            tvReviewCount.setText("(" + destination.getReviewCount() + " đánh giá)");

            // Lấy danh sách ảnh và cập nhật vào ViewPager
            List<String> imageUrls = destination.getImages();
            adapter = new ImageSliderAdapter(this, imageUrls);
            viewPager.setAdapter(adapter);

            // Cài đặt Handler và Runnable cho tự động chuyển ảnh
            handler = new Handler();
            autoScrollRunnable = new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem() == imageUrls.size() - 1) {
                        viewPager.setCurrentItem(0, true);
                    } else {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(this, 3000);  // Chuyển sau 3 giây
                }
            };
            handler.postDelayed(autoScrollRunnable, 3000);

            // Cập nhật dots theo ảnh hiện tại
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateDots(position, imageUrls.size());
                }
            });
        }
    }

    // Cập nhật dots chỉ thị cho ảnh hiện tại
    private void updateDots(int position, int total) {
        if (total > 1) {
            if (position == 0) {
                dot1.setImageResource(R.drawable.ic_dot_active);
                dot2.setImageResource(R.drawable.ic_dot_inactive);
            } else {
                dot1.setImageResource(R.drawable.ic_dot_inactive);
                dot2.setImageResource(R.drawable.ic_dot_active);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null && autoScrollRunnable != null) {
            handler.removeCallbacks(autoScrollRunnable);
        }
    }
}
