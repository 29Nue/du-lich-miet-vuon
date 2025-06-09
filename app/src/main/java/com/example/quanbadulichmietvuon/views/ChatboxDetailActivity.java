package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.MessageAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatboxDetailActivity extends AppCompatActivity {
    private TextView textUser;
    private EditText editAdminMessage;
    private Button buttonAdminSend;
    private RecyclerView recyclerViewAdminMessages;
    private DatabaseReference chatRef;
    private String username;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox_detail);

        textUser = findViewById(R.id.textUser);
        editAdminMessage = findViewById(R.id.editAdminMessage);
        buttonAdminSend = findViewById(R.id.buttonAdminSend);
        recyclerViewAdminMessages = findViewById(R.id.recyclerViewAdminMessages);

        username = getIntent().getStringExtra("username");
        if (username == null) {
            finish();
            return;
        }

        textUser.setText("Chat với " + username);
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(username);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, "admin");

        recyclerViewAdminMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdminMessages.setAdapter(messageAdapter);

        buttonAdminSend.setOnClickListener(v -> sendAdminMessage());

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewAdminMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendAdminMessage() {
        String messageText = editAdminMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        String messageId = chatRef.push().getKey();
        if (messageId == null) return;

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", "admin");
        messageData.put("message", messageText);
        messageData.put("timestamp", System.currentTimeMillis());

        chatRef.child(messageId).setValue(messageData);
        editAdminMessage.setText("");
    }
}
