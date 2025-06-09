package com.example.quanbadulichmietvuon.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Tour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTourActivity extends AppCompatActivity {

    private EditText editTourName, editShortDescription, edtDurationDays, edtDurationNights,
            editDestinations, editItinerary, editPrice, editIncludedServices,
            editAccommodation, editTourGuide, editCancellationPolicy;
    private Button btnSave;
    private EditText editStartDate, editEndDate;
    private Button btnPickStartDate, btnPickEndDate;
    private final Calendar calendar = Calendar.getInstance();
    private FirebaseDatabase mDatabase;
    private DatabaseReference refTour;
    private String tourId;
    private ShapeableImageView imagePhoto;
    private static final int SELECT_A_PHOTO = 1;
    private Uri pathFromDevice;
    private StorageReference refPhotoTour;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tour);

        findViews();
        FirebaseApp.initializeApp(this);
        initFirebase();

        // Get tour ID from Intent
        Intent intent = getIntent();
        tourId = intent.getStringExtra("tourId");

        if (tourId != null) {
            Log.d("EditTourActivity", "Tour ID received: " + tourId);
            loadTourDetails(tourId);
            refPhotoTour = mStorage.getReference()
                    .child(Constant.Storage.PHOTO_TOUR)
                    .child(tourId); // Reference to the specific tour's photo
        } else {
            Log.e("EditTourActivity", "No tourId received!");
        }

        btnSave.setOnClickListener(v -> saveTourDetails());
    }

    private void findViews() {
        editTourName = findViewById(R.id.edtTourName);
        editShortDescription = findViewById(R.id.edtDescription);
        edtDurationDays = findViewById(R.id.edtDurationDays);
        edtDurationNights = findViewById(R.id.edtDurationNights);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        editDestinations = findViewById(R.id.edtDestinations);
        editItinerary = findViewById(R.id.edtItinerary);
        editPrice = findViewById(R.id.edtPrice);
        editIncludedServices = findViewById(R.id.edtIncludedServices);
        editAccommodation = findViewById(R.id.edtAccommodation);
        editTourGuide = findViewById(R.id.edtTourGuide);
        editCancellationPolicy = findViewById(R.id.edtCancellationPolicy);
        btnSave = findViewById(R.id.btnSaveTour);
        imagePhoto = findViewById(R.id.imagePhoto);

        // Start Date Picker
        btnPickStartDate.setOnClickListener(v -> showDatePicker(editStartDate));

        // End Date Picker
        btnPickEndDate.setOnClickListener(v -> showDatePicker(editEndDate));
        imagePhoto.setOnClickListener(v -> selectPhoto());
    }

    private void showDatePicker(final EditText targetEditText) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Update calendar with selected date
            calendar.set(year, month, dayOfMonth);
            // Format date
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
            // Display in the corresponding EditText
            targetEditText.setText(formattedDate);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refTour = mDatabase.getReference(Constant.Database.TOUR_LIST_NODE);
        mStorage = FirebaseStorage.getInstance(); // Initialize Firebase Storage
    }

    private void loadTourDetails(String tourId) {
        refTour.child(tourId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        editTourName.setText(tour.getTourName());
                        editShortDescription.setText(tour.getShortDescription());
                        edtDurationDays.setText(String.format("%s ngày ", tour.getDurationDays()));
                        edtDurationNights.setText(String.format("%s đêm", tour.getDurationNights()));
                        editStartDate.setText(String.format("Ngày khởi hành: %s ", tour.getStartDate()));
                        editEndDate.setText(String.format("Ngày kết thúc: %s", tour.getEndDate()));
                        editDestinations.setText(tour.getDestinations());
                        editItinerary.setText(tour.getItinerary());
                        editPrice.setText(String.format("%s ", tour.getPrice()));
                        editIncludedServices.setText(tour.getIncludedServices());
                        editAccommodation.setText(tour.getAccommodation());
                        editTourGuide.setText(tour.getTourGuide());
                        editCancellationPolicy.setText(tour.getCancellationPolicy());

                        // Tải ảnh từ Firebase Storage nếu refPhotoTour không null
                        if (refPhotoTour != null) {
                            refPhotoTour.getDownloadUrl()
                                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(imagePhoto))
                                    .addOnFailureListener(e -> imagePhoto.setImageResource(R.drawable.a));
                        } else {
                            Log.e("EditTourActivity", "Photo reference is null!");
                        }
                    }
                } else {
                    Log.e("EditTourActivity", "Tour does not exist!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditTourActivity", "Error loading tour data: " + error.getMessage());
            }
        });
    }
    private void saveTourDetails() {
        String newTourName = editTourName.getText().toString().trim();
        String newShortDescription = editShortDescription.getText().toString().trim();
        String newDurationDays =  edtDurationDays.getText().toString().trim();
        String newDurationNights = edtDurationNights.getText().toString().trim();
        String newStartDate = editStartDate.getText().toString().trim();
        String newEndDate = editEndDate.getText().toString().trim();
        String newDestinations = editDestinations.getText().toString().trim();
        String newItinerary = editItinerary.getText().toString().trim();
        String newIncludedServices = editIncludedServices.getText().toString().trim();
        String newAccommodation = editAccommodation.getText().toString().trim();
        String newTourGuide = editTourGuide.getText().toString().trim();
        String newCancellationPolicy = editCancellationPolicy.getText().toString().trim();

        // Validate price input
        String priceInput = editPrice.getText().toString().trim();
        if (priceInput.isEmpty()) {
            Toast.makeText(this, "Price is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newTourName.isEmpty()) {
            Toast.makeText(this, "Tour name is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pathFromDevice != null) {
            StorageReference photoRef = refPhotoTour.child(tourId);
            photoRef.putFile(pathFromDevice).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        saveTourDetailsToDatabase(tourId, newTourName, newShortDescription, newDurationDays,
                                newDurationNights, newStartDate, newEndDate, newDestinations,
                                newItinerary, priceInput, newIncludedServices, newAccommodation,
                                newTourGuide, newCancellationPolicy, photoUrl);
                    });
                } else {
                    Log.e("EditTourActivity", "Upload failed: " + task.getException().getMessage());
                    Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveTourDetailsToDatabase(tourId, newTourName, newShortDescription, newDurationDays,
                    newDurationNights, newStartDate, newEndDate, newDestinations,
                    newItinerary, priceInput, newIncludedServices, newAccommodation,
                    newTourGuide, newCancellationPolicy, null);
        }
    }
    private void saveTourDetailsToDatabase(String tourId, String tourName, String shortDescription, String durationDays,
                                           String durationNights, String startDate, String endDate, String destinations,
                                           String itinerary, String price, String includedServices, String accommodation,
                                           String tourGuide, String cancellationPolicy, String photoUrl) {
        Map<String, Object> tourUpdates = new HashMap<>();
        tourUpdates.put("tourName", tourName);
        tourUpdates.put("shortDescription", shortDescription);
        tourUpdates.put("durationDays", durationDays);
        tourUpdates.put("durationNights", durationNights);
        tourUpdates.put("startDate", startDate);
        tourUpdates.put("endDate", endDate);
        tourUpdates.put("destinations", destinations);
        tourUpdates.put("itinerary", itinerary);
        tourUpdates.put("price", price);
        tourUpdates.put("includedServices", includedServices);
        tourUpdates.put("accommodation", accommodation);
        tourUpdates.put("tourGuide", tourGuide);
        tourUpdates.put("cancellationPolicy", cancellationPolicy);

        if (photoUrl != null) {
            tourUpdates.put("photo", photoUrl);
        }

        refTour.child(tourId).updateChildren(tourUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditTourActivity.this, "Tour updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("EditTourActivity", "Update failed: " + task.getException().getMessage());
            }
        });
    }
    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), SELECT_A_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_A_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pathFromDevice = data.getData();
            imagePhoto.setImageURI(pathFromDevice);
        }
    }
}
