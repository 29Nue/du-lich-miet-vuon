package com.example.quanbadulichmietvuon.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.modules.JourneyPost;
import com.example.quanbadulichmietvuon.Adapter.TourJourneyAdapter;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ProfileTourJourneyActivity extends AppCompatActivity implements TourJourneyAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private TourJourneyAdapter adapter;
    private List<JourneyPost> postList;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refJourney, userRef;
    private StorageReference refPhoto;
    private FirebaseStorage mStorage;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_tour_journey);

        initFirebase();
        initUI();
        fetchPosts();
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refJourney = mDatabase.getReference(Constant.Database.POSTS_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhoto = mStorage.getReference().child(Constant.Storage.POST_IMAGES);
    }

    private void initUI() {
        recyclerView = findViewById(R.id.postsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new TourJourneyAdapter(this, postList, this);
        recyclerView.setAdapter(adapter);

        ImageView imgAddPost = findViewById(R.id.imgAddPost);
        EditText edtPost = findViewById(R.id.edtPost);
        imgAddPost.setOnClickListener(v -> goToAddJourney());
        edtPost.setOnClickListener(v -> goToAddJourney());
    }

    private void goToAddJourney() {
        Intent intent = new Intent(ProfileTourJourneyActivity.this, AddTourJourneyActivity.class);
        startActivity(intent);
    }
    public void fetchPosts() {
        refJourney.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) return;

                String userEmail = currentUser.getEmail();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    JourneyPost post = dataSnapshot.getValue(JourneyPost.class);
                    if (post != null) {
                        String visibility = dataSnapshot.child("visibility").getValue(String.class);

                        // kiểm tra quyền hiển thị (chỉ hiển thị bài công khai hoặc của chính người đăng)
                        if ("public".equals(visibility) || (userEmail != null && userEmail.equals(post.getUserEmail()))) {
                            // lấy danh sách ảnh trực tiếp từ firebase database
                            List<String> imageUrls = new ArrayList<>();
                            DataSnapshot imagesSnapshot = dataSnapshot.child("journeyImages");
                            for (DataSnapshot imgSnap : imagesSnapshot.getChildren()) {
                                String imageUrl = imgSnap.getValue(String.class);
                                if (imageUrl != null) {
                                    imageUrls.add(imageUrl);
                                }
                            }
                            post.setJourneyImages(imageUrls); // set danh sách ảnh
                            postList.add(post);
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // cập nhật giao diện sau khi load xong
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu: " + error.getMessage());
            }
        });
    }


    @Override
    public void onLikeClick(int position) {
        JourneyPost post = postList.get(position);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        DatabaseReference postRef = refJourney.child(post.getPostId());
        DatabaseReference likeRef = postRef.child("likes").child(userId);

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // nếu đã like thì bỏ like
                    likeRef.removeValue();
                    post.setLikeCount(post.getLikeCount() - 1);
                } else {
                    // nếu chưa like thì thêm like
                    likeRef.setValue(true);
                    post.setLikeCount(post.getLikeCount() + 1);
                }
                adapter.notifyItemChanged(position);
                postRef.child("likeCount").setValue(post.getLikeCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi xử lý like: " + error.getMessage());
            }
        });
    }
    @Override
    public void onCommentClick(int position) {
        JourneyPost post = postList.get(position);
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("postId", post.getPostId());
        startActivity(intent);

        // lắng nghe thay đổi số lượng bình luận từ firebase
        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("Comments")
                .child(post.getPostId());

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int commentCount = (int) snapshot.getChildrenCount();
                post.setCommentCount(commentCount);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải số lượng bình luận", error.toException());
            }
        });
    }

}
