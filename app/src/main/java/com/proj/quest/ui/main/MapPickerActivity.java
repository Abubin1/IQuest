package com.proj.quest.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;

import java.util.ArrayList;

public class MapPickerActivity extends BaseActivity {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private IMapController mapController;
    private Button confirmButton;

    // Координаты Костромы
    private final GeoPoint KOSTROMA_CENTER = new GeoPoint(57.7679, 40.9269);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Важно! setContentView вызывается до инициализации карты
        setContentView(R.layout.activity_map_picker);

        map = findViewById(R.id.mapview);
        confirmButton = findViewById(R.id.confirm_button);

        map.setMultiTouchControls(true);

        mapController = map.getController();
        mapController.setZoom(12.0);
        mapController.setCenter(KOSTROMA_CENTER);

        confirmButton.setOnClickListener(v -> {
            GeoPoint selectedPoint = (GeoPoint) map.getMapCenter();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedPoint.getLatitude());
            resultIntent.putExtra("longitude", selectedPoint.getLongitude());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
} 