package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.NotificationAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Notification;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notificationRecyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        findViewById(R.id.backButton).setOnClickListener(v -> finish()); // quay lại

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);

        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationRecyclerView.setAdapter(adapter);

        // reference to "Notifications" node in firebase
        notificationRef = FirebaseDatabase.getInstance().getReference(Constant.Database.NOTIFICATION_NODE);

        loadNotifications();

        // gán ItemTouchHelper vào RecyclerView
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                Notification notification = notificationList.get(position);

                // hiển thị dialog với hai nút "xóa" và "không"
                new AlertDialog.Builder(NotificationActivity.this)
                        .setMessage("Bạn có chắc chắn muốn xóa thông báo này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // xóa thông báo trong Firebase
                            notificationRef.child(notification.getId())
                                    .removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        notificationList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        Toast.makeText(NotificationActivity.this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(NotificationActivity.this, "Lỗi xóa thông báo", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            // hoàn tác swipe
                            adapter.notifyItemChanged(position);
                        })
                        .setCancelable(false) // không cho đóng dialog khi nhấn ra ngoài
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notificationRecyclerView); // áp dụng ItemTouchHelper cho RecyclerView
    }

    private void loadNotifications() {
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear(); // clear old data
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification notification = data.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }
                adapter.notifyDataSetChanged(); // update adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NotificationActivity", "Database error: ", error.toException());
            }
        });
    }
}
