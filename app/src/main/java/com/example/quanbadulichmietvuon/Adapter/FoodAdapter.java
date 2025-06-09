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
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.views.EditFoodActivity;
import com.example.quanbadulichmietvuon.views.SigninActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private final int RESOURCE_ID;
    private ArrayList<Food> foods;
    private Context context;
    private String userId;
    private DatabaseReference favoritesRef;
    private OnFoodClickListener onFoodClickListener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(int RESOURCE_ID, ArrayList<Food> foods, String userId) {
        this.RESOURCE_ID = RESOURCE_ID;
        this.foods = foods;
        this.userId = userId;
        this.favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(RESOURCE_ID, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);

        // Set food details
        holder.textName.setText(food.getName());
        holder.textDescription.setText(food.getDescription());
        holder.textLocation.setText(food.getLocation());
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(food.getPrice()) + " đ";
        holder.textPrice.setText(formattedPrice);


        // Load tour image from Firebase Storage
        String photoPath = food.getImageUrl();
        if (photoPath != null && !photoPath.isEmpty()) {
            StorageReference refPhotoCategory = FirebaseStorage.getInstance()
                    .getReference()
                    .child(photoPath);

            refPhotoCategory.getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Picasso.get()
                            .load(task.getResult())
                            .into(holder.imagePhoto);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Image loading failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // kiểm tra trạng thái yêu thích
        checkFavoriteStatus(food.getId(), holder.imgBtnFavorite);

        // sự kiện khi nhấn vào nút yêu thích
        //holder.imgBtnFavorite.setOnClickListener(v -> toggleFavorite(food, holder.imgBtnFavorite));
        // kiểm tra nếu userId rỗng thì disable nút yêu thích
        if (userId.isEmpty()) {
            holder.imgBtnFavorite.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Bạn cần đăng nhập để yêu thích!", Toast.LENGTH_SHORT).show();
                v.getContext().startActivity(new Intent(v.getContext(), SigninActivity.class));
            });
        } else {
            // đã đăng nhập thì mới cho thêm/xóa yêu thích
            holder.imgBtnFavorite.setOnClickListener(v -> toggleFavorite(food, holder.imgBtnFavorite));
        }

        // xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(view -> {
            if (onFoodClickListener != null) {
                onFoodClickListener.onFoodClick(food);
            }
        });
        // Handle item click
        holder.itemView.setOnClickListener(view -> {
            if (onFoodClickListener != null) {
                onFoodClickListener.onFoodClick(food);
            }
        });
        // Make textLocation clickable to open Google Maps
        holder.textLocation.setOnClickListener(view -> {
            String location = holder.textLocation.getText().toString();
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
            Context context = view.getContext();
            PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);
            popupMenu.inflate(R.menu.menu_item_food);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    // Open EditFoodActivity and pass food details
                    Intent intent = new Intent(context, EditFoodActivity.class);
                    intent.putExtra("foodId", food.getId());
                    intent.putExtra("foodName", food.getName());
                    intent.putExtra("foodDescription", food.getDescription());
                    intent.putExtra("foodLocation", food.getLocation());
                    intent.putExtra("foodPrice", food.getPrice());
                    intent.putExtra("foodImageUrl", food.getImageUrl());
                    intent.putExtra("foodCategory", food.getCategory());
                    context.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    // Xác nhận xóa
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa món này không?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                // Xóa món ăn khỏi Firebase Database và Storage
                                DatabaseReference refFood = FirebaseDatabase.getInstance().getReference("food").child(food.getId());
                                StorageReference refPhotoFoodDel = FirebaseStorage.getInstance().getReference().child(food.getImageUrl());

                                refFood.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Nếu xóa thành công trong Database, tiếp tục xóa ảnh
                                        refPhotoFoodDel.delete().addOnCompleteListener(deleteTask -> {
                                            if (deleteTask.isSuccessful()) {
                                                Toast.makeText(context, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                                                foods.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, foods.size());
                                            } else {
                                                Log.e("FoodAdapter", "Lỗi khi xóa ảnh món ăn: " + deleteTask.getException().getMessage());
                                                Toast.makeText(context, "Xóa ảnh món ăn trong Firebase thất bại", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.e("FoodAdapter", "Lỗi khi xóa món ăn khỏi Database: " + task.getException().getMessage());
                                        Toast.makeText(context, "Xóa món ăn khỏi Firebase thất bại", Toast.LENGTH_SHORT).show();
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
        return foods.size();
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.onFoodClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imagePhoto, btnMore,imgBtnFavorite;
        private TextView textName, textDescription, textPrice, textLocation;
        private FirebaseAuth mAuth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePhoto = itemView.findViewById(R.id.imgFood);
            textName = itemView.findViewById(R.id.tv_traicay_name);
            textDescription = itemView.findViewById(R.id.tv_traicay_desc);
            textLocation = itemView.findViewById(R.id.tv_dd_monan);
            textPrice = itemView.findViewById(R.id.tv_burger_price);
            btnMore = itemView.findViewById(R.id.btn_more);
            imgBtnFavorite = itemView.findViewById(R.id.btn_favorite);
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
    private void checkFavoriteStatus(String foodId, ImageView imgBtnFavorite) {
        favoritesRef.child(foodId).addListenerForSingleValueEvent(new ValueEventListener() {
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


    // thêm/xóa món ăn khỏi danh sách yêu thích
    private void toggleFavorite(Food food, ImageView imgBtnFavorite) {
        favoritesRef.child(food.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // nếu đã yêu thích -> xóa khỏi danh sách
                    favoritesRef.child(food.getId()).removeValue().addOnSuccessListener(aVoid -> {
                        imgBtnFavorite.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(imgBtnFavorite.getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(imgBtnFavorite.getContext(), "Lỗi khi bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String formattedPrice = formatter.format(food.getPrice()) + " đ"; // format giá tiền
                    String imageUrl = getFullImageUrl(food.getImageUrl());
                    // nếu chưa yêu thích -> thêm vào danh sách, lưu thêm type
                    FavoriteItem favoriteItem = new FavoriteItem(
                            food.getId(),
                            food.getName(),
                            imageUrl,
                            food.getLocation(),
                            formattedPrice,
                            "món ăn" // gán type là "food"
                    );

                    favoritesRef.child(food.getId()).setValue(favoriteItem).addOnSuccessListener(aVoid -> {
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
    public void updateFoods(List<Food> newFoods) {
        this.foods.clear();
        this.foods.addAll(newFoods);
        notifyDataSetChanged();
    }
}
