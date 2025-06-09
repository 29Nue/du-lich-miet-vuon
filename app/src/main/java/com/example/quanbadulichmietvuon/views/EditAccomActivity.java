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
import com.example.quanbadulichmietvuon.modules.Accommodation;
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
import java.util.List;
import java.util.Map;

public class EditAccomActivity extends AppCompatActivity {

    private EditText edtAccomName, edtLocation, edtRating,etPrice,etDescription;
    private Spinner spinnerType;
    private Button btnSaveAccom;
    private ImageView ivAccomImage1, ivAccomImage2, ivAccomImage3;

    private FirebaseDatabase mDatabase;
    private DatabaseReference refAccom;
    private FirebaseStorage mStorage;
    private StorageReference refAccomPhoto;

    private String accomId;
    private static final int SELECT_IMAGE_1 = 1;
    private static final int SELECT_IMAGE_2 = 2;
    private static final int SELECT_IMAGE_3 = 3;
    private Uri pathFromDevice1, pathFromDevice2, pathFromDevice3; // Để lưu ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_accom);

        findViews();
        FirebaseApp.initializeApp(this);
        initFirebase();

        // Lấy accomId từ Intent
        Intent intent = getIntent();
        accomId = intent.getStringExtra("accomId");
        if (accomId != null) {
            loadAccomDetails(accomId);
        } else {
            Log.e("EditAccomActivity", "No accomId received!");
        }

        btnSaveAccom.setOnClickListener(v -> saveAccomDetails());

        // Thêm sự kiện click cho các ảnh
        ivAccomImage1.setOnClickListener(v -> selectImage(SELECT_IMAGE_1)); // Chọn ảnh cho ivAccomImage1
        ivAccomImage2.setOnClickListener(v -> selectImage(SELECT_IMAGE_2)); // Chọn ảnh cho ivAccomImage2
        ivAccomImage3.setOnClickListener(v -> selectImage(SELECT_IMAGE_3)); // Chọn ảnh cho ivAccomImage3

