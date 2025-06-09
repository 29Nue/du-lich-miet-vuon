package com.example.quanbadulichmietvuon.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.MainActivity;
import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.Adapter.TourAdapter;
import com.example.quanbadulichmietvuon.modules.Tour;
import com.example.quanbadulichmietvuon.until.Constant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TourListActivity extends AppCompatActivity {
    private RecyclerView recyclerTour;
    private FloatingActionButton buttonAdd;
    private ImageButton backButton;
    private ArrayList<Tour> tours;
    private ArrayList<Tour> filteredTours; // Danh sách tour đã lọc
    private TextView txtStartDate;
    private EditText edtPrice;
    private TourAdapter adapter;
    private FirebaseDatabase mDatabase;
    private DatabaseReference refTour;
    private static final int ADD_TOUR = 1;
    private int minPriceFilter = 0;
    private List<Tour> originalTourList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_list);

        findViews();   // Ánh xạ các thành phần giao diện
        initData();    // Khởi tạo dữ liệu
        initFirebase();  // Khởi tạo Firebase
        initRecyclerView();  // Khởi tạo RecyclerView
        loadTours(null, 0);  // Tải tất cả danh sách tour ban đầu
        setListeners();  // Thiết lập các sự kiện click
        EditText searchField = findViewById(R.id.searchFieldAccom);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                searchTourByName(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // không cần xử lý
            }
        });
    }
    private void searchTourByName(String query) {
        List<Tour> filteredList = new ArrayList<>();

        for (Tour tour : originalTourList) {
            if (tour.getTourName().toLowerCase().contains(query)) {
                filteredList.add(tour);
            }
        }

        filteredTours.clear();
        filteredTours.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }


    // Hiển thị DatePicker để chọn ngày
    private void showDatePickerDialog(OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            listener.onDateSet(sdf.format(selectedDate.getTime()));
        }, year, month, day);

        datePickerDialog.show();
    }

    // Khởi tạo RecyclerView với GridLayoutManager
    private void initRecyclerView() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(this, SigninActivity.class); // thay bằng activity đăng nhập của em
            startActivity(intent);
            finish(); // đóng activity hiện tại
        } else {
            String userId = user.getUid();
            adapter = new TourAdapter(R.layout.layout_gird_item_tourlist, filteredTours, userId);
        }

        recyclerTour.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerTour.setAdapter(adapter);
    }

    // Khởi tạo Firebase
    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        refTour = mDatabase.getReference(Constant.Database.TOUR_LIST_NODE);
    }

    // Tải tất cả danh sách tour từ Firebase
    private void loadTours(String startDateFilter, int minPriceFilter) {
        refTour.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tours.clear();
                filteredTours.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Tour tour = data.getValue(Tour.class);
                    if (tour != null) {
                        boolean dateMatches = true; // Assume the date always matches if no filter is applied
                        boolean priceMatches = true; // Assume price always matches if no filter is applied

                        // Apply the start date filter if specified
                        if (startDateFilter != null && !startDateFilter.isEmpty()) {
                            dateMatches = startDateFilter.equals(tour.getStartDate());
                        }

                        // Apply the price filter if specified
                        if (minPriceFilter > 0) {
                            try {
                                int price = Integer.parseInt(tour.getPrice()); // Convert price to integer
                                priceMatches = (price >= minPriceFilter);
                            } catch (NumberFormatException e) {
                                priceMatches = false; // If the price is not valid, exclude this tour
                            }
                        }

                        // Add the tour if it passes the filters
                        if (dateMatches && priceMatches) {
                            filteredTours.add(tour);
                        }
                    }
                    originalTourList.clear();
                    originalTourList.addAll(filteredTours);
                    adapter.notifyDataSetChanged();

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TourListActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lọc tour theo ngày bắt đầu
    private void filterToursByStartDate(String startDate) {
        loadTours(startDate, 0);  // Set price filter to 0 to ignore price filtering when filtering by date
    }

    // Lọc tour theo giá
    private void filterToursByPrice(int minPrice) {
        loadTours(null, minPrice);  // Set date filter to null to ignore date filtering when filtering by price
    }

    // Khởi tạo danh sách tour ban đầu
    private void initData() {
        tours = new ArrayList<>();
        filteredTours = new ArrayList<>();
    }

    // Thiết lập các sự kiện click
    private void setListeners() {
        adapter.setOnTourClickListener(tour -> {
            Intent intent = new Intent(TourListActivity.this, TourDetailActivity.class);
            intent.putExtra("tourId", tour.getId());  // Truyền ID của tour sang màn hình chi tiết
            startActivity(intent);
        });

//        buttonAdd.setOnClickListener(view -> {
//            Intent intent = new Intent(TourListActivity.this, AddTourActivity.class);
//            startActivityForResult(intent, ADD_TOUR);
//        });
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(TourListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Lọc theo ngày
        txtStartDate.setOnClickListener(v -> showDatePickerDialog(date -> {
            txtStartDate.setText(date);
            filterToursByStartDate(date); // Filter tours by selected start date
        }));

        // Lọc theo giá với TextWatcher
        edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to do anything before text changes
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // When the text changes, check if it's a valid number
                String priceText = charSequence.toString().trim();
                if (!priceText.isEmpty()) {
                    try {
                        minPriceFilter = Integer.parseInt(priceText);
                        filterToursByPrice(minPriceFilter); // Filter by price when user types
                    } catch (NumberFormatException e) {
                        // If the input is not a valid number, reset the filter
                        Toast.makeText(TourListActivity.this, "Vui lòng nhập mức giá hợp lệ.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If the input is empty, remove the price filter
                    loadTours(null, 0); // Reload all tours without any price filter
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // No need to do anything after text changes
            }
        });

    }
        // Ánh xạ các view từ layout
    private void findViews() {
        recyclerTour = findViewById(R.id.recycleviewTour);
       // buttonAdd = findViewById(R.id.buttonAdd);
        txtStartDate = findViewById(R.id.txtStartDate);
        edtPrice = findViewById(R.id.edtPrice);
        backButton = findViewById(R.id.backButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTours(null, 0); // Load all tours without any filters when the activity resumes
    }

    // Interface để lắng nghe sự kiện chọn ngày
    interface OnDateSetListener {
        void onDateSet(String date);
    }
}
