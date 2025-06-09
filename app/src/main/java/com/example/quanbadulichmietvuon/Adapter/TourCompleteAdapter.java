package com.example.quanbadulichmietvuon.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.CompleteTour;
import com.example.quanbadulichmietvuon.views.ReviewActivity;

import java.util.List;

public class TourCompleteAdapter extends RecyclerView.Adapter<TourCompleteAdapter.TourViewHolder> {

    private List<CompleteTour> tourList;

    public TourCompleteAdapter(List<CompleteTour> tourList) {
        this.tourList = tourList;
    }

    @Override
    public TourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour_complete, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TourViewHolder holder, int position) {
        CompleteTour completeTour = tourList.get(position);
        holder.txtTourName.setText(completeTour.getTourName());
        holder.txtStartDate.setText(completeTour.getStartDate());
        holder.txtEndDate.setText(completeTour.getEndDate());
        if (completeTour.getCompleteTime() != null && !completeTour.getCompleteTime().isEmpty()) {
            holder.txtCompleteTime.setText(completeTour.getCompleteTime());
        } else {
            holder.txtCompleteTime.setText("Chưa có thời gian hoàn thành");
        }

        // Sự kiện xóa mục
        holder.btnDelete.setOnClickListener(v -> {
            tourList.remove(position); // Xóa mục ở vị trí hiện tại
            notifyItemRemoved(position); // Cập nhật RecyclerView
            notifyItemRangeChanged(position, tourList.size()); // Cập nhật các vị trí sau khi xóa
        });
        // Sự kiện Đánh giá
        holder.revTourHT.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReviewActivity.class);
            intent.putExtra("tourId", completeTour.getTourId()); // Truyền ID tour
            intent.putExtra("tourName", completeTour.getTourName()); // Truyền tên tour
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public static class TourViewHolder extends RecyclerView.ViewHolder {

        TextView txtTourName, txtStartDate, txtEndDate, txtCompleteTime,revTourHT; ;
        ImageButton btnDelete;

        public TourViewHolder(View itemView) {
            super(itemView);
            txtTourName = itemView.findViewById(R.id.txtTourName);
            txtStartDate = itemView.findViewById(R.id.txtStartDate);
            txtEndDate = itemView.findViewById(R.id.txtEndDate);
           // txtCompleteTime = itemView.findViewById(R.id.txtCompleteTime);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Nút xóa
            revTourHT = itemView.findViewById(R.id.revTourHT);
        }
    }
}
