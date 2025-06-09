package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Notification;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class EditNotificationActivity extends AppCompatActivity {

    private EditText titleEditText, messageEditText;
    private Button saveButton;
    private String notificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notification);

        titleEditText = findViewById(R.id.editNotificationTitle);
        messageEditText = findViewById(R.id.editNotificationMessage);
        saveButton = findViewById(R.id.saveNotificationButton);

        // Nhận ID thông báo từ Intent
        notificationId = getIntent().getStringExtra("notificationId");

        if (notificationId == null || notificationId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông báo cần sửa", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại trang trước
            return;
        }

        // Lấy thông tin thông báo từ Firebase
        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference(Constant.Database.NOTIFICATION_NODE).child(notificationId);
        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        titleEditText.setText(notification.getTitle());
                        messageEditText.setText(notification.getMessage());
                    }
                } else {
                    Toast.makeText(EditNotificationActivity.this, "Thông báo không tồn tại", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditNotificationActivity.this, "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện lưu thông tin đã sửa
        saveButton.setOnClickListener(v -> {
            String updatedTitle = titleEditText.getText().toString().trim();
            String updatedMessage = messageEditText.getText().toString().trim();

            if (updatedTitle.isEmpty() || updatedMessage.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo đối tượng Notification mới với title và message sửa đổi, giữ nguyên id và timestamp
            Notification updatedNotification = new Notification(
                    notificationId, // giữ nguyên ID
                    updatedTitle,   // title mới
                    updatedMessage, // message mới
                    "timestamp"     // Giữ nguyên timestamp cũ (có thể sử dụng giá trị cũ hoặc lấy thời gian hiện tại nếu cần)
            );

            // Cập nhật thông báo trong Firebase
            notificationRef.setValue(updatedNotification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thông báo đã được cập nhật", Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại trang trước
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật thông báo", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
