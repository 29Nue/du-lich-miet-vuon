package com.example.quanbadulichmietvuon.views;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {
    private EditText edtPost;
    private Button btnSave, btnAddImage,btnAddFeeling, btnAddLocation;
    private GridLayout imageGrid;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();
    private DatabaseReference postRef;
    private StorageReference storageRef;
    private String postId;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        edtPost = findViewById(R.id.edtPost);
        btnSave = findViewById(R.id.btnPost);
        btnAddImage = findViewById(R.id.btnAddImage);
        imageGrid = findViewById(R.id.imageGrid);
        btnAddFeeling = findViewById(R.id.btnAddFeeling);
        btnAddLocation = findViewById(R.id.btnAddLocation);

        postId = getIntent().getStringExtra("postId");
        postRef = FirebaseDatabase.getInstance().getReference(Constant.Database.POSTS_NODE).child(postId);
        storageRef = FirebaseStorage.getInstance().getReference(Constant.Storage.POST_IMAGES).child(postId);

        loadPostData();

        btnSave.setOnClickListener(v -> savePost());
        btnAddImage.setOnClickListener(v -> openImagePicker());
        btnAddFeeling.setOnClickListener(v -> showFeelingDialog());
        btnAddLocation.setOnClickListener(v -> showLocationDialog());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null && selectedImages.size() < 6) {
                            selectedImages.add(imageUri);
                            updateImageGrid();
                        }
                    }
                });
    }
    private final String[] feelings = {
            "😊", "😢", "🤩", "😌",
            "❤️", "🎉", "😓", "😰"
    };
    private void loadPostData() {
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String description = snapshot.child("description").getValue(String.class);
                    edtPost.setText(description);

                    uploadedImageUrls.clear();
                    for (DataSnapshot imgSnapshot : snapshot.child("journeyImages").getChildren()) {
                        String imageUrl = imgSnapshot.getValue(String.class);
                        if (imageUrl != null) uploadedImageUrls.add(imageUrl);
                    }
                    updateImageGrid();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPostActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateImageGrid() {
        List<Uri> allImages = new ArrayList<>();
        for (String url : uploadedImageUrls) allImages.add(Uri.parse(url));
        allImages.addAll(selectedImages);

        // Đảm bảo tối đa 6 ảnh
        while (allImages.size() < 6) {
            allImages.add(null);
        }

        // Gán ảnh vào từng ImageView có sẵn
        for (int i = 0; i < 6; i++) {
            int resId = getResources().getIdentifier("img" + (i + 1), "id", getPackageName());
            ImageView imageView = findViewById(resId);

            if (allImages.get(i) != null) {
                Uri imageUri = allImages.get(i);
                if (imageUri.toString().startsWith("http")) {
                    Picasso.get().load(imageUri.toString()).into(imageView);
                } else {
                    imageView.setImageURI(imageUri);
                }
            } else {
                imageView.setImageDrawable(null); // nếu không có ảnh thì xóa hiển thị cũ
            }
        }
    }

    private void showFeelingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn cảm xúc")
                .setItems(feelings, (dialog, which) -> {
                    String selectedFeeling = feelings[which];
                    String currentText = edtPost.getText().toString();

                    // kiểm tra nếu cảm xúc đã tồn tại thì không thêm lại
                    if (!currentText.contains(selectedFeeling)) {
                        edtPost.setText(currentText.isEmpty() ? selectedFeeling : currentText + " " + selectedFeeling);
                        edtPost.setSelection(edtPost.getText().length());
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

                // kiểm tra nếu đã có vị trí trước đó thì thay thế
                if (currentText.contains("\n📍 Vị trí: ")) {
                    currentText = currentText.replaceAll("\n📍 Vị trí: .*", "").trim();
                }

                // thêm vị trí mới vào cuối bài đăng
                edtPost.setText(currentText + "\n📍 Vị trí: " + location);
                edtPost.setSelection(edtPost.getText().length());
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void savePost() {
        String updatedDesc = edtPost.getText().toString().trim();
        if (updatedDesc.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedImages.isEmpty()) {
            deleteOldImages(() -> uploadImages(() -> updatePostData(updatedDesc)));
        } else {
            updatePostData(updatedDesc);
        }
    }

    private void deleteOldImages(Runnable onComplete) {
        for (String url : uploadedImageUrls) {
            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            oldImageRef.delete();
        }
        uploadedImageUrls.clear();
        onComplete.run();
    }

    private void uploadImages(Runnable onComplete) {
        List<String> newImageUrls = new ArrayList<>();
        for (Uri imageUri : selectedImages) {
            StorageReference imageRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            imageRef.putFile(imageUri)
                    .continueWithTask(task -> imageRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> {
                        newImageUrls.add(uri.toString());
                        if (newImageUrls.size() == selectedImages.size()) {
                            uploadedImageUrls.addAll(newImageUrls);
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditPostActivity.this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show());
        }
    }

    private void updatePostData(String updatedDesc) {
        postRef.child("description").setValue(updatedDesc);
        postRef.child("journeyImages").setValue(uploadedImageUrls)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditPostActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditPostActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                });
    }
}
