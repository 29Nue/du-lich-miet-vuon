package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.MessageAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatboxUserActivity extends AppCompatActivity {
    private EditText editMessage;
    private Button buttonSend;
    private RecyclerView recyclerViewMessages;
    private DatabaseReference chatRef;
    private String userId;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox_user);

        // kiểm tra nếu chưa đăng nhập thì không vào chatbox
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        // lấy userId từ firebase auth
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(userId);

        // ánh xạ view
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // khởi tạo danh sách tin nhắn
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, userId);

        // setup recyclerView
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // nút gửi tin nhắn
        buttonSend.setOnClickListener(v -> sendMessage());

        // lắng nghe tin nhắn từ firebase
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
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

    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        String messageId = chatRef.push().getKey();
        if (messageId == null) return;

        // Lấy email của người dùng
        String userEmail = FirebaseAuth.getInstance().getCurrentUser ().getEmail();

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("sender", userEmail); // Sử dụng email thay vì userId
        messageData.put("message", messageText);
        messageData.put("timestamp", System.currentTimeMillis());

        chatRef.child(messageId).setValue(messageData);
        editMessage.setText("");
    }
}
