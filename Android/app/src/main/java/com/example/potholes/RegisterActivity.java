package com.example.potholes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.potholes.fragments.DetectHoleFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

//TODO:Login

public class RegisterActivity extends AppCompatActivity {

    private Button continue_button;
    private EditText username;
    private SharedPreferences sharedPreferences;
    public static boolean locationPermissionGranted;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final String IP = "20.73.84.69";
    private final int PORT = 80;
    private ProgressDialog dialog;
    private String response;


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

        dialog = new ProgressDialog(RegisterActivity.this);
        getPermissions();

        sharedPreferences = RegisterActivity.this.getSharedPreferences("user", MODE_PRIVATE);
        if (sharedPreferences.getString("username", null) != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {
            setUpViewComponents();
            setListeners();

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
                    dialog.setMessage("Invio dati al server");
                    dialog.show();
                    sendUsernameToServer(username);

                } else {
                    //L'utente nn ha inserito un username
                    MotionToast.Companion.darkToast(RegisterActivity.this, "Errore", "Inserire uno username", MotionToastStyle.ERROR, MotionToast.GRAVITY_BOTTOM, MotionToast.SHORT_DURATION, ResourcesCompat.getFont(RegisterActivity.this, R.font.helveticabold));

                }

            } else {
                if (!username.getText().toString().isEmpty()) {
                    showPermissionDialog();

                } else {
                    //L'utente nn ha inserito un username
                    MotionToast.Companion.darkToast(RegisterActivity.this, "Errore", "Inserire uno username", MotionToastStyle.ERROR, MotionToast.GRAVITY_BOTTOM, MotionToast.SHORT_DURATION, ResourcesCompat.getFont(RegisterActivity.this, R.font.helveticabold));
                }

            }
        });

    }


    private void sendUsernameToServer(EditText username) {
        Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Socket s = new Socket(IP, PORT);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                    out.println("3" + username.getText().toString() + "#");
                    BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    response = fromServer.readLine();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean check = response.equals("0");
                        if (check) {
                            dialog.dismiss();
                            //Todo:Controllo duplicato
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username.getText().toString());
                            editor.apply();
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                            MotionToast.Companion.darkToast(RegisterActivity.this, "Errore", "Lo username inserito esiste già", MotionToastStyle.ERROR, MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION, ResourcesCompat.getFont(RegisterActivity.this, R.font.helveticabold));

                        }
                    }
                });

            }
        });

        thread.start();

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
        builder.setTitle("Permessi necessari");
        builder.setMessage("Devi abilitare i permessi per la localizzazione per poter usufruire della mappa");
        Intent permissions_intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        builder.setPositiveButton("Abilita permessi", (dialog, which) ->
                startActivityForResult(permissions_intent, PERMISSION_REQUEST_CODE)).setNegativeButton("Annulla", (dialog, which) -> {
        });
        AlertDialog mGPSDialog = builder.create();
        mGPSDialog.show();
    }

    /*

    FINE CODICE PER I PERMESSI DELL'APP

     */


}