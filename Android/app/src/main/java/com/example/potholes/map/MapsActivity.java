package com.example.potholes.map;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.example.potholes.R;
import com.example.potholes.databinding.ActivityMapsBinding;
import com.example.potholes.entities.Hole;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<Hole> holeArrayList;
    private Geocoder geocoder;
    private List<Address> address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        holeArrayList = new ArrayList<>();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        holeArrayList = intent.getParcelableArrayListExtra("hole");


    }

    /*

    Recuperiamo l'array list di Hole in un certo raggio dalla posizione corrente per poi posizionare
    i marker per ogni buca recuperata dal server

     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        for (int i = 0; i < holeArrayList.size(); i++) {

            try {
                address = geocoder.getFromLocation(holeArrayList.get(i).getLat(), holeArrayList.get(i).getLon(),1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final LatLng position = new LatLng(holeArrayList.get(i).getLat(), holeArrayList.get(i).getLon());
            if (address != null)
                mMap.addMarker(new MarkerOptions()
                        .title(address.get(0).getAddressLine(0))
                        .position(position)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.
                                        HUE_ORANGE)));
            else
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.
                                        HUE_ORANGE)));



        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(holeArrayList.get(0).getLat(), holeArrayList.get(0).getLon()))
                .zoom(10)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }
}