package com.example.potholes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//TODO:Login

public class RegisterActivity extends AppCompatActivity {

    Button continue_button;
    EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpViewComponents();
        setListeners();

    }


    private void setUpViewComponents(){
        continue_button = findViewById(R.id.continue_button);
        username = findViewById(R.id.username_editText);
    }

    private void setListeners(){
        continue_button.setOnClickListener(v -> {
            if (!username.getText().toString().isEmpty()) {
                SharedPreferences sharedPreferences = RegisterActivity.this.getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username.getText().toString());
                editor.apply();
                startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
            }else{
                //L'utente nn ha inserito un username
                Toast.makeText(RegisterActivity.this, "Inserisci lo username", Toast.LENGTH_SHORT).show();
            }
        });
    }


}