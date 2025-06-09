package com.example.quanbadulichmietvuon.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.TravelDestinationsAdapter;
import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Accommodation;
import com.example.quanbadulichmietvuon.modules.TravelDestination;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TravelDestinationsActivity extends AppCompatActivity {
    private RecyclerView rvItems;
    private TravelDestinationsAdapter adapter;
    private List<TravelDestination> destinationList, filteredList;
    private DatabaseReference databaseReference;
    private EditText searchField;
    private TextView btnAll;
    private ImageButton backButton, userIcon;
    private ImageView  btnVuontraicay, btnKhudulich, btnDiemthamquan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_destinations);

        rvItems = findViewById(R.id.rv_items);
        searchField = findViewById(R.id.searchField);
        backButton = findViewById(R.id.backButton);
        userIcon = findViewById(R.id.userIcon);
        btnAll = findViewById(R.id.btnAll);
        btnVuontraicay = findViewById(R.id.btn_vuontraicay);
        btnKhudulich = findViewById(R.id.btn_khudulich);
        btnDiemthamquan = findViewById(R.id.btn_diemthamquan);

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        destinationList = new ArrayList<>();
        filteredList = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : ""; // nếu chưa đăng nhập thì để userId rỗng

        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // lấy userId hiện tại
        adapter = new TravelDestinationsAdapter(this, filteredList, userId);
        rvItems.setAdapter(adapter);


        databaseReference = FirebaseDatabase.getInstance().getReference(Constant.Database.DESTINATION_NODE);
        loadDestinations();

        userIcon.setOnClickListener(view -> {
            Intent intent = new Intent(TravelDestinationsActivity.this, UserActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(TravelDestinationsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btnVuontraicay.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(TravelDestination.TravelDestinationType.VUON_TRAI_CAY), 200);
        });
        btnDiemthamquan.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(TravelDestination.TravelDestinationType.DIEM_THAM_QUAN), 200);
        });btnKhudulich.setOnClickListener(v -> {
            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.button_press);
            anim.setTarget(v);
            anim.start();

            v.postDelayed(() -> filterCategory(TravelDestination.TravelDestinationType.KHU_DU_LICH), 200);
        });
//        btnVuontraicay.setOnClickListener(v -> filterCategory(TravelDestination.TravelDestinationType.VUON_TRAI_CAY));
//        btnDiemthamquan.setOnClickListener(v -> filterCategory(TravelDestination.TravelDestinationType.DIEM_THAM_QUAN));
//        btnKhudulich.setOnClickListener(v -> filterCategory(TravelDestination.TravelDestinationType.KHU_DU_LICH));
        btnAll.setOnClickListener(v -> showAllCategories());

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
    private void filterBySearch(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(destinationList);
        } else {
            for (TravelDestination destination : destinationList) {
                if (destination.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(destination);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadDestinations() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destinationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TravelDestination destination = dataSnapshot.getValue(TravelDestination.class);
                    if (destination != null) {
                        destinationList.add(destination);
                    }
                }
                showAllCategories();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // xử lý lỗi nếu cần
            }
        });
    }

    private void filterCategory(TravelDestination.TravelDestinationType category) {
        filteredList.clear();
        for (TravelDestination destination : destinationList) {
            if (destination.getType() == category) {
                filteredList.add(destination);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAllCategories() {
        filteredList.clear();
        filteredList.addAll(destinationList);
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadDestinations();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDestinations();
    }
}
