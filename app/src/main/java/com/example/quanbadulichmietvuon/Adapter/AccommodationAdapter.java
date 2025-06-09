package com.example.quanbadulichmietvuon.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.modules.FavoriteItem;
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.views.EditAccomActivity;
import com.example.quanbadulichmietvuon.views.EditFoodActivity;
import com.example.quanbadulichmietvuon.views.SigninActivity;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AccommodationAdapter extends RecyclerView.Adapter<AccommodationAdapter.AccommodationViewHolder> {

    private Context context;
    private List<Accommodation> accommodationList;
    private String userId;
    private DatabaseReference favoritesRef;

    // Constructor
    public AccommodationAdapter(Context context, List<Accommodation> accommodationList,String userId) {
        this.context = context;
        this.accommodationList = accommodationList;
        this.userId = userId;
        this.favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
    }

    @NonNull
    @Override
    public AccommodationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_accommodation, parent, false);
        return new AccommodationViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AccommodationViewHolder holder, int position) {
        Accommodation accommodation = accommodationList.get(position);

        // Thiết lập tên chỗ ở
        holder.nameTextView.setText(accommodation.getName());

        // Thiết lập vị trí
        holder.locationTextView.setText(accommodation.getLocation());

        // Thiết lập đánh giá
        holder.ratingTextView.setText( accommodation.getRating()+ "★");

        // Thiết lập giá và mô tả
        double price = accommodation.getPrice(); // Giả sử giá là double hoặc float
        DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Định dạng số có dấu phân cách
        String formattedPrice = decimalFormat.format(price) + " VND/đêm";
        holder.priceTextView.setText(formattedPrice);

        String description = accommodation.getDescription();  // Giả sử có phương thức getDescription() trả về mô tả
        holder.descriptionTextView.setText(description);
        // Thiết lập ViewPager2 cho hình ảnh
        List<String> imageUrls = accommodation.getImageResId(); // Giả sử bạn có phương thức để lấy URL hình ảnh
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(holder.itemView.getContext(), imageUrls);
            holder.viewPager.setAdapter(imageSliderAdapter);
        }
        // Thêm tính năng tự động cuộn ảnh cho RecyclerView
        autoSlideImages(holder.viewPager, imageUrls.size());

        // kiểm tra trạng thái yêu thích
        checkFavoriteStatus(accommodation.getId(), holder.btn_favorite);

        // kiểm tra nếu userId rỗng thì disable nút yêu thích
        if (userId.isEmpty()) {
            holder.btn_favorite.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Bạn cần đăng nhập để yêu thích!", Toast.LENGTH_SHORT).show();
                v.getContext().startActivity(new Intent(v.getContext(), SigninActivity.class));
            });
        } else {
            // đã đăng nhập thì mới cho thêm/xóa yêu thích
            holder.btn_favorite.setOnClickListener(v -> toggleFavorite(accommodation, holder.btn_favorite));
        }

        // Make textLocation clickable to open Google Maps
        holder.locationTextView.setOnClickListener(view -> {
            String location = holder.locationTextView.getText().toString();
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if there's an app to handle the intent
            if (mapIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                view.getContext().startActivity(mapIntent);
            } else {
                // Optional: Handle the case when Google Maps is not installed
                Toast.makeText(view.getContext(), "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
        });
        // Set up the PopupMenu for the more button
        holder.btnMore.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);
            popupMenu.inflate(R.menu.menu_item_accom);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    // Open EditFoodActivity and pass accommodation details
                    Intent intent = new Intent(context, EditAccomActivity.class);

                    // Pass necessary details to the EditFoodActivity
                    intent.putExtra("accomId", accommodation.getId());
                    intent.putExtra("accomName", accommodation.getName());
                    intent.putExtra("accomLocation", accommodation.getLocation());
                    intent.putExtra("accomRating", accommodation.getRating());
                    intent.putExtra("accomType", accommodation.getType().toString());  // Assuming Type is an enum
                    intent.putStringArrayListExtra("accomImageResIds", new ArrayList<>(accommodation.getImageResId()));

                    if (accommodation.getImageResId() != null && accommodation.getImageResId().size() > 0) {
                        intent.putStringArrayListExtra("accomImageResIds", new ArrayList<>(accommodation.getImageResId()));
                    }
                    intent.putExtra("accomPrice", accommodation.getPrice());  // Assuming there's a getPrice() method
                    intent.putExtra("accomDescription", accommodation.getDescription());  // Assuming there's a getDescription() method

                    context.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    // Xác nhận xóa
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa chỗ lưu trú này không?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                // Xóa accommodation từ Firebase Database
                                DatabaseReference refAccom = FirebaseDatabase.getInstance().getReference("accommodation").child(accommodation.getId());
                                refAccom.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Nếu xóa thành công accommodation, tiến hành xóa ảnh từ Firebase Storage
                                        if (accommodation.getImageResId() != null && !accommodation.getImageResId().isEmpty()) {
                                            for (String imageResId : accommodation.getImageResId()) {
                                                // Lấy reference đến ảnh trong Firebase Storage và xóa nó
                                                StorageReference refPhotoDel = FirebaseStorage.getInstance().getReferenceFromUrl(imageResId);
                                                refPhotoDel.delete().addOnCompleteListener(deleteTask -> {
                                                    if (deleteTask.isSuccessful()) {
                                                        Log.d("AccomAdapter", "Đã xóa ảnh thành công: " + imageResId);
                                                    } else {
                                                        // Xử lý khi xóa ảnh gặp lỗi
                                                        Log.e("AccomAdapter", "Lỗi khi xóa ảnh: " + deleteTask.getException().getMessage());
                                                    }
                                                });
                                            }
                                        }

                                        // Cập nhật UI sau khi xóa thành công
                                        Toast.makeText(context, "Đã xóa chỗ lưu trú", Toast.LENGTH_SHORT).show();
                                        accommodationList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, accommodationList.size());

                                    } else {
                                        // Nếu có lỗi khi xóa accommodation từ Firebase Database
                                        Log.e("AccomAdapter", "Lỗi khi xóa chỗ lưu trú khỏi Firebase: " + task.getException().getMessage());
                                        Toast.makeText(context, "Không thể xóa chỗ lưu trú từ Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
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
        return accommodationList.size();
    }

    // ViewHolder class
    public static class AccommodationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView, ratingTextView,priceTextView,descriptionTextView;
        ImageView  btnMore,btn_favorite ;
        ViewPager2 viewPager;  // Sửa lại để khai báo đúng loại ViewPager2
        private FirebaseAuth mAuth;
        public AccommodationViewHolder(@NonNull View itemView) {
            super(itemView);
            // Khởi tạo các view
            viewPager = itemView.findViewById(R.id.viewpager_accommodation_images);  // ID đúng của ViewPager2
            nameTextView = itemView.findViewById(R.id.text_accommodation_name);
            locationTextView = itemView.findViewById(R.id.text_accommodation_location);
            ratingTextView = itemView.findViewById(R.id.text_accommodation_rating);
            priceTextView = itemView.findViewById(R.id.text_accom_price);
            descriptionTextView = itemView.findViewById(R.id.text_accom_description);
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

    // kiểm tra trạng thái yêu thích
    private void checkFavoriteStatus(String accomId, ImageView btn_favorite) {
        favoritesRef.child(accomId).addListenerForSingleValueEvent(new ValueEventListener() {
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


    // thêm/xóa món ăn khỏi danh sách yêu thích
    private void toggleFavorite(Accommodation accommodation, ImageView btn_favorite) {
        favoritesRef.child(accommodation.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // nếu đã yêu thích -> xóa khỏi danh sách
                    favoritesRef.child(accommodation.getId()).removeValue().addOnSuccessListener(aVoid -> {
                        btn_favorite.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(btn_favorite.getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(btn_favorite.getContext(), "Lỗi khi bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String formattedPrice = formatter.format(accommodation.getPrice()) + " đ"; // format giá tiền

                    // lấy ảnh đầu tiên, nếu danh sách rỗng thì gán chuỗi rỗng
                    String firstImageUrl = (accommodation.getImageResId() != null && !accommodation.getImageResId().isEmpty())
                            ? accommodation.getImageResId().get(0)
                            : "";

                    FavoriteItem favoriteItem = new FavoriteItem(
                            accommodation.getId(),
                            accommodation.getName(),
                            firstImageUrl,  // chỉ lấy ảnh đầu tiên
                            accommodation.getLocation(),
                            formattedPrice,
                            "chỗ ở" // gán type là "chỗ ở"
                    );


                    favoritesRef.child(accommodation.getId()).setValue(favoriteItem).addOnSuccessListener(aVoid -> {
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

    // Cập nhật lại danh sách các chỗ ở
    public void updateAccommodationList(List<Accommodation> newAccommodationList) {
        this.accommodationList.clear();
        this.accommodationList.addAll(newAccommodationList);
        notifyDataSetChanged(); // Thông báo cho adapter rằng dữ liệu đã thay đổi
    }
    // Tính năng tự động cuộn ảnh sau mỗi 3 giây
    private void autoSlideImages(ViewPager2 viewPager, int totalImages) {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int currentItem = 0;

            @Override
            public void run() {
                if (currentItem == totalImages) {
                    currentItem = 0;  // Nếu đến ảnh cuối cùng, quay lại ảnh đầu tiên
                }
                viewPager.setCurrentItem(currentItem++, true);  // Cuộn đến ảnh kế tiếp
                handler.postDelayed(this, 3000);  // Đợi 3 giây trước khi cuộn sang ảnh tiếp theo
            }
        };

        handler.post(runnable);  // Bắt đầu việc tự động cuộn ảnh
    }
}
