package com.example.quanbadulichmietvuon.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.FavoriteItem;
import com.example.quanbadulichmietvuon.modules.Tour;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.ViewHolder> {
    private final int resourceId;
    private ArrayList<Tour> tours;
    private String userId;
    private DatabaseReference favoritesRef;
    private OnTourClickListener onTourClickListener;
    private List<Tour> tourList;

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    public TourAdapter(int resourceId, ArrayList<Tour> tours, String userId) {
        this.resourceId = resourceId;
        this.tours = tours;
        this.userId = userId;
        this.favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tour tour = tours.get(position);

        // set thông tin tour
        holder.txtDurationDaysNights.setText(tour.getDurationDays() + " ngày - " + tour.getDurationNights() + " đêm");
        holder.textName.setText(tour.getTourName());
        holder.txtLocation.setText(tour.getDestinations());
        holder.txtPrice.setText(String.format("%s VND/Người", tour.getPrice()));

        // load hình ảnh từ firebase storage
        String photoPath = tour.getPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            StorageReference refPhoto = FirebaseStorage.getInstance().getReference().child(photoPath);
            refPhoto.getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Picasso.get().load(task.getResult()).into(holder.imagePhoto);
                } else {
                    holder.imagePhoto.setImageResource(R.drawable.a); // ảnh mặc định nếu load thất bại
                }
            });
        } else {
            holder.imagePhoto.setImageResource(R.drawable.a);
        }

        // kiểm tra trạng thái yêu thích
        checkFavoriteStatus(tour.getId(), holder.imgBtnFavorite);

        // sự kiện khi nhấn vào nút yêu thích
        holder.imgBtnFavorite.setOnClickListener(v -> toggleFavorite(tour, holder.imgBtnFavorite));

        // xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(view -> {
            if (onTourClickListener != null) {
                onTourClickListener.onTourClick(tour);
            }
        });

        // mở google maps khi nhấn vào location
        holder.txtLocation.setOnClickListener(view -> openGoogleMaps(view, holder.txtLocation.getText().toString().trim()));
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    public void setOnTourClickListener(OnTourClickListener listener) {
        this.onTourClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imagePhoto;
        private TextView textName, txtDurationDaysNights, txtPrice, txtLocation;
        private ImageButton imgBtnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
            textName = itemView.findViewById(R.id.textName);
            txtDurationDaysNights = itemView.findViewById(R.id.txtDurationDaysNights);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            imgBtnFavorite = itemView.findViewById(R.id.ImgBtnFavorite);
        }
    }

    // kiểm tra trạng thái yêu thích
    private void checkFavoriteStatus(String tourId, ImageButton imgBtnFavorite) {
        favoritesRef.child(tourId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    imgBtnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    imgBtnFavorite.setImageResource(R.drawable.ic_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // lỗi firebase
            }
        });
    }

    // thêm/xóa tour khỏi danh sách yêu thích
    private void toggleFavorite(Tour tour, ImageButton imgBtnFavorite) {
        favoritesRef.child(tour.getId()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // nếu đã yêu thích -> xóa khỏi danh sách
                    favoritesRef.child(tour.getId()).removeValue().addOnSuccessListener(aVoid -> {
                        imgBtnFavorite.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(imgBtnFavorite.getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(imgBtnFavorite.getContext(), "Lỗi khi bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    String priceRange = tour.getPrice(); // lấy nguyên chuỗi
                    String formattedPrice = priceRange.replaceAll("(\\d)(?=(\\d{3})+(?!\\d))", "$1,") + " đ";
                    String imageUrl = getFullImageUrl(tour.getPhoto());
                    FavoriteItem favoriteItem = new FavoriteItem(
                            tour.getId(),
                            tour.getTourName(),
                            imageUrl, // giờ đã là URL đầy đủ
                            tour.getDestinations(),
                            formattedPrice,
                            "tour"
                    );



                    favoritesRef.child(tour.getId()).setValue(favoriteItem).addOnSuccessListener(aVoid -> {
                        imgBtnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(imgBtnFavorite.getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(imgBtnFavorite.getContext(), "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(imgBtnFavorite.getContext(), "Lỗi khi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFullImageUrl(String imagePath) {
        if (imagePath.startsWith("http")) {
            return imagePath; // nếu đã là full URL thì giữ nguyên
        }
        String bucketName = "quanbadulichmietvuonvinhlong.appspot.com"; // tên bucket firebase storage
        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/"
                + Uri.encode(imagePath) + "?alt=media";
    }

    // mở google maps
    private void openGoogleMaps(View view, String location) {
        if (!location.isEmpty()) {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                view.getContext().startActivity(mapIntent);
            } else {
                Toast.makeText(view.getContext(), "Không tìm thấy ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(view.getContext(), "Không có thông tin vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    // cập nhật danh sách tour khi có dữ liệu mới
    public void updateTours(List<Tour> newTours) {
        this.tours.clear();
        this.tours.addAll(newTours);
        notifyDataSetChanged();
    }
    public void updateList(List<Tour> newList) {
        this.tourList = newList;
        notifyDataSetChanged();
    }


}
