package com.example.quanbadulichmietvuon.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Notification;
import com.example.quanbadulichmietvuon.views.EditNotificationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Kiểm tra tài khoản người dùng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && "nhi@gmail.com".equals(currentUser.getEmail())) {
            holder.editNotifi.setVisibility(View.VISIBLE);  // Hiển thị nút sửa nếu tài khoản là nhi@gmail.com
        } else {
            holder.editNotifi.setVisibility(View.GONE);  // Ẩn nút sửa nếu không phải tài khoản nhi@gmail.com
        }

        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getTimestamp());

        // Thêm sự kiện cho nút sửa
        holder.editNotifi.setOnClickListener(v -> {
            // Chuyển sang trang sửa khi nhấn nút sửa
            Intent intent = new Intent(context, EditNotificationActivity.class);

            // Truyền thông tin cần thiết qua Intent
            intent.putExtra("notificationId", notification.getId());  // Truyền ID thông báo
            intent.putExtra("notificationTitle", notification.getTitle());  // Truyền tiêu đề
            intent.putExtra("notificationMessage", notification.getMessage());  // Truyền nội dung
            intent.putExtra("notificationTimestamp", notification.getTimestamp());  // Truyền thời gian

            // Chạy Activity sửa
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Thêm tính năng lướt và xóa thông báo
    public void setSwipeToDelete(ItemTouchHelper itemTouchHelper) {
        // Thay vì cast context, em cần lấy RecyclerView từ Activity
        RecyclerView recyclerView = ((Activity) context).findViewById(R.id.notificationRecyclerView);  // Đảm bảo đúng ID của RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, messageTextView, timeTextView;
        ImageView editNotifi;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notificationTitle);
            messageTextView = itemView.findViewById(R.id.notificationMessage);
            timeTextView = itemView.findViewById(R.id.notificationTime);
            editNotifi = itemView.findViewById(R.id.editNotifi);
        }
    }
}
