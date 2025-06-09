package com.example.quanbadulichmietvuon.views;

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
import android.widget.Spinner;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddFoodActivity extends AppCompatActivity {
    private EditText edtFoodName, edtDescription, edtPrice,edtLocation;
    private Button btnSaveFood;
    private ImageView imageFoodPhoto;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refFood;
    private FirebaseStorage mStorage;
    private StorageReference refPhotoFood;

    private static final int SELECT_A_PHOTO = 1; // Request code for photo selection
    private Uri pathFromDevice; // URI to store the selected image

    private Spinner spinnerCategory;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        findViews();
        setListener();
        initFirebase();
        requestStoragePermission(); // Request storage permission
        setupCategorySpinner();
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
                Toast.makeText(this, "Thêm món ăn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Initialize Firebase database and storage
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refFood = mDatabase.getReference(Constant.Database.FOOD_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhotoFood = mStorage.getReference().child(Constant.Storage.PHOTO_FOOD);
    }

    // Find views by ID
    private void findViews() {
        edtFoodName = findViewById(R.id.etFoodName);
        edtDescription = findViewById(R.id.etFoodDescription);
        edtPrice = findViewById(R.id.etFoodPrice);
        btnSaveFood = findViewById(R.id.btnAddFood);
        imageFoodPhoto = findViewById(R.id.ivFoodImage);
        spinnerCategory = findViewById(R.id.spinnerFoodCategory);
        edtLocation = findViewById(R.id.etFoodLocation);
    }

    // Setup category spinner
    private void setupCategorySpinner() {
        // Example categories
        categoryList.add("Trái cây");
        categoryList.add("Bánh dân gian");
        categoryList.add("Món ăn đặc sản");

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    // Set listeners for events
    private void setListener() {
        // Listener for image selection
        imageFoodPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(intent, "Select an image"),
                    SELECT_A_PHOTO
            );
        });

        // Listener for saving the food item
        btnSaveFood.setOnClickListener(v -> {
            DatabaseReference refFoodId = refFood.push();
            String id = refFoodId.getKey();

            // Gather food information
            String foodName = edtFoodName.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            double price = 0.0;

            try {
                price = Double.parseDouble(edtPrice.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(AddFoodActivity.this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }
            String location = edtLocation.getText().toString().trim();
            String photoPath = ""; // Store single image as string

            // Create Food object
            Food food = new Food(
                    id,
                    foodName,
                    description,
                    price, // Use double value for price
                    category,
                    location,
                    Constant.Storage.PHOTO_FOOD + refFoodId.getKey()
            );

            // Save food item to Firebase
            refFoodId.setValue(food).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Check if there is an image path
                    if (pathFromDevice != null) {
                        refPhotoFood.child(refFoodId.getKey())
                                .putFile(pathFromDevice)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(AddFoodActivity.this, "Food item saved successfully!", Toast.LENGTH_SHORT).show();
                                        finish(); // End Activity
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(AddFoodActivity.this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(AddFoodActivity.this, "Failed to save food item: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

        // Handle results from selecting an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_A_PHOTO && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                pathFromDevice = data.getData(); // Get the selected image URI
                imageFoodPhoto.setImageURI(pathFromDevice); // Display selected image in ImageView
            }
        }
    }
}
