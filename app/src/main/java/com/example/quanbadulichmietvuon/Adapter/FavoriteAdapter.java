package com.example.quanbadulichmietvuon.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.FavoriteItem;
import com.squareup.picasso.Picasso;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private Context context;
    private List<FavoriteItem> favoriteList;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteItem item);
    }

    public FavoriteAdapter(Context context, List<FavoriteItem> favoriteList, OnFavoriteClickListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = favoriteList.get(position);
        holder.txtTitle.setText(item.getName());

        // xử lý trường hợp không có giá hoặc địa điểm
        holder.txtPrice.setText(item.getPrice() != null ? item.getPrice() : "Không có giá");
        holder.txtLocation.setText(item.getLocation() != null ? item.getLocation() : "Không có địa điểm");

        // load ảnh từ url firebase
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).placeholder(R.drawable.a).into(holder.imgItem);
        } else {
            holder.imgItem.setImageResource(R.drawable.a);
        }

        holder.imgFavorite.setOnClickListener(v -> listener.onFavoriteClick(item));
    }

    // cập nhật danh sách hiển thị mà không thay đổi danh sách gốc
    public void updateList(List<FavoriteItem> newList) {
        this.favoriteList.clear();
        this.favoriteList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, imgFavorite;
        TextView txtTitle, txtPrice, txtLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
