package com.example.quanbadulichmietvuon.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.Adapter.FoodAdapter;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.modules.Food;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FoodActivity extends AppCompatActivity {
    private RecyclerView recyclerFood;
    private EditText searchBar;
    private TextView btn_all1;
    private ImageView btnTraicay, btnBanh, btnMonan, btnAll,btnAllList;
    private FoodAdapter foodAdapter;
    private FirebaseDatabase mDatabase;
    private ArrayList<Food> foodList; // Danh sách tất cả thực phẩm
    private ArrayList<Food> currentList; // Danh sách thực phẩm hiển thị
    private DatabaseReference refFood;
    private StorageReference refPhoto;
    private FirebaseStorage mStorage;

    private RecyclerView rvItems;

    private ImageButton backButton,btnUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        initFirebase();  // Khởi tạo Firebase
        findViews();     // Ánh xạ các view
        setupSearchListener(); // Thiết lập bộ lắng nghe tìm kiếm
        loadFoodData();  // Tải danh sách thực phẩm từ Firebase
        setListeners();  // Thiết lập các sự kiện click
    }

    // Ánh xạ các view từ layout
    private void findViews() {
        backButton = findViewById(R.id.backButton);
        btnUser = findViewById(R.id.userIcon);
        recyclerFood = findViewById(R.id.rv_items);
        searchBar = findViewById(R.id.searchField);
        btnTraicay = findViewById(R.id.btn_traicay);
        btnBanh = findViewById(R.id.btn_banh);
        btnMonan = findViewById(R.id.btn_monan);
        btn_all1 = findViewById(R.id.btn_all1);

        foodList = new ArrayList<>();
        currentList = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : ""; // nếu chưa đăng nhập thì để userId rỗng

        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // lấy userId hiện tại
        foodAdapter = new FoodAdapter(R.layout.item_food, currentList, userId);

        recyclerFood.setAdapter(foodAdapter);
        recyclerFood.setLayoutManager(new GridLayoutManager(this, 1)); // Sử dụng GridLayoutManager
    }
    private void setListeners() {
        //user
        btnUser.setOnClickListener(view -> {
            Intent intent = new Intent(FoodActivity.this, UserActivity.class);
            startActivity(intent);
        });
        // Quay lại màn hình chính
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(FoodActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btnTraicay.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory("Trái cây"), 200);
        });
        btnBanh.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory("Bánh dân gian"), 200);
        });
        btnMonan.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory("Món ăn đặc sản"), 200);
        });

//        btnTraicay.setOnClickListener(v -> filterCategory("Trái cây"));
//        btnBanh.setOnClickListener(v -> filterCategory("Bánh dân gian"));
//        btnMonan.setOnClickListener(v -> filterCategory("Món ăn đặc sản"));
        btn_all1.setOnClickListener(v -> showAllCategories());
    }
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refFood = mDatabase.getReference(Constant.Database.FOOD_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhoto = mStorage.getReference().child(Constant.Storage.PHOTO_FOOD);
    }

    private void setupSearchListener() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFood(s.toString()); // Lọc danh sách thực phẩm
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    private void filterFood(String query) {
        query = query.toLowerCase().trim();
        currentList.clear();

        if (query.isEmpty()) {
            currentList.addAll(foodList);
        } else {
            for (Food food : foodList) {
                if (food.getName().toLowerCase().contains(query) ||
                        food.getCategory().toLowerCase().contains(query)) {
                    currentList.add(food);
                }
            }
        }

        foodAdapter.notifyDataSetChanged();

        if (currentList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món ăn phù hợp.", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterCategory(String category) {
        List<Food> filteredList = new ArrayList<>();
        for (Food food : foodList) {
            if (food.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(food);
            }
        }
        currentList.clear();
        currentList.addAll(filteredList);
        foodAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Hiển thị loại: " + category, Toast.LENGTH_SHORT).show();
    }

    private void showAllCategories() {
        currentList.clear();
        currentList.addAll(foodList);
        foodAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Hiển thị tất cả các loại", Toast.LENGTH_SHORT).show();
    }

    private void loadFoodData() {
        refFood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Food food = data.getValue(Food.class);
                    if (food != null) {
                        foodList.add(food);
                    }
                }
                currentList.clear();
                currentList.addAll(foodList);
                foodAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodActivity.this, "Không thể tải dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadFoodData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoodData();
    }
}
