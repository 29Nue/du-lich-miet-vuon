package com.example.quanbadulichmietvuon.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.modules.Tour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddTourActivity extends AppCompatActivity {
    private EditText edtTourName, edtDescription, edtDurationDays, edtDurationNights, edtDestinations,
            edtItinerary, edtPrice, edtIncludedServices, edtAccommodation, edtTourGuide, edtCancellationPolicy;
    private Button btnPickStartDate, btnPickEndDate, btnSaveTour;
    private ImageView imagePhoto;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refTour;
    private FirebaseStorage mStorage;
    private StorageReference refPhotoTour;

    private static final int SELECT_A_PHOTO = 1; // Request code for photo selection
    private Uri pathFromDevice; // URI to store the selected image
    private Calendar calendarStartDate, calendarEndDate;
    private DatePickerDialog datePickerDialog;

    private Spinner spinnerAccommodation;
    private ArrayAdapter<String> accommodationAdapter;
    private List<String> accommodationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour);
        findViews();
        setListener();
        initFirebase();
        requestStoragePermission(); // Request storage permission

        // Initialize calendar for start and end dates
        calendarStartDate = Calendar.getInstance();
        calendarEndDate = Calendar.getInstance();
        setupAccommodationSpinner();

    }

    // Request storage permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    // Handle results from permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Thêm Tour", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Initialize Firebase database and storage
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refTour = mDatabase.getReference(Constant.Database.TOUR_LIST_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhotoTour = mStorage.getReference().child(Constant.Storage.PHOTO_TOUR);
    }

    // Find views by ID
    private void findViews() {
        edtTourName = findViewById(R.id.edtTourName);
        edtDescription = findViewById(R.id.edtDescription);
        edtDurationDays = findViewById(R.id.edtDurationDays);
        edtDurationNights = findViewById(R.id.edtDurationNights);
        edtDestinations = findViewById(R.id.edtDestinations);
        edtItinerary = findViewById(R.id.edtItinerary);
        edtPrice = findViewById(R.id.edtPrice);
        edtIncludedServices = findViewById(R.id.edtIncludedServices);
        edtAccommodation = findViewById(R.id.edtAccommodation);
        edtTourGuide = findViewById(R.id.edtTourGuide);
        edtCancellationPolicy = findViewById(R.id.edtCancellationPolicy);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnSaveTour = findViewById(R.id.btnSaveTour);
        imagePhoto = findViewById(R.id.imagePhoto);
        spinnerAccommodation = findViewById(R.id.spinnerAccommodation);

    }
    private void setupAccommodationSpinner() {
        // Kết nối Firebase Database
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference refAccommodation = mDatabase.getReference(Constant.Database.ACCOM_NODE);

        // Danh sách khách sạn
        List<String> accommodationList = new ArrayList<>();
        ArrayAdapter<String> accommodationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accommodationList);
        accommodationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccommodation.setAdapter(accommodationAdapter);

        // Lấy danh sách khách sạn từ Firebase
        refAccommodation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accommodationList.clear(); // Xóa danh sách cũ
                for (DataSnapshot data : snapshot.getChildren()) {
                    Accommodation accommodation = data.getValue(Accommodation.class);
                    if (accommodation != null) {
                        accommodationList.add(accommodation.getName()); // Lấy tên khách sạn
                    }
                }
                accommodationAdapter.notifyDataSetChanged(); // Cập nhật dữ liệu cho Spinner
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTourActivity.this, "Failed to load accommodations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set listeners for events
    private void setListener() {
        // Listener for image selection
        imagePhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Select an image"),
                    SELECT_A_PHOTO
            );
        });

        // Listener for saving the tour
        btnSaveTour.setOnClickListener(v -> {
                    DatabaseReference refTourId = refTour.push();
                    String id = refTourId.getKey();

                    // Gather tour information
                    String tourName = edtTourName.getText().toString().trim();
                    String shortDescription = edtDescription.getText().toString().trim();
                    String destinations = edtDestinations.getText().toString().trim();
                    String itinerary = edtItinerary.getText().toString().trim();
                    String accommodation = spinnerAccommodation.getSelectedItem().toString();
                    //String accommodation = edtAccommodation.getText().toString().trim();
                    String tourGuide = edtTourGuide.getText().toString().trim();
                    String cancellationPolicy = edtCancellationPolicy.getText().toString().trim();
                    String durationDays = edtDurationDays.getText().toString().trim();
                    String durationNights = edtDurationNights.getText().toString().trim();
                    String startDate = btnPickStartDate.getText().toString();
                    String endDate = btnPickEndDate.getText().toString();
                    String price = edtPrice.getText().toString().trim();
                    String includedServices = edtIncludedServices.getText().toString().trim();
                    String photo = ""; // Store single image as string

                    // Create Tour object
                    Tour tour = new Tour(
                            id,
                            tourName,
                            shortDescription,
                            durationDays,
                            durationNights,
                            startDate,
                            endDate,
                            destinations,
                            itinerary,
                            price,
                            includedServices,
                            accommodation,
                            tourGuide,
                            cancellationPolicy,
                            Constant.Storage.PHOTO_TOUR + refTourId.getKey()
                    );

                    // Save tour to Firebase
                    refTourId.setValue(tour).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Check if there is an image path
                            if (pathFromDevice != null) {
                                refPhotoTour.child(refTourId.getKey())
                                        .putFile(pathFromDevice)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Toast.makeText(AddTourActivity.this, "Tour saved successfully!", Toast.LENGTH_SHORT).show();
                                                finish(); // End Activity
                                            }
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(AddTourActivity.this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(AddTourActivity.this, "Failed to save tour: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                    // Listener for start date selection
        btnPickStartDate.setOnClickListener(v -> {
            int year = calendarStartDate.get(Calendar.YEAR);
            int month = calendarStartDate.get(Calendar.MONTH);
            int day = calendarStartDate.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(AddTourActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                calendarStartDate.set(selectedYear, selectedMonth, selectedDay);
                // Cập nhật text trên nút với ngày đã chọn
                btnPickStartDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Listener for end date selection
        btnPickEndDate.setOnClickListener(v -> {
            int year = calendarEndDate.get(Calendar.YEAR);
            int month = calendarEndDate.get(Calendar.MONTH);
            int day = calendarEndDate.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(AddTourActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                calendarEndDate.set(selectedYear, selectedMonth, selectedDay);
                // Cập nhật text trên nút với ngày đã chọn
                btnPickEndDate.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    // Handle results from selecting an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_A_PHOTO && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                pathFromDevice = data.getData(); // Get the selected image URI
                imagePhoto.setImageURI(pathFromDevice); // Display selected image in ImageView
            }
        }
    }
}
