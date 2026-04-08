package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inisialisasi View
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            
            // Backup Nama dari Email jika data di database sedang loading
            String emailParts = currentUser.getEmail().split("@")[0];
            tvName.setText(emailParts);
            tvEmail.setText(currentUser.getEmail());

            // Ambil data asli dari Realtime Database: users/{uid}
            mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameFromDb = snapshot.child("name").getValue(String.class);
                        String emailFromDb = snapshot.child("email").getValue(String.class);
                        
                        if (nameFromDb != null && !nameFromDb.isEmpty()) {
                            tvName.setText(nameFromDb);
                        }
                        if (emailFromDb != null && !emailFromDb.isEmpty()) {
                            tvEmail.setText(emailFromDb);
                        }
                    } else {
                        // Jika tidak ditemukan di Database, coba buat datanya sekarang 
                        // agar kedepannya tidak kosong lagi
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Klik Dashboard
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Klik Notifikasi
        findViewById(R.id.navNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
            overridePendingTransition(0, 0);
        });

        // Klik Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Perintah Logout Firebase
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
