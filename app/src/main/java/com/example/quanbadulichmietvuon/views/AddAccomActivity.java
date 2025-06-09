package com.example.quanbadulichmietvuon.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddAccomActivity extends AppCompatActivity {
    private EditText edtName, edtLocation, edtRating,etPrice,etDescription;
    private Button btnSaveAccommodation;
    private ImageView imageAccommodationPhoto1, imageAccommodationPhoto2, imageAccommodationPhoto3;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refAccommodation;
    private FirebaseStorage mStorage;
    private StorageReference refPhotoAccom;
    private Uri pathFromDevice1, pathFromDevice2, pathFromDevice3;

    private Spinner spinnerType;
    private ArrayAdapter<Accommodation.AccommodationType> typeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_accom);
        findViews();
        setListener();
        initFirebase();
        requestStoragePermission();
        setupTypeSpinner();
    }

    private void findViews() {
        edtName = findViewById(R.id.et_name);
        edtLocation = findViewById(R.id.et_location);
        edtRating = findViewById(R.id.et_rating);
        btnSaveAccommodation = findViewById(R.id.btn_add);
        imageAccommodationPhoto1 = findViewById(R.id.ivAccomImage1);
        imageAccommodationPhoto2 = findViewById(R.id.ivAccomImage2);
        imageAccommodationPhoto3 = findViewById(R.id.ivAccomImage3);
        spinnerType = findViewById(R.id.spinner_type);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);

        List<String> accommodationTypes = new ArrayList<>();
        accommodationTypes.add("HOTEL");
        accommodationTypes.add("HOMESTAY");
        accommodationTypes.add("ECOLODGE");
        accommodationTypes.add("GUESTHOUSE");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accommodationTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupTypeSpinner() {
        typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Accommodation.AccommodationType.values()
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void setListener() {
        imageAccommodationPhoto1.setOnClickListener(v -> selectImage(1));
        imageAccommodationPhoto2.setOnClickListener(v -> selectImage(2));
        imageAccommodationPhoto3.setOnClickListener(v -> selectImage(3));
        btnSaveAccommodation.setOnClickListener(v -> saveAccommodation());
    }

    private void selectImage(int imageIndex) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select an image"), imageIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();
            if (requestCode == 1) {
                pathFromDevice1 = selectedImage;
                imageAccommodationPhoto1.setImageURI(pathFromDevice1);
            } else if (requestCode == 2) {
                pathFromDevice2 = selectedImage;
                imageAccommodationPhoto2.setImageURI(pathFromDevice2);
            } else if (requestCode == 3) {
                pathFromDevice3 = selectedImage;
                imageAccommodationPhoto3.setImageURI(pathFromDevice3);
            }
        }
    }


    private void saveAccommodation() {
        String name = edtName.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String rating = edtRating.getText().toString().trim();

        String priceString = etPrice.getText().toString().trim();
        double price = 0.0;
        if (!priceString.isEmpty()) {
            try {
                price = Double.parseDouble(priceString); // Ensure price is parsed as double
            } catch (NumberFormatException e) {
                e.printStackTrace(); // or show error to user if price input is invalid
                Toast.makeText(this, "Invalid price input!", Toast.LENGTH_SHORT).show();
            }
        }

        String description = etDescription.getText().toString().trim();
        String typeString = spinnerType.getSelectedItem().toString();
        Accommodation.AccommodationType type = Accommodation.AccommodationType.valueOf(typeString);

        List<String> imageUrls = new ArrayList<>();
        int totalImages = 0;

        if (pathFromDevice1 != null) totalImages++;
        if (pathFromDevice2 != null) totalImages++;
        if (pathFromDevice3 != null) totalImages++;

        if (totalImages > 0) {
            if (pathFromDevice1 != null) {
                uploadImageToFirebase(pathFromDevice1, imageUrls, totalImages, name, location, rating, type, price, description);
            }
            if (pathFromDevice2 != null) {
                uploadImageToFirebase(pathFromDevice2, imageUrls, totalImages, name, location, rating, type, price, description);
            }
            if (pathFromDevice3 != null) {
                uploadImageToFirebase(pathFromDevice3, imageUrls, totalImages, name, location, rating, type, price, description);
            }
        } else {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(Uri imageUri, List<String> imageUrls, int totalImages, String name, String location, String rating, Accommodation.AccommodationType type, double price, String description) {
        if (imageUri == null) return;

        StorageReference refPhotoAccommodation = mStorage.getReference().child(Constant.Storage.PHOTO_ACCOM + System.currentTimeMillis());
        refPhotoAccommodation.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            refPhotoAccommodation.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrls.add(uri.toString());
                if (imageUrls.size() == totalImages) {
                    saveAccommodationToDatabase(imageUrls, name, location, rating, type, price, description); // Include price here
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(AddAccomActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveAccommodationToDatabase(List<String> imageUrls, String name, String location, String rating, Accommodation.AccommodationType type, double price, String description) {
        DatabaseReference refAccommodationId = refAccommodation.push();
        String id = refAccommodationId.getKey();

        // Pass price to the Accommodation object
        Accommodation accommodation = new Accommodation(id, imageUrls, name, location, rating, type, price, description);
        refAccommodationId.setValue(accommodation).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddAccomActivity.this, "Accommodation saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddAccomActivity.this, "Failed to save accommodation: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refAccommodation = mDatabase.getReference(Constant.Database.ACCOM_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhotoAccom = mStorage.getReference().child(Constant.Storage.PHOTO_ACCOM);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Thêm chổ ở", Toast.LENGTH_SHORT).show();
        }
    }
}
