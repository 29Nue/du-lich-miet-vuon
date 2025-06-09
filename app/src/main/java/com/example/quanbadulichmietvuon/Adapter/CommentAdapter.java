package com.example.quanbadulichmietvuon.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Comment;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<Comment> commentList;
    private final String currentUserEmail;
    private final String postOwnerEmail;
    private final boolean isAdmin;
    private final String postId;
    private final Context context;

    public CommentAdapter(Context context, List<Comment> commentList, String currentUserEmail, String postOwnerEmail, boolean isAdmin, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserEmail = currentUserEmail;
        this.postOwnerEmail = postOwnerEmail;
        this.isAdmin = isAdmin;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvUserEmail.setText(comment.getUserEmail());
        holder.tvCommentText.setText(comment.getCommentText());
        holder.tvTimestamp.setText(formatTimestamp(comment.getTimestamp()));

        loadUserAvatar(comment.getUserEmail(), holder.imgUserAvatar);

        holder.btnMoreOptions.setOnClickListener(v -> showPopupMenu(v, comment, position));
    }
    private void loadUserAvatar(String userEmail, ImageView imgUserAvatar) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constant.Database.USER_NODE);

        userRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String avatarUrl = userSnap.child("photo").getValue(String.class);
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get()
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.a)
                                        .error(R.drawable.a)
                                        .into(imgUserAvatar);
                            } else {
                                imgUserAvatar.setImageResource(R.drawable.a);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void showPopupMenu(View view, Comment comment, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_item_comment, popupMenu.getMenu());

        MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.action_delete);

        String userEmail = (comment.getUserEmail() != null) ? comment.getUserEmail() : "";
        String currentUser = (currentUserEmail != null) ? currentUserEmail : "";
        String postOwner = (postOwnerEmail != null) ? postOwnerEmail : "";

        boolean canDelete = userEmail.equals(currentUser) || postOwner.equals(currentUser) || isAdmin;

        Log.d("DEBUG", "UserEmail: " + userEmail);
        Log.d("DEBUG", "CurrentUserEmail: " + currentUser);
        Log.d("DEBUG", "PostOwnerEmail: " + postOwner);
        Log.d("DEBUG", "IsAdmin: " + isAdmin);
        Log.d("DEBUG", "CanDelete: " + canDelete);

        deleteItem.setVisible(canDelete);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(comment, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(Comment comment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa bình luận này không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> deleteComment(comment, position));
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteComment(Comment comment, int position) {
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId).child(comment.getCommentId());
        commentRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (position >= 0 && position < commentList.size()) {
                        commentList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi: Không tìm thấy bình luận", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi xóa", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvCommentText, tvTimestamp;
        ImageView btnMoreOptions, imgUserAvatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
        }
    }


    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
