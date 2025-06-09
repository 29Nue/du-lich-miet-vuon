package com.example.quanbadulichmietvuon.views;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.BookingTour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class BookingTourActivity extends AppCompatActivity {
    private EditText editStartDate, editEndDate, editCustomerName, editCustomerPhone, editCustomerCount, editDepartureLocation;
    private Spinner spinnerTourName;
    private Button btnConfirmCreateTour;
    private RadioButton radioFixedTour, radioCustomTour;
    private String tourId;
    private DatabaseReference tourRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.quanbadulichmietvuon.R.layout.activity_booking_tour);

        spinnerTourName = findViewById(com.example.quanbadulichmietvuon.R.id.spinnerTourName);
        editDepartureLocation = findViewById(com.example.quanbadulichmietvuon.R.id.editDepartureLocation);
        editStartDate = findViewById(com.example.quanbadulichmietvuon.R.id.editStartDate);
        editEndDate = findViewById(com.example.quanbadulichmietvuon.R.id.editEndDate);
        editCustomerName = findViewById(com.example.quanbadulichmietvuon.R.id.editCustomerName);
        editCustomerPhone = findViewById(com.example.quanbadulichmietvuon.R.id.editCustomerPhone);
        editCustomerCount = findViewById(com.example.quanbadulichmietvuon.R.id.editCustomerCount);
        btnConfirmCreateTour = findViewById(com.example.quanbadulichmietvuon.R.id.btnConfirmCreateTour);
        radioFixedTour = findViewById(com.example.quanbadulichmietvuon.R.id.radioFixedTour);
        radioCustomTour = findViewById(R.id.radioCustomTour);

        radioFixedTour.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editStartDate.setText("Chọn tour để hiển thị ngày");
                editEndDate.setText("Chọn tour để hiển thị ngày");
                editStartDate.setEnabled(false);
                editEndDate.setEnabled(false);
            }
        });

        radioCustomTour.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editStartDate.setText("");
                editEndDate.setText("");
                editStartDate.setEnabled(true);
                editEndDate.setEnabled(true);
            }
        });

        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate));

        loadTourList();
        tourId = getIntent().getStringExtra("tourId");
        if (tourId != null) loadTourDetails(tourId);
        else Toast.makeText(this, "Lỗi: Không nhận được tourId", Toast.LENGTH_SHORT).show();

        btnConfirmCreateTour.setOnClickListener(v -> saveTourToFirebase());

        editCustomerPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString();
                if (!isValidPhoneNumber(phoneNumber)) {
                    editCustomerPhone.setError("Số điện thoại phải có 10 số và bắt đầu bằng 0");
                } else {
                    editCustomerPhone.setError(null);
                }
            }
        });
    }

    // hàm kiểm tra số điện thoại hợp lệ
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^0\\d{9}$"); // số bắt đầu bằng 0 và có đúng 10 chữ số
    }

    private void loadTourList() {
        tourRef = FirebaseDatabase.getInstance().getReference(Constant.Database.TOUR_LIST_NODE);
        tourRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> tourNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String tourName = snapshot.child("tourName").getValue(String.class);
                    if (tourName != null) tourNames.add(tourName);
                }
                if (tourNames.isEmpty()) tourNames.add("Không có tour nào");
                spinnerTourName.setAdapter(new ArrayAdapter<>(BookingTourActivity.this, android.R.layout.simple_spinner_item, tourNames));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BookingTourActivity.this, "Lỗi khi tải danh sách tour", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTourDetails(String tourId) {
        tourRef = FirebaseDatabase.getInstance().getReference(Constant.Database.TOUR_LIST_NODE).child(tourId);
        tourRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(BookingTourActivity.this, "Tour không tồn tại", Toast.LENGTH_SHORT).show();
                    return;
                }
                editDepartureLocation.setText(dataSnapshot.child("destinations").getValue(String.class));
                editStartDate.setText(dataSnapshot.child("startDate").getValue(String.class));
                editEndDate.setText(dataSnapshot.child("endDate").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BookingTourActivity.this, "Lỗi khi tải thông tin tour", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> editText.setText(dayOfMonth + "/" + (month + 1) + "/" + year), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTourToFirebase() {
        if (Arrays.asList(editStartDate, editEndDate, editCustomerName, editCustomerPhone, editCustomerCount, editDepartureLocation).stream().anyMatch(e -> e.getText().toString().isEmpty())) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference(Constant.Database.BOOKTOUR_NODE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String bookingId = bookingRef.push().getKey();

        // lấy thời gian hiện tại
        String createdTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        BookingTour bookingTour = new BookingTour(
                bookingId,
                userId,
                spinnerTourName.getSelectedItem().toString(),
                editStartDate.getText().toString(),
                editEndDate.getText().toString(),
                editCustomerName.getText().toString(),
                editCustomerPhone.getText().toString(),
                Integer.parseInt(editCustomerCount.getText().toString()),
                editDepartureLocation.getText().toString(),
                createdTime,
                false // trạng thái hoàn thành ban đầu là false
        );

        bookingRef.child(bookingId).setValue(bookingTour)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingTourActivity.this, "Đặt tour thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BookingTourActivity.this, "Lỗi khi đặt tour", Toast.LENGTH_SHORT).show());
    }
}