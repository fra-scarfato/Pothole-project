package com.example.potholes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//TODO:Login

public class RegisterActivity extends AppCompatActivity {

    private Button continue_button;
    private EditText username;
    private SharedPreferences sharedPreferences;
    //Forse non serve vediamo piu avanti
    public static boolean locationPermissionGranted;
    private static final int PERMISSION_REQUEST_CODE = 1;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        getPermissions();

        sharedPreferences = RegisterActivity.this.getSharedPreferences("user", MODE_PRIVATE);
        if (sharedPreferences.getString("username", null) == null) {
            setUpViewComponents();
            setListeners();
        } else {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }


    }


    private void setUpViewComponents() {
        continue_button = findViewById(R.id.continue_button);
        username = findViewById(R.id.username_editText);
    }

    private void setListeners() {

        continue_button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                if (!username.getText().toString().isEmpty()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username.getText().toString());
                    editor.apply();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));

                } else {
                    //L'utente nn ha inserito un username
                    Toast.makeText(RegisterActivity.this, "Inserisci lo username", Toast.LENGTH_SHORT).show();
                }

            } else {
                  showPermissionDialog();
            }
        });

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