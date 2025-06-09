package com.example.quanbadulichmietvuon.views;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.modules.TourBooking;
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CustomTourBookingActivity extends AppCompatActivity {

    private EditText edtStartDate, edtGuests, edtCustomerName, edtPhone, edtEmail, edtDesiredCost, edtNotes;
    private MultiAutoCompleteTextView multiAutoCompleteFoods, multiAutoCompleteHotels,multiAutoCompleteDDDL;
    private Button btnSubmit;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_tour_booking);

        edtStartDate = findViewById(R.id.edtStartDate);
        edtGuests = findViewById(R.id.edtGuests);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtDesiredCost = findViewById(R.id.edtDesiredCost);
        edtNotes = findViewById(R.id.edtNotes);
        multiAutoCompleteFoods = findViewById(R.id.multiAutoCompleteFoods);
        multiAutoCompleteHotels = findViewById(R.id.multiAutoCompleteHotels);
        multiAutoCompleteDDDL = findViewById(R.id.multiAutoCompleteDDDL);
        btnSubmit = findViewById(R.id.btnSubmit);

        calendar = Calendar.getInstance();
        dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDate();
        };

        edtStartDate.setOnClickListener(v -> {
            new DatePickerDialog(CustomTourBookingActivity.this, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSubmit.setOnClickListener(v -> submitTourBooking());
        loadDataForMultiAutoComplete();

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = s.toString().trim();

                if (emailInput.isEmpty()) {
                    edtEmail.setError("Email không được để trống");
                } else if (containsEmoji(emailInput)) {
                    edtEmail.setError("Email không được chứa emoji");
                } else if (emailInput.contains(" ")) {
                    edtEmail.setError("Email không được chứa khoảng trắng");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    edtEmail.setError("Email không đúng định dạng");
                } else {
                    edtEmail.setError(null); // Xoá lỗi nếu hợp lệ
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phoneNumber = s.toString();
                if (!isValidPhoneNumber(phoneNumber)) {
                    edtPhone.setError("Số điện thoại phải có 10 số và bắt đầu bằng 0");
                } else {
                    edtPhone.setError(null);
                }
            }
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

    // hàm kiểm tra số điện thoại hợp lệ (đặt bên ngoài onCreate)
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^0\\d{9}$"); // số bắt đầu bằng 0 và có đúng 10 chữ số
    }
    private void updateStartDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        edtStartDate.setText(sdf.format(calendar.getTime()));
    }

    private void submitTourBooking() {

        String guestsStr = edtGuests.getText().toString().trim();
        String customerName = edtCustomerName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String desiredCostStr = edtDesiredCost.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();

        // lấy ngày khởi hành
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // lấy thời gian hiện tại
        String createdTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        // kiểm tra dữ liệu không được trống
        if (startDate.isEmpty() || guestsStr.isEmpty() || customerName.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || desiredCostStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin yêu cầu.", Toast.LENGTH_SHORT).show();
            return;
        }

        // chuyển đổi dữ liệu về kiểu phù hợp
        int guests = Integer.parseInt(guestsStr);
        double desiredCost = Double.parseDouble(desiredCostStr);

        List<String> selectedPlaces = Arrays.asList(multiAutoCompleteDDDL.getText().toString().trim().split(","));
        List<String> selectedFoods = Arrays.asList(multiAutoCompleteFoods.getText().toString().trim().split(","));
        List<String> selectedHotels = Arrays.asList(multiAutoCompleteHotels.getText().toString().trim().split(","));

        // giả sử days = nights + 1 (tuỳ vào logic của em iu muốn)
        int nights = 2;  // giả định là 2 đêm
        int days = nights + 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String bookingId = UUID.randomUUID().toString();

        // tạo object booking có thêm createdTime
        TourBooking booking = new TourBooking(bookingId, selectedPlaces, selectedFoods, selectedHotels, days, nights, startDate, guests, customerName, phone, email, desiredCost, notes, userId, createdTime);

        // lưu dữ liệu lên Firebase với bookingId làm key
        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Constant.Database.TOURBOOKINGS_NODE);
        database.child(bookingId).setValue(booking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đặt tour thành công. Chúng tôi sẽ liên hệ lại cho bạn ngay lập tức.", Toast.LENGTH_SHORT).show();
                finish(); // đóng activity hiện tại
                startActivity(getIntent()); // mở lại activity
            } else {
                Toast.makeText(this, "Lỗi khi gửi dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadDataForMultiAutoComplete() {
        loadMultiAutoCompleteData("destinations", TravelDestination.class, multiAutoCompleteDDDL);
        loadMultiAutoCompleteData("food", Food.class, multiAutoCompleteFoods);
        loadMultiAutoCompleteData("accom", Accommodation.class, multiAutoCompleteHotels);
    }

    private <T> void loadMultiAutoCompleteData(String path, Class<T> modelClass, MultiAutoCompleteTextView multiAutoCompleteTextView) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> itemList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("FirebaseData", "Raw Value: " + snapshot.getValue()); // debug dữ liệu gốc

                    try {
                        T item = snapshot.getValue(modelClass);
                        if (item != null) {
                            if (item instanceof Food) {
                                itemList.add(((Food) item).getName());
                            }   else if (item instanceof Accommodation) {
                                itemList.add(((Accommodation) item).getName());
                            }
                                else if (item instanceof TravelDestination) {  // xử lý cho destination
                                 itemList.add(((TravelDestination) item).getName());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseError", "Lỗi khi parse dữ liệu: " + e.getMessage());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(CustomTourBookingActivity.this,
                        android.R.layout.simple_dropdown_item_1line, itemList);
                multiAutoCompleteTextView.setAdapter(adapter);
                multiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CustomTourBookingActivity.this, "Lỗi khi tải dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
