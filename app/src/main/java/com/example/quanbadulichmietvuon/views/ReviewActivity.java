package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Review;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private EditText edtReviewReason;
    private Button btnSubmitReview;
    private RatingBar ratingBar;
    private Spinner spinnerTour;
    private DatabaseReference mDatabase;

    private List<String> tourList;
    private Map<String, String> tourMap; // lưu tourName -> tourId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // khởi tạo firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // liên kết view
        ratingBar = findViewById(R.id.ratingBar);
        edtReviewReason = findViewById(R.id.edtReviewReason);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        spinnerTour = findViewById(R.id.spinnerTour);

        tourList = new ArrayList<>();
        tourMap = new HashMap<>();

        // lấy danh sách tour
        getTourList();

        // xử lý sự kiện khi nhấn nút gửi đánh giá
        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void getTourList() {
        mDatabase.child(Constant.Database.TOUR_LIST_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tourList.clear();
                tourMap.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String tourId = snapshot.getKey();
                    String tourName = snapshot.child("tourName").getValue(String.class);

                    if (tourId != null && tourName != null) {
                        tourList.add(tourName);
                        tourMap.put(tourName, tourId);
                    }
                }

                if (tourList.isEmpty()) {
                    Toast.makeText(ReviewActivity.this, "Không có tour nào để đánh giá.", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ReviewActivity.this,
                            android.R.layout.simple_spinner_item, tourList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTour.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReviewActivity.this, "Lỗi khi tải danh sách tour: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitReview() {
        String reviewReason = edtReviewReason.getText().toString().trim();
        int rating = (int) ratingBar.getRating();
        String selectedTour = (String) spinnerTour.getSelectedItem();

        if (selectedTour == null || !tourMap.containsKey(selectedTour)) {
            Toast.makeText(this, "Vui lòng chọn tour hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        String tourId = tourMap.get(selectedTour);

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reviewReason.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập lý do đánh giá.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (currentUser != null) ? currentUser.getEmail() : "Ẩn danh";

        String reviewId = mDatabase.child("tourRv").child(tourId).child("reviews").push().getKey();

        if (reviewId != null) {
            Review review = new Review(reviewId, rating, reviewReason, userEmail, System.currentTimeMillis());

            mDatabase.child("tourRv").child(tourId).child("reviews").child(reviewId).setValue(review)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đánh giá đã được gửi.", Toast.LENGTH_SHORT).show();
                            resetForm();
                        } else {
                            Toast.makeText(this, "Lỗi khi gửi đánh giá: " + task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            Toast.makeText(this, "Không thể tạo ID đánh giá.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetForm() {
        edtReviewReason.setText("");
        ratingBar.setRating(0);
        spinnerTour.setSelection(0);
    }
}
