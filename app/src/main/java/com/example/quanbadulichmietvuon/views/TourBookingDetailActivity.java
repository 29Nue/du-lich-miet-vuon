package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.TourBooking;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TourBookingDetailActivity extends AppCompatActivity {

    private TextView tvCustomerName, tvPhone, tvEmail, tvStartDate, tvGuests,
            tvPlaces, tvFoods, tvHotels, tvCost, tvNotes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_booking_detail);

        // Ánh xạ view
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvGuests = findViewById(R.id.tvGuests);
        tvPlaces = findViewById(R.id.tvPlaces);
        tvFoods = findViewById(R.id.tvFoods);
        tvHotels = findViewById(R.id.tvHotels);
        tvCost = findViewById(R.id.tvCost);
        tvNotes = findViewById(R.id.tvNotes);

        // Lấy bookingId từ Intent
        String bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference(Constant.Database.TOURBOOKINGS_NODE)
                    .child(bookingId);

            ref.get().addOnSuccessListener(snapshot -> {
                TourBooking tour = snapshot.getValue(TourBooking.class);
                if (tour != null) {
                    tvCustomerName.setText(tour.getCustomerName());
                    tvPhone.setText(tour.getPhone());
                    tvEmail.setText(tour.getEmail());
                    tvStartDate.setText(tour.getStartDate());
                    tvGuests.setText(tour.getGuests() + " người");
                    tvCost.setText(tour.getDesiredCost() + " VNĐ");
                    tvNotes.setText(tour.getNotes());

                    if (tour.getPlaces() != null)
                        tvPlaces.setText(TextUtils.join(", ", tour.getPlaces()));
                    if (tour.getFoods() != null)
                        tvFoods.setText(TextUtils.join(", ", tour.getFoods()));
                    if (tour.getHotels() != null)
                        tvHotels.setText(TextUtils.join(", ", tour.getHotels()));

                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin đặt tour!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Thiếu mã đặt tour!", Toast.LENGTH_SHORT).show();
        }
    }
}
