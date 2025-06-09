package com.example.quanbadulichmietvuon.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.Adapter.ReviewAdapter;
import com.example.quanbadulichmietvuon.modules.Review;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.modules.Tour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.example.quanbadulichmietvuon.R;

import java.util.ArrayList;
import java.util.List;

public class TourDetailActivity extends AppCompatActivity {

    private TextView textTourName, textShortDescription, textDuration, textDates,
            textDestinations, textItinerary, textPrice, textIncludedServices,
            textAccommodation, textTourGuide, textCancellationPolicy;
    private ImageView imageTourPhoto;
    private MaterialButton btnCreateTour;
    private ImageButton btnMenu;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refTour;
    private boolean isFavorite = false; // Trạng thái yêu thích
    private FirebaseAuth mAuth;
    private String tourName, startDate, endDate, destinations, tourId;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        findViews();
        initFirebase();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Kiểm tra email người dùng
        if (currentUser != null && "nhi@gmail.com".equals(currentUser.getEmail())) {
            btnMenu.setVisibility(View.VISIBLE); // Hiển thị nút
        } else {
            btnMenu.setVisibility(View.GONE); // Ẩn nút
        }
        // Lấy tourId từ Intent
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tourId");

        if (tourId != null) {
            // Tải thông tin tour từ Firebase
            loadTourDetails(tourId);
        } else {
            Log.e("TourDetailActivity", "tourId bị null, không thể tải thông tin tour!");
            Toast.makeText(this, "Thiếu mã tour, không thể hiển thị chi tiết!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có tourId
        }

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo adapter và gán cho RecyclerView
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);

        // Lấy danh sách đánh giá từ Firebase
        getReviewsForTour(tourId);

        // Lấy tourId từ Intent (giả sử em đã truyền tourId khi mở TourDetailActivity)
        tourId = getIntent().getStringExtra("tourId");

    }
    // Lấy đánh giá từ Firebase
    private void getReviewsForTour(String tourId) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("tourRv").child(tourId).child("reviews");
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewList.clear(); // Xóa danh sách cũ
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    reviewList.add(review); // Thêm đánh giá vào danh sách
                }
                // Thông báo adapter đã thay đổi dữ liệu
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
                Log.e("TourDetailActivity", "Failed to load reviews: " + databaseError.getMessage());
            }
        });
    }
    private void findViews() {
            textTourName = findViewById(R.id.textTourName);
            textShortDescription = findViewById(R.id.textShortDescription);
            textDuration = findViewById(R.id.textDuration);
            textDates = findViewById(R.id.textDates);
            textDestinations = findViewById(R.id.textDestinations);
            textItinerary = findViewById(R.id.textItinerary);
            textPrice = findViewById(R.id.textPrice);
            textIncludedServices = findViewById(R.id.textIncludedServices);
            textAccommodation = findViewById(R.id.textAccommodation);
            textTourGuide = findViewById(R.id.textTourGuide);
            textCancellationPolicy = findViewById(R.id.textCancellationPolicy);
            imageTourPhoto = findViewById(R.id.imageTourPhoto);
            btnMenu = findViewById(R.id.btnMenu);
            btnCreateTour = findViewById(R.id.btnCreateTour1);

        btnCreateTour.setOnClickListener(tour -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // đã đăng nhập -> mở trang đặt tour
                Intent intent = new Intent(TourDetailActivity.this, BookingTourActivity.class);
                intent.putExtra("tourId", tourId);  // Truyền ID của tour sang
                startActivity(intent);
            } else {
                // chưa đăng nhập -> chuyển qua trang đăng nhập
                Intent intent = new Intent(TourDetailActivity.this, SigninActivity.class);
                startActivity(intent);
                Toast.makeText(TourDetailActivity.this, "Bạn cần đăng nhập để đặt tour!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the menu for the button
            btnMenu.setOnClickListener(v -> {
                // Create and show the PopupMenu
                PopupMenu popupMenu = new PopupMenu(TourDetailActivity.this, btnMenu);
                getMenuInflater().inflate(R.menu.tour_detail_menu, popupMenu.getMenu());

                // Set menu item click listener
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        // Handle the edit action
                        if (tourId != null) {
                            Intent editIntent = new Intent(TourDetailActivity.this, EditTourActivity.class);
                            editIntent.putExtra("tourId", tourId);
                            startActivity(editIntent);
                        }
                        return true;
                    } else if (item.getItemId() == R.id.action_delete) {
                        // Handle the delete action
                        if (tourId != null) {
                            new AlertDialog.Builder(TourDetailActivity.this)
                                    .setTitle("Xác nhận xoá")
                                    .setMessage("Bạn có chắc chắn muốn xoá tour này không?")
                                    .setPositiveButton("Xoá", (dialog, which) -> deleteTour())
                                    .setNegativeButton("Huỷ", null)
                                    .show();
                        } else {
                            Toast.makeText(TourDetailActivity.this, "Không tìm thấy mã tour!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else {
                        return false;
                    }
                });

                // Show the menu
                popupMenu.show();
            });
        }

    private void deleteTour() {
        if (tourId != null) {
            refTour.child(tourId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(TourDetailActivity.this, "Xoá tour thành công", Toast.LENGTH_SHORT).show();
                            finish();  // Đóng activity sau khi xoá
                        } else {
                            Toast.makeText(TourDetailActivity.this, "Xoá tour thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("TourDetailActivity", "tourId bị null, không thể xoá tour.");
        }
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refTour = mDatabase.getReference(Constant.Database.TOUR_LIST_NODE);
    }

    private void loadTourDetails(String tourId) {
        if (tourId != null) {
            refTour.child(tourId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Tour tour = snapshot.getValue(Tour.class);
                        if (tour != null) {
                            tourName = tour.getTourName();
                            startDate = tour.getStartDate();
                            endDate = tour.getEndDate();
                            destinations = tour.getDestinations();

                            // Cập nhật giao diện
                            textTourName.setText(tourName);
                            textShortDescription.setText(tour.getShortDescription());
                            textDuration.setText(String.format("%s ngày %s đêm", tour.getDurationDays(), tour.getDurationNights()));
                            textDates.setText(String.format("Ngày khởi hành: %s - Ngày kết thúc: %s", startDate, endDate));
                            textDestinations.setText(destinations);
                            textItinerary.setText(tour.getItinerary());
                            textPrice.setText(String.format("%s VND", tour.getPrice()));
                            textIncludedServices.setText(tour.getIncludedServices());
                            textAccommodation.setText(tour.getAccommodation());
                            textTourGuide.setText(tour.getTourGuide());
                            textCancellationPolicy.setText(tour.getCancellationPolicy());

                            // Tải ảnh
                            FirebaseStorage.getInstance().getReference(tour.getPhoto())
                                    .getDownloadUrl()
                                    .addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).into(imageTourPhoto))
                                    .addOnFailureListener(e -> Log.e("FirebaseStorage", "Error loading image: " + e.getMessage()));
                        }
                    } else {
                        Log.e("TourDetailActivity", "Tour not found in database.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("TourDetailActivity", "Error loading tour data: " + error.getMessage());
                }
            });
        } else {
            Log.e("TourDetailActivity", "tourId is null, cannot load tour details!");
        }
    }
}
