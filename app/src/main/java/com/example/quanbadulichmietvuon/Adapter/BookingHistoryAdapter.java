package com.example.quanbadulichmietvuon.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.BookingTour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.example.quanbadulichmietvuon.views.TourCancelActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private Context context;
    private List<BookingTour> bookingList;
    private DatabaseReference databaseReference;

    public BookingHistoryAdapter(Context context, List<BookingTour> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference(Constant.Database.BOOKTOUR_NODE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingTour booking = bookingList.get(position);
        holder.txtTourName.setText(booking.getTourName());
        holder.txtDate.setText(booking.getStartDate() + " - " + booking.getEndDate());
        holder.txtCustomer.setText("Customer: " + booking.getCustomerName());
        holder.txtPhone.setText("Phone: " + booking.getCustomerPhone());
        holder.txtCount.setText("Guests: " + booking.getCustomerCount());
        holder.txtCreatedTime.setText(booking.getCreatedTime());

        // set trạng thái ban đầu
        holder.btnComplete.setEnabled(!booking.isCompleted());
        holder.btnCancel.setEnabled(!booking.isCompleted());

        if (booking.isCompleted()) {
            holder.btnComplete.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
            holder.btnComplete.setText("Đã Hoàn Thành");
        } else {
            holder.btnComplete.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray));
            holder.btnComplete.setText("Hoàn Thành");
        }

        if (booking.isCompleted()) {
            holder.btnCancel.setEnabled(false);
            holder.btnCancel.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red)); // đổi màu đỏ nếu đã hủy
        } else {
            if (canCancelTour(booking.getCreatedTime())) {
                holder.btnCancel.setEnabled(true);
                holder.btnCancel.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gray)); // màu xám nếu chưa hủy
            } else {
                holder.btnCancel.setEnabled(false);
                holder.btnCancel.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.black));
            }
        }

        // xử lý khi nhấn hoàn thành
        holder.btnComplete.setOnClickListener(v -> {
            holder.btnComplete.setEnabled(false);
            holder.btnCancel.setEnabled(false);
            booking.setCompleted(true);
            notifyItemChanged(position);

            if (booking.getId() != null) {
                databaseReference.child(booking.getId()).child("completed").setValue(true);
            }
        });

        // xử lý khi nhấn hủy
        holder.btnCancel.setOnClickListener(v -> {
            if (canCancelTour(booking.getCreatedTime())) {
                holder.btnCancel.setEnabled(false);
                holder.btnComplete.setEnabled(false);
                Intent intent = new Intent(context, TourCancelActivity.class);
                intent.putExtra("tourName", booking.getTourName());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Bạn không thể hủy - hãy liên hệ admin", Toast.LENGTH_SHORT).show();
            }
        });

        // xử lý khi nhấn xóa
        holder.txtDeleteTour.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xác Nhận Xóa")
                    .setMessage("Cậu có chắc muốn xóa đặt tour này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (booking.getId() != null) {
                            databaseReference.child(booking.getId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        bookingList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(v.getContext(), "Đã xóa đặt tour", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(v.getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTourName, txtDate, txtCustomer, txtPhone, txtCount, txtDeleteTour, txtCreatedTime;
        Button btnComplete, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTourName = itemView.findViewById(R.id.txtTourName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtCustomer = itemView.findViewById(R.id.txtCustomer);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtCount = itemView.findViewById(R.id.txtCount);
            txtDeleteTour = itemView.findViewById(R.id.txtDeleteTour);
            txtCreatedTime = itemView.findViewById(R.id.txtCreatedTime);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    private boolean canCancelTour(String createdTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date createdDate = sdf.parse(createdTime);
            long timeDiff = System.currentTimeMillis() - createdDate.getTime();
            return timeDiff <= 30 * 60 * 1000; // dưới 30 phút thì hủy được
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}

