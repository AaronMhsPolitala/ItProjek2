package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private Button btnRefresh;
    private TextView tvTersedia, tvTerisi;
    private MaterialCardView[] cardSlots = new MaterialCardView[6];
    private DatabaseReference databaseReference;
    private DatabaseReference notificationRef;
    private ValueEventListener parkingListener;
    private ValueEventListener notificationListener;
    private DataSnapshot latestNotifSnapshot;

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("parking");
        notificationRef = database.getReference("SlotNotifications");

        // Mengambil data awal dashboard secara satu kali (tidak otomatis update di UI)
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateUI(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot result = task.getResult();
                    updateUI(result);
                    Toast.makeText(HomeActivity.this, "Data diperbarui", Toast.LENGTH_SHORT).show();
                }
            });
        });

        findViewById(R.id.navNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        parkingListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                syncNotifications(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error listener di background
            }
        });

        notificationListener = notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latestNotifSnapshot = snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error listener di background
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && parkingListener != null) {
            databaseReference.removeEventListener(parkingListener);
        }
        if (notificationRef != null && notificationListener != null) {
            notificationRef.removeEventListener(notificationListener);
        }
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

    /**
     * Sinkronisasi otomatis ke node SlotNotifications setiap ada perubahan status fisik
     */
    private void syncNotifications(DataSnapshot snapshot) {
        Log.d("SmartParkingDebug", "HomeActivity: syncNotifications triggered");
        DataSnapshot statusSnapshot = snapshot.child("status_per_slot");
        if (!statusSnapshot.exists()) {
            Log.d("SmartParkingDebug", "HomeActivity: status_per_slot does not exist in snapshot");
            return;
        }

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String[] slotLetters = {"A", "B", "C", "D", "E", "F"};

        for (int i = 1; i <= 6; i++) {
            String slotKey = "slot_" + i;
            Boolean isAvailable = statusSnapshot.child(slotKey).getValue(Boolean.class);
            Log.d("SmartParkingDebug", "HomeActivity: Checking " + slotKey + ", isAvailable = " + isAvailable);
            if (isAvailable != null) {
                // Gunakan format deskriptif: "terisi pada pukul" atau "kosong pada pukul"
                String statusTeks = isAvailable ? "kosong pada pukul" : "terisi pada pukul";
                String slotLetter = slotLetters[i-1];

                // Periksa apakah notifikasi untuk slot ini sudah ada di database dan apakah statusnya berubah
                boolean statusBerubah = true;
                if (latestNotifSnapshot != null && latestNotifSnapshot.hasChild(slotKey)) {
                    HistoryParkir existingNotif = latestNotifSnapshot.child(slotKey).getValue(HistoryParkir.class);
                    if (existingNotif != null && existingNotif.status != null) {
                        Log.d("SmartParkingDebug", "HomeActivity: Existing status for " + slotKey + " is '" + existingNotif.status + "', new status is '" + statusTeks + "'");
                        // Jika status sama dengan sebelumnya, jangan ubah waktu/statusnya
                        if (existingNotif.status.equals(statusTeks)) {
                            statusBerubah = false;
                        }
                    }
                }

                // Hanya update ke Firebase jika statusnya memang berubah dari sebelumnya
                if (statusBerubah) {
                    Log.d("SmartParkingDebug", "HomeActivity: Status changed for " + slotKey + ". Writing to Firebase: status=" + statusTeks + ", waktu=" + currentTime);
                    HistoryParkir notifData = new HistoryParkir(slotLetter, statusTeks, currentTime);
                    notificationRef.child(slotKey).setValue(notifData)
                            .addOnSuccessListener(aVoid -> Log.d("SmartParkingDebug", "HomeActivity: Successfully updated " + slotKey + " in SlotNotifications"))
                            .addOnFailureListener(e -> Log.e("SmartParkingDebug", "HomeActivity: Failed to update " + slotKey + " in SlotNotifications", e));
                } else {
                    Log.d("SmartParkingDebug", "HomeActivity: No status change for " + slotKey + ". Skipping update.");
                }
            }
        }
    }
}
