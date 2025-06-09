package com.example.quanbadulichmietvuon.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Contact;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactActivity extends AppCompatActivity {

    private static final String CONTACT_NODE = "contact";

    private EditText editName, editEmail, editMessage;
    private Button btnSend,btnChatBox;
    private DatabaseReference contactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialize Firebase Database reference
        contactRef = FirebaseDatabase.getInstance().getReference(CONTACT_NODE);

        // Link views from layout
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnChatBox = findViewById(R.id.btnChatBox);

        // Handle Send button click event
        btnSend.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String message = editMessage.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                Toast.makeText(ContactActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            } else {
                saveContactInfoToFirebase(name, email, message);
            }
        });

        btnChatBox.setOnClickListener(v -> {
            Intent intent = new Intent(ContactActivity.this, ChatboxUserActivity.class);
            startActivity(intent);
        });
    }

    private void saveContactInfoToFirebase(String name, String email, String message) {
        // Create a unique ID for each contact entry
        String contactId = contactRef.push().getKey();

        if (contactId != null) {
            // Create a new Contact object
            Contact contact = new Contact(name, email, message);

            // Save the contact information under the generated ID
            contactRef.child(contactId).setValue(contact)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ContactActivity.this, "Thông tin đã được gửi.", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ContactActivity.this, "Gửi thông tin thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void clearFields() {
        editName.setText("");
        editEmail.setText("");
        editMessage.setText("");
    }
}