        // Initialize spinner
        setupSpinner();
    }

    private void findViews() {
        edtAccomName = findViewById(R.id.et_name);
        edtLocation = findViewById(R.id.et_location);
        edtRating = findViewById(R.id.et_rating);
        spinnerType = findViewById(R.id.spinner_type);
        btnSaveAccom = findViewById(R.id.btn_save);
        ivAccomImage1 = findViewById(R.id.ivAccomImage1);
        ivAccomImage2 = findViewById(R.id.ivAccomImage2);
        ivAccomImage3 = findViewById(R.id.ivAccomImage3);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"HOTEL", "HOMESTAY", "ECOLODGE", "GUESTHOUSE"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refAccom = mDatabase.getReference(Constant.Database.ACCOM_NODE);
        mStorage = FirebaseStorage.getInstance();
        refAccomPhoto = mStorage.getReference().child(Constant.Storage.PHOTO_ACCOM);
    }

    private void loadAccomDetails(String accomId) {
        refAccom.child(accomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Accommodation accom = snapshot.getValue(Accommodation.class);
                    if (accom != null) {
                        edtAccomName.setText(accom.getName());
                        edtLocation.setText(accom.getLocation());
                        edtRating.setText(accom.getRating());

                        etPrice.setText(String.valueOf(accom.getPrice())); // Hiển thị giá
                        etDescription.setText(accom.getDescription()); // Hiển thị mô tả

                        // Lấy loại của accommodation
                        Accommodation.AccommodationType accomType = accom.getType();
                        String accomTypeString = accomType.toString(); // HOTEL, HOMESTAY, ECOLODGE, GUESTHOUSE
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerType.getAdapter();
                        int categoryPosition = adapter.getPosition(accomTypeString); // Tìm vị trí của loại trong Spinner
                        spinnerType.setSelection(categoryPosition); // Đặt lựa chọn của spinner


                        // Kiểm tra và tải ảnh vào các ImageView nếu có
                        List<String> imageResIds = accom.getImageResId(); // Lấy danh sách ảnh
                        if (imageResIds != null && imageResIds.size() >= 3) {
                            Picasso.get().load(imageResIds.get(0)).into(ivAccomImage1); // Ảnh đầu tiên
                            Picasso.get().load(imageResIds.get(1)).into(ivAccomImage2); // Ảnh thứ hai
                            Picasso.get().load(imageResIds.get(2)).into(ivAccomImage3); // Ảnh thứ ba
                        } else {
                            Log.e("EditAccomActivity", "Không có đủ ảnh hoặc imageResIds là null!");
                        }
                    }
                } else {
                    Log.e("EditAccomActivity", "Accommodation does not exist!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditAccomActivity", "Error loading accom data: " + error.getMessage());
            }
        });
    }

    private void saveAccomDetails() {
        String newAccomName = edtAccomName.getText().toString().trim();
        String newAccomLocation = edtLocation.getText().toString().trim();
        String newAccomRating = edtRating.getText().toString().trim();
        String newType = spinnerType.getSelectedItem().toString();
        double newPrice;

        try {
            newPrice = Double.parseDouble(etPrice.getText().toString().trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            newPrice = 0.0;
        }
        String newDescription = etDescription.getText().toString().trim();

        // Cập nhật ảnh nếu có
        if (pathFromDevice1 != null || pathFromDevice2 != null || pathFromDevice3 != null) {
            Map<String, Object> accomUpdates = new HashMap<>();

            if (pathFromDevice1 != null) {
                updateImageInFirebase(pathFromDevice1, "_1", accomUpdates, newAccomName, newAccomLocation, newAccomRating, newType, newPrice, newDescription);
            }

            if (pathFromDevice2 != null) {
                updateImageInFirebase(pathFromDevice2, "_2", accomUpdates, newAccomName, newAccomLocation, newAccomRating, newType, newPrice, newDescription);
            }

            if (pathFromDevice3 != null) {
                updateImageInFirebase(pathFromDevice3, "_3", accomUpdates, newAccomName, newAccomLocation, newAccomRating, newType, newPrice, newDescription);
            }
        } else {
            Map<String, Object> emptyUpdates = new HashMap<>();
            uploadAccommodationDetails(emptyUpdates, newAccomName, newAccomLocation, newAccomRating, newType, newPrice, newDescription);
        }
    }

    private void updateImageInFirebase(Uri imagePath, String imageSuffix, Map<String, Object> accomUpdates, String newAccomName, String newAccomLocation, String newAccomRating, String newType, double newPrice, String newDescription) {
        StorageReference photoRef = refAccomPhoto.child(accomId + imageSuffix);
        photoRef.putFile(imagePath).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    accomUpdates.put("imageResId" + imageSuffix, uri.toString());
                    uploadAccommodationDetails(accomUpdates, newAccomName, newAccomLocation, newAccomRating, newType, newPrice, newDescription);
                });
            }
        });
    }

    private void uploadAccommodationDetails(Map<String, Object> accomUpdates, String accomName, String accomLocation, String accomRating, String type, double price, String description) {
        accomUpdates.put("name", accomName);
        accomUpdates.put("location", accomLocation);
        accomUpdates.put("rating", accomRating);
        accomUpdates.put("type", type);
        accomUpdates.put("price", price);  // Cập nhật giá
        accomUpdates.put("description", description);  // Cập nhật mô tả

        refAccom.child(accomId).updateChildren(accomUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditAccomActivity.this, "Accommodation updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("EditAccomActivity", "Update failed: " + task.getException().getMessage());
            }
        });
    }

    private void selectImage(int imageNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), imageNumber);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            switch (requestCode) {
                case SELECT_IMAGE_1:
                    pathFromDevice1 = selectedImageUri;
                    ivAccomImage1.setImageURI(pathFromDevice1);
                    break;
                case SELECT_IMAGE_2:
                    pathFromDevice2 = selectedImageUri;
                    ivAccomImage2.setImageURI(pathFromDevice2);
                    break;
                case SELECT_IMAGE_3:
                    pathFromDevice3 = selectedImageUri;
                    ivAccomImage3.setImageURI(pathFromDevice3);
                    break;
            }
        }
    }
}
