package com.example.quanbadulichmietvuon.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.AdminActivity;
import com.example.quanbadulichmietvuon.FavoriteActivity;
import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.LocaleHelper;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.UUID;

public class UserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    public static final String PHOTO_USER = "photo/user/";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    private TextView userName, userEmail,txtHTCN, txtEditPass, logoutButton, txtTour, txtTourHT, txtChangeLanguage,notificationBadge,txtLienHe, txtDanhGia, txtYeuThich;
    private ImageView profileImage, editProfile,notificationBell;
    private Button editAdminButton; // Edit Admin button
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_user);


        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constant.Database.USER_NODE);
        storageReference = FirebaseStorage.getInstance().getReference();

        // UI Elements
        txtHTCN = findViewById(R.id.txtHTCN);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);
        txtTour = findViewById(R.id.txtTour);
        txtTourHT = findViewById(R.id.txtTourHT);
        txtChangeLanguage = findViewById(R.id.txtChangeLanguage);
        txtEditPass = findViewById(R.id.txtEditPass);
        editAdminButton = findViewById(R.id.btn_edit_admin);
        txtLienHe = findViewById(R.id.txtLienHe);
        txtDanhGia = findViewById(R.id.txtDanhGia);
        txtYeuThich = findViewById(R.id.txtYeuThich);


        // Show the Edit Admin button only for "nhi@gmail.com"
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            if (user.getEmail().equals("nhi@gmail.com")) {
                editAdminButton.setVisibility(View.VISIBLE); // Show button for admin
            }
        }

        // Set click listener for Edit Admin button
        editAdminButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Set click listener for Edit Password
        txtEditPass.setOnClickListener(v -> showEditPasswordDialog());

        txtHTCN.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // đã đăng nhập -> mở trang hành trình cá nhân
                Intent intent = new Intent(UserActivity.this, ProfileTourJourneyActivity.class);
                startActivity(intent);
            } else {
                // chưa đăng nhập -> chuyển qua trang đăng nhập
                Intent intent = new Intent(UserActivity.this, SigninActivity.class);
                startActivity(intent);
                Toast.makeText(UserActivity.this, "Bạn cần đăng nhập để xem hành trình cá nhân!", Toast.LENGTH_SHORT).show();
            }
        });


        // Set click listeners for other UI actions
        txtTour.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, BookingHistoryActivity.class);
            startActivity(intent);
        });
        txtTourHT.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, TourBookingListActivity.class);
            startActivity(intent);
        });

        // sự kiện khi click vào "Liên hệ tư vấn"
        txtLienHe.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, ContactActivity.class);
            startActivity(intent);
        });

        // sự kiện khi click vào "Đánh giá"
        txtDanhGia.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, ReviewActivity.class);
            startActivity(intent);
        });

        // sự kiện khi click vào "Danh sách yêu thích"
        txtYeuThich.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, FavoriteActivity.class);
            startActivity(intent);
        });
        txtChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        // Fetch user data from Firebase
        if (user != null) {
            String userId = user.getUid();
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("photo").getValue(String.class);

                        userName.setText(name);
                        userEmail.setText(email);

                        // Load the profile image if available
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl).into(profileImage);
                        }

                        // Set listener for profile image
                        profileImage.setOnClickListener(v -> openFileChooser());

                        // Set listener for user name
                        userName.setOnClickListener(v -> showEditNameDialog());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UserActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logout functionality
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(UserActivity.this, SigninActivity.class));
            finish();
        });
    }
    private void showLanguageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Ngôn Ngữ");
        String[] languages = {"Tiếng Việt", "English"};
        builder.setItems(languages, (dialog, which) -> {
            String selectedLang = (which == 0) ? "vi" : "en";
            LocaleHelper.saveLanguage(this, selectedLang);

            // restart lại app để đổi ngôn ngữ toàn bộ
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.show();
    }

    private void showEditPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        // Layout chứa hai ô nhập
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText currentPassword = new EditText(this);
        currentPassword.setHint("Mật khẩu hiện tại");
        currentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPassword);

        final EditText newPassword = new EditText(this);
        newPassword.setHint("Mật khẩu mới (ít nhất 8 ký tự, chữ hoa, số, ký tự đặc biệt)");
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPassword);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String currentPass = currentPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(UserActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!isStrongPassword(newPass)) {
                Toast.makeText(UserActivity.this, "Mật khẩu mới phải có ít nhất 8 ký tự, chữ hoa, chữ thường, số và ký tự đặc biệt", Toast.LENGTH_LONG).show();
            } else {
                changeUserPassword(currentPass, newPass);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean isStrongPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(pattern);
    }


    private void changeUserPassword(String currentPass, String newPass) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        // Re-authenticate user before password change
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update password
                user.updatePassword(newPass).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(UserActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserActivity.this, "Đổi mật khẩu thất bại: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(UserActivity.this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tên của bạn là ?");

        // EditText for entering the new name
        final EditText input = new EditText(this);
        input.setText(userName.getText().toString());
        builder.setView(input);

        // "Save" button to update the name
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                saveUserName(newName);
            } else {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        // "Cancel" button
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Save the new name in Firebase
    private void saveUserName(String newName) {
        String userId = auth.getCurrentUser().getUid();
        databaseReference.child(userId).child("username").setValue(newName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userName.setText(newName);
                Toast.makeText(UserActivity.this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserActivity.this, "Không thể cập nhật tên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(profileImage);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang tải ảnh lên...");
            progressDialog.show();

            String userId = auth.getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child(PHOTO_USER + userId + "/" + UUID.randomUUID().toString());

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    databaseReference.child(userId).child("photo").setValue(downloadUrl);

                    progressDialog.dismiss();
                    Toast.makeText(UserActivity.this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(UserActivity.this, "Lỗi khi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
