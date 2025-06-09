package com.example.quanbadulichmietvuon.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditTravelDestinationActivity extends AppCompatActivity {

    private EditText etDestinationName, etDescription, etAddress, etRating, etReviewCount;
    private Spinner spinnerDestinationType;
    private Button btnSaveDestination;
    private ImageView ivImage1, ivImage2;

    private FirebaseDatabase mDatabase;
    private DatabaseReference refDestination;
    private FirebaseStorage mStorage;
    private StorageReference refDestinationPhoto;

    private String destinationId;
    private static final int SELECT_IMAGE_1 = 1;
    private static final int SELECT_IMAGE_2 = 2;
    private Uri pathFromDevice1, pathFromDevice2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_travel_destination);

        findViews();
        FirebaseApp.initializeApp(this);
        initFirebase();

        Intent intent = getIntent();
        destinationId = intent.getStringExtra("destinationId");
        if (destinationId != null) {
            loadDestinationDetails(destinationId);
        } else {
            Log.e("EditTravelDestination", "No destinationId received!");
        }

        btnSaveDestination.setOnClickListener(v -> saveDestinationDetails());
        ivImage1.setOnClickListener(v -> selectImage(SELECT_IMAGE_1));
        ivImage2.setOnClickListener(v -> selectImage(SELECT_IMAGE_2));

        setupSpinner();
    }

    private void findViews() {
        etDestinationName = findViewById(R.id.etDestinationName);
        etDescription = findViewById(R.id.etDescription);
        etAddress = findViewById(R.id.etAddress);
        etRating = findViewById(R.id.etRating);
        etReviewCount = findViewById(R.id.etReviewCount);
        spinnerDestinationType = findViewById(R.id.spinnerDestinationType);
        btnSaveDestination = findViewById(R.id.btnSaveDestination);
        ivImage1 = findViewById(R.id.ivImage1);
        ivImage2 = findViewById(R.id.ivImage2);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Vườn Trái Cây", "Điểm Tham Quan", "Khu Du Lịch"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestinationType.setAdapter(adapter);
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refDestination = mDatabase.getReference(Constant.Database.DESTINATION_NODE);
        mStorage = FirebaseStorage.getInstance();
        refDestinationPhoto = mStorage.getReference().child(Constant.Storage.PHOTO_DESTINATION);
    }

    private void loadDestinationDetails(String destinationId) {
        refDestination.child(destinationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TravelDestination destination = snapshot.getValue(TravelDestination.class);
                    if (destination != null) {
                        etDestinationName.setText(destination.getName());
                        etDescription.setText(destination.getDescription());
                        etAddress.setText(destination.getAddress());
                        etRating.setText(String.valueOf(destination.getRating()));
                        etReviewCount.setText(String.valueOf(destination.getReviewCount()));

                        String destinationTypeString = destination.getType().toString();

                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDestinationType.getAdapter();
                        int categoryPosition = adapter.getPosition(destinationTypeString);
                        spinnerDestinationType.setSelection(categoryPosition);


                        if (destination.getImages() != null && destination.getImages().size() >= 2) {
                            Picasso.get().load(destination.getImages().get(0)).into(ivImage1);
                            Picasso.get().load(destination.getImages().get(1)).into(ivImage2);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditTravelDestination", "Error loading data: " + error.getMessage());
            }
        });
    }

    private void saveDestinationDetails() {
        String name = etDestinationName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String reviewCount = etReviewCount.getText().toString().trim();
        String type = spinnerDestinationType.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", description);
        updates.put("address", address);
        updates.put("rating", rating);
        updates.put("reviewCount", Integer.parseInt(reviewCount));
        updates.put("type", type);

        refDestination.child(destinationId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditTravelDestinationActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("EditTravelDestination", "Update failed: " + task.getException().getMessage());
            }
        });
    }

    private void selectImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == SELECT_IMAGE_1) {
                pathFromDevice1 = selectedImageUri;
                ivImage1.setImageURI(pathFromDevice1);
            } else if (requestCode == SELECT_IMAGE_2) {
                pathFromDevice2 = selectedImageUri;
                ivImage2.setImageURI(pathFromDevice2);
            }
        }
    }
}
