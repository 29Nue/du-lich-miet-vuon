package com.example.quanbadulichmietvuon.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.FavoriteItem;
import com.example.quanbadulichmietvuon.modules.Tour;
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import com.example.quanbadulichmietvuon.views.EditTravelDestinationActivity;
import com.example.quanbadulichmietvuon.views.SigninActivity;
import com.example.quanbadulichmietvuon.views.TravelDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TravelDestinationsAdapter extends RecyclerView.Adapter<TravelDestinationsAdapter.ViewHolder> {
    private Context context;
    private List<TravelDestination> destinationList;
    private String userId;
    private DatabaseReference favoritesRef;

    public TravelDestinationsAdapter(Context context, List<TravelDestination> destinationList,String userId) {
        this.context = context;
        this.destinationList = destinationList;
        this.userId = userId;
        this.favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_travel_destinations, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelDestination destination = destinationList.get(position);

        if (destination.getImages() != null && !destination.getImages().isEmpty()) {
            Picasso.get().load(destination.getImages().get(0)).into(holder.imgDestination);
        }

        holder.txtDestinationName.setText(destination.getName());
        holder.txtDestinationAddress.setText(destination.getAddress());
        holder.txtDestinationRating.setText("⭐ " + destination.getRating() + "/5 (" + destination.getReviewCount() + " đánh giá)");

        holder.txtDestinationStatus.setText(destination.isActive() ? "🟢 Đang hoạt động" : "🔴 Tạm đóng cửa");
        holder.txtDestinationStatus.setTextColor(context.getResources().getColor(
                destination.isActive() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        holder.imgDestination.setOnClickListener(v -> {
            Intent intent = new Intent(context, TravelDetailActivity.class);
            intent.putExtra("destinations", destination);  // Kiểm tra đúng key là "destination"
            context.startActivity(intent);
        });
        // kiểm tra trạng thái yêu thích
        checkFavoriteStatus(destination.getId(), holder.btn_favorite);

        // sự kiện khi nhấn vào nút yêu thích
        //holder.btn_favorite.setOnClickListener(v -> toggleFavorite(destination, holder.btn_favorite));

        // kiểm tra nếu userId rỗng thì disable nút yêu thích
        if (userId.isEmpty()) {
            holder.btn_favorite.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Bạn cần đăng nhập để yêu thích!", Toast.LENGTH_SHORT).show();
                v.getContext().startActivity(new Intent(v.getContext(), SigninActivity.class));
            });
        } else {
            // đã đăng nhập thì mới cho thêm/xóa yêu thích
            holder.btn_favorite.setOnClickListener(v -> toggleFavorite(destination, holder.btn_favorite));
        }

        holder.txtDestinationAddress.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(destination.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "Google Maps chưa được cài đặt", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);
            popupMenu.inflate(R.menu.menu_item_travel);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    Intent intent = new Intent(context, EditTravelDestinationActivity.class);
                    intent.putExtra("destinationId", destination.getId());
                    intent.putExtra("destinationName", destination.getName());
                    intent.putExtra("destinationDescription", destination.getDescription());
                    intent.putExtra("destinationAddress", destination.getAddress());
                    intent.putExtra("destinationRating", destination.getRating());
                    intent.putExtra("destinationReviewCount", destination.getReviewCount());
                    intent.putExtra("destinationIsActive", destination.isActive());
                    intent.putStringArrayListExtra("destinationImageUrls", new ArrayList<>(destination.getImages()));
                    intent.putExtra("destinationCategory", destination.getType().toString());
                    context.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa điểm đến này?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                DatabaseReference refDestination = FirebaseDatabase.getInstance()
                                        .getReference("travelDestinations")
                                        .child(destination.getId());

                                List<String> imageUrls = destination.getImages();
                                if (imageUrls != null && !imageUrls.isEmpty()) {
                                    for (String imageUrl : imageUrls) {
                                        StorageReference refPhotoDestinationDel = FirebaseStorage.getInstance()
                                                .getReferenceFromUrl(imageUrl);
                                        refPhotoDestinationDel.delete().addOnCompleteListener(deleteTask -> {
                                            if (!deleteTask.isSuccessful()) {
                                                Log.e("TravelAdapter", "Lỗi xóa ảnh: " + deleteTask.getException().getMessage());
                                            }
                                        });
                                    }
                                }

                                refDestination.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Đã xóa điểm đến", Toast.LENGTH_SHORT).show();
                                        destinationList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, destinationList.size());
                                    } else {
                                        Log.e("TravelAdapter", "Lỗi xóa điểm đến: " + task.getException().getMessage());
                                        Toast.makeText(context, "Xóa điểm đến thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                            .show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDestination, btnMore,btn_favorite;
        TextView txtDestinationName, txtDestinationAddress, txtDestinationRating, txtDestinationStatus;
        private FirebaseAuth mAuth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDestination = itemView.findViewById(R.id.imgDestination);
            txtDestinationName = itemView.findViewById(R.id.txtDestinationName);
            txtDestinationAddress = itemView.findViewById(R.id.txtDestinationAddress);
            txtDestinationRating = itemView.findViewById(R.id.txtDestinationRating);
            txtDestinationStatus = itemView.findViewById(R.id.txtDestinationStatus);
            btnMore = itemView.findViewById(R.id.btn_more);
            btn_favorite = itemView.findViewById(R.id.btn_favorite);

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            // Kiểm tra email người dùng
            if (currentUser != null && "nhi@gmail.com".equals(currentUser.getEmail())) {
                btnMore.setVisibility(View.VISIBLE); // Hiển thị nút
            } else {
                btnMore.setVisibility(View.GONE); // Ẩn nút
            }
        }
    }
    private void checkFavoriteStatus(String destinationId, ImageView btn_favorite) {
        favoritesRef.child(destinationId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btn_favorite.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    btn_favorite.setImageResource(R.drawable.ic_favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // lỗi firebase
            }
        });
    }

    // thêm/xóa địa điểm khỏi danh sách yêu thích
    private void toggleFavorite(TravelDestination travelDestination, ImageView btn_favorite) {
        favoritesRef.child(travelDestination.getId()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // nếu đã yêu thích -> xóa khỏi danh sách
                    favoritesRef.child(travelDestination.getId()).removeValue().addOnSuccessListener(aVoid -> {
                        btn_favorite.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(btn_favorite.getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(btn_favorite.getContext(), "Lỗi khi bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // lấy ảnh đầu tiên trong danh sách (nếu có)
                    String firstImage = (travelDestination.getImages() != null && !travelDestination.getImages().isEmpty())
                            ? travelDestination.getImages().get(0)
                            : ""; // nếu không có ảnh thì để chuỗi rỗng

                    FavoriteItem favoriteItem = new FavoriteItem(
                            travelDestination.getId(),
                            travelDestination.getName(),
                            firstImage, // chỉ lấy ảnh đầu tiên
                            travelDestination.getAddress(), // dùng địa chỉ thay cho giá
                            travelDestination.getDescription(), // giữ mô tả
                            "địa điểm"
                    );

                    favoritesRef.child(travelDestination.getId()).setValue(favoriteItem).addOnSuccessListener(aVoid -> {
                        btn_favorite.setImageResource(R.drawable.ic_favorite_filled);
                        Toast.makeText(btn_favorite.getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(btn_favorite.getContext(), "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(btn_favorite.getContext(), "Lỗi khi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

}