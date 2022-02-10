package com.example.potholes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.potholes.map.MapsActivity;

public class MainActivity extends AppCompatActivity {

    private Button apri_mappa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpViewComponents();
        setUpListeners();
    }


    private void setUpViewComponents(){
        apri_mappa=findViewById(R.id.apri_mappa);

    }

    private void setUpListeners(){
        //Apre la mappa
        apri_mappa.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        });
    }
}