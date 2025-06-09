package com.example.quanbadulichmietvuon.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.quanbadulichmietvuon.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton fabLocation, fabDirections;

    // Tọa độ của Cù lao An Bình
    private final LatLng destination = new LatLng(10.29375, 105.98214);
    private final LatLng cuLaoMay = new LatLng(9.954167, 105.904167);
    private final LatLng choNoiTraOn = new LatLng(9.9665032, 105.9214240);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fabLocation = findViewById(R.id.fab_location);
        fabDirections = findViewById(R.id.fab_directions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fabLocation.setOnClickListener(view -> getDeviceLocation());
        fabDirections.setOnClickListener(view -> showDirections());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Thêm marker tại điểm đến và di chuyển camera
        mMap.addMarker(new MarkerOptions().position(destination).title("Cù lao An Bình"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 12));

        mMap.addMarker(new MarkerOptions().position(cuLaoMay).title("Cù lao Mây"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cuLaoMay, 12));

        // Thêm marker cho Chợ nổi Trà Ôn
        mMap.addMarker(new MarkerOptions().position(choNoiTraOn).title("Chợ nổi Trà Ôn"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(choNoiTraOn, 12));

        // Kiểm tra và yêu cầu quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Vị trí của bạn"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void showDirections() {
        // Kiểm tra quyền truy cập vị trí trước khi lấy vị trí hiện tại
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Tạo URI cho Google Maps chỉ đường từ vị trí hiện tại đến điểm đến
                    Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="
                            + currentLocation.latitude + "," + currentLocation.longitude
                            + "&destination=" + destination.latitude + "," + destination.longitude
                            + "&travelmode=driving");

                    // Khởi chạy Intent để mở Google Maps với URI
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}
