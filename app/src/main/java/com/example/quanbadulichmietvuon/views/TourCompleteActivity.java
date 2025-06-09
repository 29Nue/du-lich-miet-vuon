package com.example.quanbadulichmietvuon.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.quanbadulichmietvuon.Adapter.TourCompleteAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.CompleteTour;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TourCompleteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TourCompleteAdapter tourAdapter;
    private List<CompleteTour> tourList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_complete);

        // Ánh xạ RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTourHT);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Tạo danh sách tour
        tourList = new ArrayList<>();
        tourAdapter = new TourCompleteAdapter(tourList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(tourAdapter);

        // Lấy dữ liệu từ Intent
        String tourId = getIntent().getStringExtra("tourId");
        String tourName = getIntent().getStringExtra("tourName");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");

        // Lấy ngày giờ hiện tại
        String completeTime = getCurrentDateTime();

        // Lưu dữ liệu nếu đủ thông tin
        if (tourId != null && !tourId.isEmpty()) {
            CompleteTour completeTour = new CompleteTour(tourId, tourName, startDate, endDate, completeTime);
            saveTourToFirebase(completeTour);
        }

        // Tải danh sách tour hoàn thành
        fetchCompleteTours();
    }

    // Hàm lấy thời gian hiện tại dưới dạng chuỗi
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    // Lưu tour vào Firestore
    private void saveTourToFirebase(CompleteTour tour) {
        HashMap<String, Object> tourData = new HashMap<>();
        tourData.put("tourId", tour.getTourId());
        tourData.put("tourName", tour.getTourName());
        tourData.put("startDate", tour.getStartDate());
        tourData.put("endDate", tour.getEndDate());
        tourData.put("completeTime", tour.getCompleteTime()); // Thêm completeTime

        db.collection("CompleteTours")
                .document(tour.getTourId())
                .set(tourData)
                .addOnSuccessListener(unused -> System.out.println("Tour hoàn thành đã được lưu thành công!"))
                .addOnFailureListener(e -> System.err.println("Lỗi khi lưu tour hoàn thành: " + e.getMessage()));
    }

    // Hàm tải danh sách tour hoàn thành từ Firestore
    private void fetchCompleteTours() {
        db.collection("CompleteTours")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tourList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            CompleteTour tour = document.toObject(CompleteTour.class);
                            tourList.add(tour);
                        }
                        tourAdapter.notifyDataSetChanged();
                    } else {
                        System.err.println("Lỗi khi tải danh sách tour hoàn thành: " + task.getException());
                    }
                });
    }
}
