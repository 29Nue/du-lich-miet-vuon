package com.example.quanbadulichmietvuon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanbadulichmietvuon.views.ChatboxDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatboxAdminActivity extends AppCompatActivity {
    private ListView listViewUsers;
    private DatabaseReference chatRef;
    private List<String> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox_admin);

        listViewUsers = findViewById(R.id.listViewUsers);
        chatRef = FirebaseDatabase.getInstance().getReference("chats");

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    userList.add(userSnap.getKey()); // lấy danh sách username
                }
                listViewUsers.setAdapter(new ArrayAdapter<>(ChatboxAdminActivity.this, android.R.layout.simple_list_item_1, userList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        listViewUsers.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUser = userList.get(position);
            Intent intent = new Intent(ChatboxAdminActivity.this, ChatboxDetailActivity.class);
            intent.putExtra("username", selectedUser);
            startActivity(intent);
        });
    }
}
