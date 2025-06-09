package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Notification;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityAddNotification extends AppCompatActivity {

    private EditText editTitle, editMessage;
    private Button buttonSaveNotification;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notification);

        editTitle = findViewById(R.id.editTitle);
        editMessage = findViewById(R.id.editMessage);
        buttonSaveNotification = findViewById(R.id.buttonSaveNotification);

        // initialize firebase reference
        notificationRef = FirebaseDatabase.getInstance().getReference(Constant.Database.NOTIFICATION_NODE);

        buttonSaveNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotification();
            }
        });
    }

    private void saveNotification() {
        String title = editTitle.getText().toString().trim();
        String message = editMessage.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // generate timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // tạo một node mới trên firebase và lấy id
        String id = notificationRef.push().getKey();

        if (id != null) {
            // create notification object with id
            Notification notification = new Notification(id, title, message, timestamp);

            // lưu đối tượng notification vào firebase
            notificationRef.child(id).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ActivityAddNotification.this, "Notification added successfully", Toast.LENGTH_SHORT).show();
                        // clear input fields
                        editTitle.setText("");
                        editMessage.setText("");
                    })
                    .addOnFailureListener(e -> Toast.makeText(ActivityAddNotification.this, "Failed to add notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Failed to generate ID", Toast.LENGTH_SHORT).show();
        }
    }
}
