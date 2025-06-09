package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.TourBookingListAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.TourBooking;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class TourBookingListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TourBookingListAdapter adapter;
    private List<TourBooking> tourList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.quanbadulichmietvuon.R.layout.activity_tour_booking_list);

        recyclerView = findViewById(R.id.recyclerViewTourBooking);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tourList = new ArrayList<>();
        adapter = new TourBookingListAdapter(tourList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference(Constant.Database.TOURBOOKINGS_NODE);

        loadTourBookings();
    }

    private void loadTourBookings() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = user.getUid();
        String currentUserEmail = user.getEmail();
        boolean isAdmin = currentUserEmail != null && currentUserEmail.equals("nhi@gmail.com"); // kiểm tra email admin

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tourList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TourBooking tour = dataSnapshot.getValue(TourBooking.class);
                    if (tour != null) {
                        if (isAdmin || tour.getUserId().equals(currentUserId)) { // admin thấy hết, user chỉ thấy của mình
                            tourList.add(tour);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Lỗi khi tải dữ liệu: " + error.getMessage());
                Toast.makeText(TourBookingListActivity.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
