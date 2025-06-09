package com.example.quanbadulichmietvuon.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.views.EditPostActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.JourneyPost;
import com.example.quanbadulichmietvuon.until.Constant;
import com.example.quanbadulichmietvuon.views.ProfileTourJourneyActivity;
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

import java.util.ArrayList;
import java.util.List;

public class TourJourneyAdapter extends RecyclerView.Adapter<TourJourneyAdapter.ViewHolder> {
    private Context context;
    private List<JourneyPost> postList;
    private OnItemClickListener listener;
    private DatabaseReference refJourney;

    public interface OnItemClickListener {
        void onLikeClick(int position);
        void onCommentClick(int position);
    }

    public TourJourneyAdapter(Context context, List<JourneyPost> postList, OnItemClickListener listener) {
        this.context = context;
        this.postList = postList;
        this.listener = listener;
        this.refJourney = FirebaseDatabase.getInstance().getReference(Constant.Database.POSTS_NODE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tour_journey, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JourneyPost post = postList.get(position);

        holder.tvEmail.setText(post.getUserEmail());
        holder.tvPostTime.setText(post.getPostTime());
        holder.tvDescription.setText(post.getDescription());
        holder.tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // lắng nghe thay đổi số lượng bình luận từ firebase
        DatabaseReference commentRef = FirebaseDatabase.getInstance()
                .getReference("Comments")
                .child(post.getPostId());

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int commentCount = (int) snapshot.getChildrenCount();
                post.setCommentCount(commentCount);
                holder.tvCommentCount.setText(String.valueOf(commentCount)); // cập nhật UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải số lượng bình luận", error.toException());
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference likeRef = refJourney.child(post.getPostId()).child("likes").child(userId);
            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.btnLike.setImageResource(snapshot.exists() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Lỗi khi tải trạng thái like: " + error.getMessage());
                }
            });
        }

        List<String> imageUrls = post.getJourneyImages();
        ImageView[] imageViews = {holder.img1, holder.img2, holder.img3, holder.img4, holder.img5, holder.img6};
        for (int i = 0; i < imageViews.length; i++) {
            if (i < imageUrls.size()) {
                Picasso.get().load(imageUrls.get(i)).into(imageViews[i]);
                imageViews[i].setVisibility(View.VISIBLE);
            } else {
                imageViews[i].setVisibility(View.GONE);
            }
        }

        holder.btnLike.setOnClickListener(v -> listener.onLikeClick(position));
        holder.btnComment.setOnClickListener(v -> listener.onCommentClick(position));

        String postId = post.getPostId();
        String postOwnerEmail = post.getUserEmail();
        String userEmail = user != null ? user.getEmail() : "";
        boolean isAdmin = "nhi@gmail.com".equals(userEmail);
        DatabaseReference postRef = refJourney.child(postId);

        holder.btnMoreOptions.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(view.getContext(), holder.btnMoreOptions);
            popup.getMenuInflater().inflate(R.menu.menu_post_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (userEmail.equals(postOwnerEmail)) {
                        Intent intent = new Intent(view.getContext(), EditPostActivity.class);
                        intent.putExtra("postId", post.getPostId());
                        intent.putExtra("postDescription", post.getDescription());
                        intent.putStringArrayListExtra("postImages", new ArrayList<>(post.getJourneyImages()));
                        view.getContext().startActivity(intent);
                    } else {
                        Toast.makeText(view.getContext(), "Bạn không có quyền chỉnh sửa bài đăng này", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                } else if (item.getItemId() == R.id.action_delete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa bài đăng này không?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                if (userEmail.equals(postOwnerEmail) || isAdmin) {
                                    postRef.removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (post.getJourneyImages() != null) {
                                                for (String imageUrl : post.getJourneyImages()) {
                                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                    imageRef.delete();
                                                }
                                            }
                                            Toast.makeText(context, "Bài đăng đã bị xóa", Toast.LENGTH_SHORT).show();
                                            int currentPosition = holder.getAdapterPosition();
                                            if (currentPosition != RecyclerView.NO_POSITION) {
                                                postList.remove(currentPosition);
                                                notifyItemRemoved(currentPosition);
                                                notifyItemRangeChanged(currentPosition, postList.size());
                                            }
                                        } else {
                                            Toast.makeText(context, "Lỗi khi xóa bài đăng", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "Bạn không có quyền xóa bài đăng này", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                            .show();
                    return true;

                } else if (item.getItemId() == R.id.action_grant_permission) {
                    if (userEmail.equals(postOwnerEmail)) {
                        PopupMenu permissionMenu = new PopupMenu(view.getContext(), holder.btnMoreOptions);
                        permissionMenu.inflate(R.menu.menu_permission_options);

                        permissionMenu.setOnMenuItemClickListener(permissionItem -> {
                            String permission = "";
                            if (permissionItem.getItemId() == R.id.action_public) {
                                permission = "public";
                            } else if (permissionItem.getItemId() == R.id.action_private) {
                                permission = "private";
                            }

                            if (!permission.isEmpty()) {
                                Log.d("DEBUG", "Updating visibility for postId: " + postId);

                                FirebaseDatabase.getInstance()
                                        .getReference("posts")
                                        .child(postId)
                                        .child("visibility")
                                        .setValue(permission)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Cập nhật quyền thành công", Toast.LENGTH_SHORT).show();
                                            if (context instanceof ProfileTourJourneyActivity) {
                                                ((ProfileTourJourneyActivity) context).fetchPosts();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FirebaseError", "Update failed: " + e.getMessage());
                                            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                            return true;
                        });
                        permissionMenu.show();
                    } else {
                        Toast.makeText(view.getContext(), "Bạn không có quyền thay đổi quyền bài đăng này", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

        @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvPostTime, tvDescription, tvLikeCount, tvCommentCount;
        ImageView img1, img2, img3, img4, img5, img6;
        ImageView btnLike, btnComment, btnMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            img1 = itemView.findViewById(R.id.img1);
            img2 = itemView.findViewById(R.id.img2);
            img3 = itemView.findViewById(R.id.img3);
            img4 = itemView.findViewById(R.id.img4);
            img5 = itemView.findViewById(R.id.img5);
            img6 = itemView.findViewById(R.id.img6);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnComment = itemView.findViewById(R.id.btnComment);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}
