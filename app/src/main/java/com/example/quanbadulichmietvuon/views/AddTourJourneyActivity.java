package com.example.quanbadulichmietvuon.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.JourneyPost;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTourJourneyActivity extends AppCompatActivity {
    private EditText edtPost;
    private Button btnPost, btnAddImage, btnAddFeeling, btnAddLocation;
    private ImageView[] imageViews = new ImageView[6];
    private List<Uri> imageUris = new ArrayList<>();
    private FirebaseDatabase mDatabase;
    private DatabaseReference refPosts;
    private FirebaseStorage mStorage;
    private Spinner spinnerVisibility;
    private String[] visibilityOptions = {"Công khai", "Chỉ mình tôi"};
    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour_journey);

        findViews();
        setListeners();
        initFirebase();
        requestStoragePermission();

        // tạo adapter cho spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, visibilityOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisibility.setAdapter(adapter);
    }

    private void findViews() {
        spinnerVisibility = findViewById(R.id.spinnerVisibility);
        edtPost = findViewById(R.id.edtPost);
        btnPost = findViewById(R.id.btnPost);
        btnAddFeeling = findViewById(R.id.btnAddFeeling);
        btnAddLocation = findViewById(R.id.btnAddLocation);
        btnAddImage = findViewById(R.id.btnAddImage);
        for (int i = 0; i < 6; i++) {
            int resID = getResources().getIdentifier("img" + (i + 1), "id", getPackageName());
            imageViews[i] = findViewById(resID);
        }
    }
    private final String[] feelings = {
            "😊", "😢", "🤩", "😌",
            "❤️", "Tuyệt vời 🎉", "😓", "😰"
    };

    private void setListeners() {
        btnAddImage.setOnClickListener(v -> selectImage());
        btnPost.setOnClickListener(v -> savePost());
        btnAddFeeling.setOnClickListener(v -> showFeelingDialog());
        btnAddLocation.setOnClickListener(v -> showLocationDialog());
    }

    private void showFeelingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn cảm xúc")
                .setItems(feelings, (dialog, which) -> {
                    String currentText = edtPost.getText().toString();
                    String selectedFeeling = feelings[which];

                    if (!currentText.contains(selectedFeeling)) {
                        if (!currentText.isEmpty()) {
                            currentText += " ";
                        }
                        currentText += selectedFeeling;
                        edtPost.setText(currentText);
                        edtPost.setSelection(currentText.length());
                    }
                })
                .show();
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập vị trí");
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String location = input.getText().toString().trim();
            if (!location.isEmpty()) {
                String currentText = edtPost.getText().toString();
                edtPost.setText(currentText + "\n📍 Vị trí: " + location);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void selectImage() {
        if (imageUris.size() >= 6) {
            Toast.makeText(this, "Bạn chỉ có thể thêm tối đa 6 ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (imageUris.size() < 6) {
                imageUris.add(data.getData());
                imageViews[imageUris.size() - 1].setImageURI(data.getData());
            }
        }
    }

    private void savePost() {
        String content = edtPost.getText().toString().trim();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String postTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // lấy trạng thái công khai hoặc riêng tư từ spinner
        String selectedVisibility = spinnerVisibility.getSelectedItem().toString();
        String visibility = selectedVisibility.equals("Công khai") ? "public" : "private";


        if (imageUris.isEmpty()) {
            savePostToDatabase(new ArrayList<>(), userEmail, postTime, content, visibility);
            return;
        }

        List<String> imageUrls = new ArrayList<>();
        final int[] uploadedCount = {0};
        for (Uri uri : imageUris) {
            uploadImageToFirebase(uri, imageUrls, uploadedCount, userEmail, postTime, content, visibility);
        }
    }


    private void uploadImageToFirebase(Uri imageUri, List<String> imageUrls, int[] uploadedCount, String userEmail, String postTime, String description, String visibility) {
        StorageReference refPostImage = mStorage.getReference().child(Constant.Storage.POST_IMAGES + System.currentTimeMillis());
        refPostImage.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                refPostImage.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    uploadedCount[0]++;
                    if (uploadedCount[0] == imageUris.size()) {
                        savePostToDatabase(imageUrls, userEmail, postTime, description, visibility);
                    }
                })
        ).addOnFailureListener(e -> {
            Toast.makeText(this, "Tải ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void savePostToDatabase(List<String> imageUrls, String userEmail, String postTime, String description, String visibility) {
        DatabaseReference refPostId = refPosts.push();
        String id = refPostId.getKey();

        JourneyPost post = new JourneyPost(id, userEmail, postTime, description, imageUrls, 0, 0, visibility);
        post.setVisibility(visibility); // lưu trạng thái công khai hoặc riêng tư

        refPostId.setValue(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi đăng bài: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refPosts = mDatabase.getReference(Constant.Database.POSTS_NODE);
        mStorage = FirebaseStorage.getInstance();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
}