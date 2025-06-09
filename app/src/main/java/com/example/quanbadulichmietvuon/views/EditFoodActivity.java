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

import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.until.Constant;
import com.example.quanbadulichmietvuon.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditFoodActivity extends AppCompatActivity {

    private EditText edtFoodName, edtDescription, edtPrice, edtLocation;
    private Spinner spinnerCategory;
    private Button btnSaveFood, btnSelectPhoto;
    private ImageView imagePhotoFood;

    private FirebaseDatabase mDatabase;
    private DatabaseReference refFood;
    private FirebaseStorage mStorage;
    private StorageReference refPhotoFood;

    private String foodId;
    private static final int SELECT_A_PHOTO = 1;
    private Uri pathFromDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food);

        findViews();
        FirebaseApp.initializeApp(this);
        initFirebase();

        // Get food ID from Intent
        Intent intent = getIntent();
        foodId = intent.getStringExtra("foodId");
        if (foodId != null) {
            loadFoodDetails(foodId);
        } else {
            Log.e("EditFoodActivity", "No foodId received!");
        }

        btnSaveFood.setOnClickListener(v -> saveFoodDetails());
        btnSelectPhoto.setOnClickListener(v -> selectPhoto());

        // Initialize spinner
        setupSpinner();
    }

    private void findViews() {
        edtFoodName = findViewById(R.id.etFoodName);
        edtDescription = findViewById(R.id.etFoodDescription);
        edtPrice = findViewById(R.id.etFoodPrice);
        spinnerCategory = findViewById(R.id.spinnerFoodCategory);
        edtLocation = findViewById(R.id.etFoodLocation);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        btnSelectPhoto = findViewById(R.id.btnSelectImage);
        imagePhotoFood = findViewById(R.id.ivFoodImage);
    }



    private void setupSpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Trái cây");
        categories.add("Bánh dân gian");
        categories.add("Món ăn đặc sản");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refFood = mDatabase.getReference(Constant.Database.FOOD_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhotoFood = mStorage.getReference().child(Constant.Storage.PHOTO_FOOD); // Initialize refPhotoFood
    }

    private void loadFoodDetails(String foodId) {
        refFood.child(foodId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Food food = snapshot.getValue(Food.class);
                    if (food != null) {
                        edtFoodName.setText(food.getName());
                        edtDescription.setText(food.getDescription());
                        edtPrice.setText(String.valueOf(food.getPrice()));
                        edtLocation.setText(food.getLocation());

                        // Kiểm tra null trước khi truy cập refPhotoFood
                        if (refPhotoFood != null && food.getId() != null) {
                            refPhotoFood.child(food.getId()).getDownloadUrl()
                                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(imagePhotoFood))
                                    .addOnFailureListener(e -> {
                                        Log.e("EditFoodActivity", "Failed to load image: " + e.getMessage());
                                        imagePhotoFood.setImageResource(R.drawable.a); // Default image
                                    });
                        } else {
                            Log.e("EditFoodActivity", "Photo reference or Food ID is null!");
                            imagePhotoFood.setImageResource(R.drawable.a); // Default image
                        }

                        int categoryPosition = ((ArrayAdapter<String>) spinnerCategory.getAdapter()).getPosition(food.getCategory());
                        spinnerCategory.setSelection(categoryPosition);
                    } else {
                        Log.e("EditFoodActivity", "Food object is null!");
                    }
                } else {
                    Log.e("EditFoodActivity", "Food does not exist!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditFoodActivity", "Error loading food data: " + error.getMessage());
            }
        });
    }

    private void saveFoodDetails() {
        String newFoodName = edtFoodName.getText().toString().trim();
        String newFoodDescription = edtDescription.getText().toString().trim();
        String newFoodLocation = edtLocation.getText().toString().trim();
        String newCategory = spinnerCategory.getSelectedItem().toString();

        // Validate price input
        double newFoodPrice;
        try {
            newFoodPrice = Double.parseDouble(edtPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pathFromDevice != null) {
            StorageReference photoRef = refPhotoFood.child(foodId);
            photoRef.putFile(pathFromDevice).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        saveFoodDataToDatabase(newFoodName, newFoodDescription, newFoodPrice, newFoodLocation, newCategory, photoUrl);
                    });
                } else {
                    Log.e("EditFoodActivity", "Upload failed: " + task.getException().getMessage());
                    Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveFoodDataToDatabase(newFoodName, newFoodDescription, newFoodPrice, newFoodLocation, newCategory, null);
        }
    }

    private void saveFoodDataToDatabase(String foodName, String foodDescription, double foodPrice, String foodLocation, String category, String photoUrl) {
        Map<String, Object> foodUpdates = new HashMap<>();
        foodUpdates.put("name", foodName);
        foodUpdates.put("description", foodDescription);
        foodUpdates.put("price", foodPrice);
        foodUpdates.put("location", foodLocation);
        foodUpdates.put("category", category);

        if (photoUrl != null) {
            foodUpdates.put("imageUrl", photoUrl);
        }


        refFood.child(foodId).updateChildren(foodUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditFoodActivity.this, "Food updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("EditFoodActivity", "Update failed: " + task.getException().getMessage());
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
            imagePhotoFood.setImageURI(pathFromDevice);
        }
    }
}
