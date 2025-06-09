package com.example.quanbadulichmietvuon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.example.quanbadulichmietvuon.views.SigninActivity;
import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanbadulichmietvuon.Adapter.FavoriteAdapter;
import com.example.quanbadulichmietvuon.modules.FavoriteItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<FavoriteItem> favoriteList, filteredList;
    private DatabaseReference databaseReference;
    private String userId;
    private TabLayout tabLayout;
    private String selectedType = "tour"; // Mặc định là "tour"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.recyclerViewFavorite);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new FavoriteAdapter(this, filteredList, this::removeFavorite);
        recyclerView.setAdapter(adapter);
        tabLayout = findViewById(R.id.tabLayout);

        // kiểm tra đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, SigninActivity.class));
            finish();
            return;
        }
        userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);

        loadFavorites(); // tải danh sách yêu thích

        // lắng nghe sự kiện chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedType = tab.getText().toString().toLowerCase();
                filterList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFavorites() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FavoriteItem item = data.getValue(FavoriteItem.class);
                    if (item != null && item.getType() != null) { // tránh lỗi null
                        favoriteList.add(item);
                    }
                }
                filterList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoriteActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList() {
        filteredList.clear();
        for (FavoriteItem item : favoriteList) {
            if (selectedType.equals(item.getType())) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        Log.d("DEBUG", "Selected Type: " + selectedType);
        for (FavoriteItem item : favoriteList) {
            Log.d("DEBUG", "Item ID: " + item.getId() + ", Type: " + item.getType());
        }
    }

    private void removeFavorite(FavoriteItem item) {
        databaseReference.child(item.getId()).removeValue();
    }
}
