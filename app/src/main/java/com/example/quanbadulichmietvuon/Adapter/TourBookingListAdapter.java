package com.example.quanbadulichmietvuon.Adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.until.Constant;
import com.example.quanbadulichmietvuon.views.TourBookingDetailActivity;
import com.example.quanbadulichmietvuon.modules.TourBooking;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TourBookingListAdapter extends RecyclerView.Adapter<TourBookingListAdapter.ViewHolder> {

    private List<TourBooking> tourList;
    private DatabaseReference databaseReference;

    public TourBookingListAdapter(List<TourBooking> tourList) {
        this.tourList = tourList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference(Constant.Database.TOURBOOKINGS_NODE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_booking_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourBooking tour = tourList.get(position);
        holder.tvCustomerName.setText("Khách hàng: " + tour.getCustomerName());
        holder.tvStartDate.setText("Ngày khởi hành: " + tour.getStartDate());
        holder.tvGuests.setText("Số khách: " + tour.getGuests() + " người");

        StringBuilder places = new StringBuilder("Điểm đến: ");
        if (tour.getPlaces() != null) {
            for (String place : tour.getPlaces()) {
                places.append(place).append(", ");
            }
        }
        holder.tvPlaces.setText(places.toString().replaceAll(", $", ""));

        holder.tvCreatedTime.setText(tour.getCreatedTime());

        holder.txtDeleteTour.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xác Nhận Xóa")
                    .setMessage("Cậu có chắc muốn xóa đặt tour này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (tour.getBookingId() != null) {
                            databaseReference.child(tour.getBookingId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        tourList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(v.getContext(), "Đã xóa đặt tour", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TourBookingDetailActivity.class);
            intent.putExtra("bookingId", tour.getBookingId()); // chỉ truyền bookingId
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvStartDate, tvGuests, tvPlaces, txtDeleteTour, tvCreatedTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvGuests = itemView.findViewById(R.id.tvGuests);
            tvPlaces = itemView.findViewById(R.id.tvPlaces);
            txtDeleteTour = itemView.findViewById(R.id.txtDeleteTour);
            tvCreatedTime = itemView.findViewById(R.id.tvCreatedTime);
        }
    }
}
