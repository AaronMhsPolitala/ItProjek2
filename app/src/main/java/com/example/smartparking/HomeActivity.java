package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private Button btnRefresh;
    private TextView tvTersedia, tvTerisi;
    private MaterialCardView[] cardSlots = new MaterialCardView[6];
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRefresh = findViewById(R.id.btnRefresh);
        tvTersedia = findViewById(R.id.tvTersedia);
        tvTerisi = findViewById(R.id.tvTerisi);

        cardSlots[0] = findViewById(R.id.cardSlot1);
        cardSlots[1] = findViewById(R.id.cardSlot2);
        cardSlots[2] = findViewById(R.id.cardSlot3);
        cardSlots[3] = findViewById(R.id.cardSlot4);
        cardSlots[4] = findViewById(R.id.cardSlot5);
        cardSlots[5] = findViewById(R.id.cardSlot6);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itprojek2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = database.getReference("parking");

        // Action saat tombol refresh diklik
        btnRefresh.setOnClickListener(v -> {
            btnRefresh.setEnabled(false); // Disable sementara agar tidak dispam
            btnRefresh.setText("Memuat...");
            
            databaseReference.get().addOnCompleteListener(task -> {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Perbarui Data");
                
                if (!task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                } else {
                    updateUI(task.getResult());
                    Toast.makeText(HomeActivity.this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Ambil data pertama kali saat halaman dibuka
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateUI(task.getResult());
            } else {
                Toast.makeText(HomeActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });

        // Klik Notifikasi
        findViewById(R.id.navNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
            overridePendingTransition(0, 0);
        });

        // Klik Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void updateUI(DataSnapshot snapshot) {
        if (!snapshot.exists()) return;
        
        Integer slotKosong = snapshot.child("slotKosong").getValue(Integer.class);
        Integer totalSlot = snapshot.child("totalSlot").getValue(Integer.class);
        
        if (slotKosong != null && totalSlot != null) {
            int terisi = totalSlot - slotKosong;
            tvTersedia.setText("Tersedia : " + slotKosong);
            tvTerisi.setText("Terisi : " + terisi);
        }

        DataSnapshot statusSnapshot = snapshot.child("status_per_slot");
        if (statusSnapshot.exists()) {
            for (int i = 0; i < 6; i++) {
                Boolean isAvailable = statusSnapshot.child("slot_" + (i + 1)).getValue(Boolean.class);
                if (isAvailable != null) {
                    if (isAvailable) {
                        cardSlots[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_green));
                    } else {
                        cardSlots[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_red));
                    }
                }
            }
        }
    }
}
