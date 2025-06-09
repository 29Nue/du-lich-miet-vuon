package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanbadulichmietvuon.Adapter.CommentAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText edtComment;
    private Button btnSendComment;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private DatabaseReference commentsRef;
    private String postId, currentUserEmail, postOwnerEmail;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        recyclerView = findViewById(R.id.recyclerViewComments);
        edtComment = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // lấy dữ liệu từ intent
        postId = getIntent().getStringExtra("postId");
        currentUserEmail = getIntent().getStringExtra("currentUserEmail");
        postOwnerEmail = getIntent().getStringExtra("postOwnerEmail");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        // tham chiếu đến firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        // khởi tạo adapter
        adapter = new CommentAdapter(this, commentList, currentUserEmail, postOwnerEmail, isAdmin, postId);
        recyclerView.setAdapter(adapter);

        fetchComments();

        btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void fetchComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    if (comment != null) commentList.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendComment() {
        String commentText = edtComment.getText().toString().trim();
        if (commentText.isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail(); // lấy email từ tài khoản đăng nhập
            if (userEmail == null) {
                Toast.makeText(this, "Lỗi: Không lấy được email", Toast.LENGTH_SHORT).show();
                return;
            }

            String commentId = commentsRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            Comment comment = new Comment(commentId, userEmail, commentText, timestamp, postId);
            commentsRef.child(commentId).setValue(comment)
                    .addOnSuccessListener(aVoid -> edtComment.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show());
        }
    }

}