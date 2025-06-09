package com.example.quanbadulichmietvuon.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.AccommodationAdapter;
import com.example.quanbadulichmietvuon.Adapter.FoodAdapter;
import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
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

public class AccommodationActivity extends AppCompatActivity {

    private TextView btnAll;
    private ImageButton backButton,btnUser;
    private RecyclerView recyclerAccom;
    private ImageView btnHotel, btnHomestay, btnEcoLodge, btnGuestHouse;
    private ImageButton btnAdd;
    private AccommodationAdapter accomAdapter;
    private FirebaseDatabase mDatabase;
    private ArrayList<Accommodation> accomList; // Danh sách tất cả chỗ ở
    private ArrayList<Accommodation> currentList; // Danh sách chỗ ở hiển thị
    private DatabaseReference refAccom;
    private StorageReference refPhoto;
    private FirebaseStorage mStorage;
    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accommodation);
        findViews();   // Ánh xạ các thành phần giao diện
        setListeners();
        initFirebase();  // Khởi tạo Firebase
        findViews();     // Ánh xạ các view
        loadAccomData(); // Tải danh sách chỗ ở từ Firebase

    }
    private void setListeners() {
        //user
        btnUser.setOnClickListener(view -> {
            Intent intent = new Intent(AccommodationActivity.this, UserActivity.class);
            startActivity(intent);
        });
        // Quay lại màn hình chính
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(AccommodationActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btnHotel.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(Accommodation.AccommodationType.HOTEL), 200);
        });
        btnHomestay.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(Accommodation.AccommodationType.HOMESTAY), 200);
        });
        btnEcoLodge.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(Accommodation.AccommodationType.ECOLODGE), 200);
        });
        btnGuestHouse.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(Accommodation.AccommodationType.GUESTHOUSE), 200);
        });
        //btnHotel.setOnClickListener(v -> filterCategory(Accommodation.AccommodationType.HOTEL));
//        btnHomestay.setOnClickListener(v -> filterCategory(Accommodation.AccommodationType.HOMESTAY));
//        btnEcoLodge.setOnClickListener(v -> filterCategory(Accommodation.AccommodationType.ECOLODGE));
//        btnGuestHouse.setOnClickListener(v -> filterCategory(Accommodation.AccommodationType.GUESTHOUSE));
//        btnAll.setOnClickListener(v -> showAllCategories());
    }
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refAccom = mDatabase.getReference(Constant.Database.ACCOM_NODE);
        mStorage = FirebaseStorage.getInstance();
        refPhoto = mStorage.getReference().child(Constant.Storage.PHOTO_ACCOM);
    }

    private void findViews() {

        btnAll = findViewById(R.id.btnall);
        backButton = findViewById(R.id.backButton);
        btnUser = findViewById(R.id.userIcon);
        recyclerAccom = findViewById(R.id.rv_top_destination);
        btnHotel = findViewById(R.id.imgHotel);
        btnHomestay = findViewById(R.id.imgHomestay);
        btnEcoLodge = findViewById(R.id.imgecolodge);
        btnGuestHouse = findViewById(R.id.imgguesthouse);
        searchField = findViewById(R.id.searchFieldAccom);

        accomList = new ArrayList<>();
        currentList = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : ""; // nếu chưa đăng nhập thì để userId rỗng

        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // lấy userId hiện tại
        accomAdapter = new AccommodationAdapter(this,currentList, userId);

        recyclerAccom.setAdapter(accomAdapter);
        recyclerAccom.setLayoutManager(new GridLayoutManager(this, 1));

        // Add TextWatcher for the search functionality
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterBySearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    // This method filters the accommodations based on the search input
    private void filterBySearch(String query) {
        List<Accommodation> filteredList = new ArrayList<>();
        for (Accommodation accom : accomList) {
            if (accom.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(accom);
            }
        }
        currentList.clear();
        currentList.addAll(filteredList);
        accomAdapter.notifyDataSetChanged();
    }

    private void filterCategory(Accommodation.AccommodationType type) {
        List<Accommodation> filteredList = new ArrayList<>();
        for (Accommodation accom : accomList) {
            if (accom.getType() == type) {
                filteredList.add(accom);
            }
        }
        currentList.clear();
        currentList.addAll(filteredList);
        accomAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Hiển thị loại: " + type.name(), Toast.LENGTH_SHORT).show();
    }

    private void showAllCategories() {
        currentList.clear();
        currentList.addAll(accomList);
        accomAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Hiển thị tất cả các loại", Toast.LENGTH_SHORT).show();
    }

    private void loadAccomData() {
        refAccom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accomList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Accommodation accom = data.getValue(Accommodation.class);
                    if (accom != null) {
                        accomList.add(accom);
                    }
                }
                currentList.clear();
                currentList.addAll(accomList);
                accomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccommodationActivity.this, "Không thể tải dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadAccomData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccomData();
    }
}
