package com.example.quanbadulichmietvuon.views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.quanbadulichmietvuon.R;

public class TourCancelActivity extends AppCompatActivity {

    private TextView txtCanceledTour, txtCanceledTourInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_cancel);

        txtCanceledTour = findViewById(R.id.txtCanceledTour);
        txtCanceledTourInfo = findViewById(R.id.txtCanceledTourInfo);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String tourName = intent.getStringExtra("tourName");

        // Hiển thị thông tin tour bị hủy
        txtCanceledTourInfo.setText("Tour " + tourName + " đã bị hủy.");
    }
}
