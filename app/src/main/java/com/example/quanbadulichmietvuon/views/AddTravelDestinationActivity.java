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
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddTravelDestinationActivity extends AppCompatActivity {
    private EditText edtName, edtDescription, edtAddress;
    private Button btnSave;
    private ImageView imagePhoto1, imagePhoto2;
    private Spinner spinnerType;
    private Uri imageUri1, imageUri2;

    private FirebaseDatabase database;
    private DatabaseReference refDestination;
    private FirebaseStorage storage;
    private StorageReference refPhoto;

    private List<String> typeList = new ArrayList<>();
    private ArrayAdapter<String> typeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_travel_destination);

        findViews();
        setListeners();
        initFirebase();
        requestStoragePermission();
        setupTypeSpinner();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initFirebase() {
        database = FirebaseDatabase.getInstance();
        refDestination = database.getReference(Constant.Database.DESTINATION_NODE);
        storage = FirebaseStorage.getInstance();
        refPhoto = storage.getReference().child(Constant.Storage.PHOTO_DESTINATION);
    }

    private void findViews() {
        edtName = findViewById(R.id.etDestinationName);
        edtDescription = findViewById(R.id.etDescription);
        edtAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSaveDestination);
        imagePhoto1 = findViewById(R.id.ivImage1);
        imagePhoto2 = findViewById(R.id.ivImage2);
        spinnerType = findViewById(R.id.spinnerDestinationType);
    }

    private void setupTypeSpinner() {
        typeList.add("Vườn Trái Cây");
        typeList.add("Điểm Tham Quan");
        typeList.add("Khu Du Lịch");

        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void setListeners() {
        imagePhoto1.setOnClickListener(v -> selectImage(1));
        imagePhoto2.setOnClickListener(v -> selectImage(2));
        btnSave.setOnClickListener(v -> saveDestination());
    }

    private void selectImage(int imageIndex) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), imageIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();
            if (requestCode == 1) {
                imageUri1 = selectedImage;
                imagePhoto1.setImageURI(imageUri1);
            } else if (requestCode == 2) {
                imageUri2 = selectedImage;
                imagePhoto2.setImageURI(imageUri2);
            }
        }
    }

    private void saveDestination() {
        DatabaseReference refId = refDestination.push();
        String id = refId.getKey();

        String name = edtName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        TravelDestination.TravelDestinationType destinationType;
        switch (type) {
            case "Vườn Trái Cây":
                destinationType = TravelDestination.TravelDestinationType.VUON_TRAI_CAY;
                break;
            case "Điểm Tham Quan":
                destinationType = TravelDestination.TravelDestinationType.DIEM_THAM_QUAN;
                break;
            default:
                destinationType = TravelDestination.TravelDestinationType.KHU_DU_LICH;
                break;
        }

        List<String> imageUrls = new ArrayList<>();
        if (imageUri1 != null) uploadImageToFirebase(imageUri1, imageUrls, refId, name, description, address, destinationType);
        if (imageUri2 != null) uploadImageToFirebase(imageUri2, imageUrls, refId, name, description, address, destinationType);
    }

    private void uploadImageToFirebase(Uri imageUri, List<String> imageUrls, DatabaseReference refId, String name, String description, String address, TravelDestination.TravelDestinationType type) {
        if (imageUri == null) return;
        StorageReference refPhotoDestination = refPhoto.child(System.currentTimeMillis() + ".jpg");
        refPhotoDestination.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                refPhotoDestination.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    if (imageUrls.size() == 2 || (imageUri1 == null || imageUri2 == null)) {
                        TravelDestination destination = new TravelDestination(refId.getKey(), name, imageUrls, true, description, address, 0.0, 0, type);
                        refId.setValue(destination).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Lưu điểm du lịch thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                })
        );
    }
}
