package com.example.potholes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.example.potholes.fragments.MainFragment;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

//TODO:RICHIEDERE ATTIVAZIONE GPS

public class HomeActivity extends AppCompatActivity {



    private MainFragment mainFragment;
    private String username;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = HomeActivity.this.getSharedPreferences("user",MODE_PRIVATE);
        username = sharedPreferences.getString("username",null);
        mainFragment = new MainFragment();

        setUpViewComponents();
        setUpListeners();
        setUpLocationServices();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        MotionToast.Companion.darkToast(HomeActivity.this,"","Bentornato "+ username, MotionToastStyle.SUCCESS,MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION, ResourcesCompat.getFont(HomeActivity.this,R.font.helveticabold));

    }

    //Configurare le richieste di aggiornamento della posizione
    private void setUpLocationServices() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //TODO:Controlli su LocationResult
                /*public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {

                        }
                    }
                }*/
            }
        };
        try {
            //TODO:Stoppare se non serve (?)
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private void setUpViewComponents() {
        addFragment(mainFragment);


    }

    private void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.commit();

    }

    private void setUpListeners() {
        //Apre la mappa
       /* apri_mappa.setOnClickListener(view -> {
            //Prima di aprire la mapppa controlliamo se i permessi sono attivati
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(HomeActivity.this, MapsActivity.class));
            } else {
                showPermissionDialog();
            }
        });*/
    }





}