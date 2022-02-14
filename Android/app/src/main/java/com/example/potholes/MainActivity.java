package com.example.potholes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import com.example.potholes.fragments.MainFragment;
import com.example.potholes.map.MapsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

//TODO:RICHIEDERE ATTIVAZIONE GPS

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    //Forse non serve vediamo piu avanti
    public static boolean locationPermissionGranted;
    MainFragment mainFragment = new MainFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViewComponents();
        locationPermissionGranted = false;

        //Appena l'utente apre l'app chiediamo tutti i permessi necessari per usarla
        getPermissions();
        setUpListeners();
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
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            } else {
                showPermissionDialog();
            }
        });*/
    }


    /*

    INIZIO CODICE PER I PERMESSI DELL'APP

     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return;

            locationPermissionGranted = true;
        }

    }


    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (fineLocationGranted && coarseLocationGranted) {
                            locationPermissionGranted = true;
                        } else {
                            locationPermissionGranted = false;
                        }
                    }
            );

    //Richiede i permessi all'utente
    private boolean getPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            locationPermissionGranted = true;
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        return locationPermissionGranted;
    }

    public void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Permissions");
        builder.setMessage("You have to enable permissions in order to use the map");
        Intent permissions_intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        builder.setPositiveButton("Enable permissions", (dialog, which) ->
                startActivityForResult(permissions_intent, PERMISSION_REQUEST_CODE)).setNegativeButton("No, Just Exit", (dialog, which) -> {
        });
        AlertDialog mGPSDialog = builder.create();
        mGPSDialog.show();
    }

    /*

    FINE CODICE PER I PERMESSI DELL'APP

     */


}