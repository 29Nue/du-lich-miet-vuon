package com.example.quanbadulichmietvuon;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quanbadulichmietvuon.views.AccommodationActivity;
import com.example.quanbadulichmietvuon.views.ActivityAddNotification;
import com.example.quanbadulichmietvuon.views.AddAccomActivity;
import com.example.quanbadulichmietvuon.views.AddFoodActivity;
import com.example.quanbadulichmietvuon.views.AddTourActivity;
import com.example.quanbadulichmietvuon.views.AddTravelDestinationActivity;
import com.example.quanbadulichmietvuon.views.FoodActivity;
import com.example.quanbadulichmietvuon.views.IntroductionActivity;
import com.example.quanbadulichmietvuon.views.MapActivity;
import com.example.quanbadulichmietvuon.views.NotificationActivity;
import com.example.quanbadulichmietvuon.views.TourBookingListActivity;
import com.example.quanbadulichmietvuon.views.TourListActivity;
import com.example.quanbadulichmietvuon.views.TravelDestinationsActivity;
import com.example.quanbadulichmietvuon.views.UserActivity;

public class AdminActivity extends AppCompatActivity {

    private CardView crdLocation, crdPlace, crdIntro, crdRestaurant, crdHotel, crdVehicle, crdExper, crdAccount,crdNotification,crdDDDL,crd_Booking;
    private ImageButton menuLocation, menuPlace, menuIntro, menuRestaurant, menuHotel, menuChatBox, menuExper, menuAccount, menuNotification,menuDDDL,menuBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        setTitle("Quản trị du lịch");

        // Kết nối các CardView với layout
        crdLocation = findViewById(R.id.crd_Location);
        crdPlace = findViewById(R.id.crd_Place);
        crdIntro = findViewById(R.id.crd_Intro);
        crdRestaurant = findViewById(R.id.crd_Restaurant);
        crdHotel = findViewById(R.id.crd_Hotel);
        crdVehicle = findViewById(R.id.crd_Vehicle);
        crdExper = findViewById(R.id.crd_Exper);
        crdAccount = findViewById(R.id.crd_Account);
        crdNotification = findViewById(R.id.crd_Nofitication);
        crdDDDL = findViewById(R.id.crd_DDDL);
        crd_Booking = findViewById(R.id.crd_Booking);

        // Kết nối các nút menu
        menuLocation = findViewById(R.id.menu_location);
        menuPlace = findViewById(R.id.menu_place);
        menuIntro = findViewById(R.id.menu_intro);
        menuRestaurant = findViewById(R.id.menu_restaurant);
        menuHotel = findViewById(R.id.menu_hotel);
        menuChatBox = findViewById(R.id.menu_chatbox);
        menuExper = findViewById(R.id.menu_exper);
        menuAccount = findViewById(R.id.menu_account);
        menuNotification = findViewById(R.id.menu_notification);
        menuDDDL = findViewById(R.id.menu_dddl);
        menuBooking = findViewById(R.id.menu_booking);

        // Gắn sự kiện
        setupMenu(menuLocation, "Tour");
        setupMenu(menuPlace, "Map");
        setupMenu(menuIntro, "Giới thiệu");
        setupMenu(menuRestaurant, "Ăn uống");
        setupMenu(menuHotel, "Khách sạn");
        setupMenu(menuChatBox, "Chat box");
        setupMenu(menuExper, "Trang chủ");
        setupMenu(menuAccount, "Tài khoản");
        setupMenu(menuNotification,"Thông báo");
        setupMenu(menuDDDL,"Địa điểm du lịch");
        setupMenu(menuBooking,"Booking");
    }

    private void setupMenu(ImageButton menuButton, String title) {
        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(AdminActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_admin_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                handleMenuAction(item, title);
                return true;
            });

            popupMenu.show();
        });
    }

    private void handleMenuAction(MenuItem item, String cardTitle) {
        Intent intent = null;

        // Xử lý cho Tour
        if ("Tour".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                // Mở TourListActivity
                intent = new Intent(AdminActivity.this, TourListActivity.class);
            } else if (item.getItemId() == R.id.action_add) {
                intent = new Intent(AdminActivity.this, AddTourActivity.class);
            }
        }

        // Xử lý cho Ăn uống
        else if ("Ăn uống".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, FoodActivity.class);
            } else if (item.getItemId() == R.id.action_add) {
                intent = new Intent(AdminActivity.this, AddFoodActivity.class);
            }
        }

        // Xử lý cho Khách sạn
        else if ("Khách sạn".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, AccommodationActivity.class);
            } else if (item.getItemId() == R.id.action_add) {
                intent = new Intent(AdminActivity.this, AddAccomActivity.class);
            }
        }

        // Xử lý cho Map
        else if ("Map".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, MapActivity.class);
            }
        }

        // Xử lý cho Giới thiệu
        else if ("Giới thiệu".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, IntroductionActivity.class);
            }
        }

        // Xử lý cho Tài khoản
        else if ("Tài khoản".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, UserActivity.class);
            }
        }

        // Xử lý triển khai
        else if ("Trang chủ".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, MainActivity.class);
            }
        }
        // Xử lý triển khai
        else if ("Thông báo".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, NotificationActivity.class);
            } else if (item.getItemId() == R.id.action_add) {
                intent = new Intent(AdminActivity.this, ActivityAddNotification.class);
            }
        }
        else if ("Chat box".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, ChatboxAdminActivity.class);
            }
        }
        else if ("Địa điểm du lịch".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, TravelDestinationsActivity.class);
            } else if (item.getItemId() == R.id.action_add) {
                intent = new Intent(AdminActivity.this, AddTravelDestinationActivity.class);
            }
        }
        else if ("Booking".equals(cardTitle)) {
            if (item.getItemId() == R.id.action_view) {
                intent = new Intent(AdminActivity.this, TourBookingListActivity.class);
            }
        }

        // Nếu có intent, bắt đầu activity
        if (intent != null) {
            startActivity(intent);
        } else if (!"Phương tiện".equals(cardTitle) && !"Kinh nghiệm".equals(cardTitle)) {
            Toast.makeText(this, "Không có hành động cho " + cardTitle, Toast.LENGTH_SHORT).show();
        }
    }
}
