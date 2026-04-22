package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar if present to make it full screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay for 2 seconds (2000 milliseconds) then go to LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close SplashActivity so user can't go back to it
            }
        }, 2000);
    }
}
