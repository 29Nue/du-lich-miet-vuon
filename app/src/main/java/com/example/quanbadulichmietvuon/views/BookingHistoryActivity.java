package com.example.quanbadulichmietvuon.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.Adapter.BookingHistoryAdapter;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.BookingTour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingHistoryAdapter adapter;
    private List<BookingTour> bookingList;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        recyclerView = findViewById(R.id.recyclerBookingHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        adapter = new BookingHistoryAdapter(this, bookingList);
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference(Constant.Database.BOOKTOUR_NODE);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String adminEmail = "nhi@gmail.com";
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BookingTour booking = dataSnapshot.getValue(BookingTour.class);
                    if (booking != null) {
                        // nếu là admin thì thấy hết, nếu không thì chỉ thấy của mình
                        if (adminEmail.equals(currentUserEmail) || currentUserId.equals(booking.getUserId())) {
                            bookingList.add(booking);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingHistoryActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }
}
