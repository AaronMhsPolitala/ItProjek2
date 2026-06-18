package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private ArrayList<HistoryParkir> historyList = new ArrayList<>();
    private LinearLayout historyContainer;
    
    // Tambahkan referensi Firebase
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // INISIALISASI UI
        historyContainer = findViewById(R.id.historyContainer);

        // INISIALISASI FIREBASE
        // Karena server database Anda berada di region asia-southeast1, kita WAJIB memasukkan URL-nya
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://itprojek2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = database.getReference("HistoryParkir");

        // MEMBACA DATA DARI FIREBASE SECARA REALTIME
        ambilDataFirebase();

        // Klik Dashboard
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Klik Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void ambilDataFirebase() {
        // Gunakan limitToLast(6) agar hanya memuat 6 data terbaru saja (sesuai jumlah slot)
        databaseReference.limitToLast(6).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Bersihkan list agar tidak duplikat
                historyList.clear();

                // Lakukan looping pada setiap data di Firebase
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    HistoryParkir history = itemSnapshot.getValue(HistoryParkir.class);
                    if (history != null) {
                        historyList.add(history);
                    }
                }

                // Urutkan berdasarkan angka pada nama slot (1 -> 6)
                java.util.Collections.sort(historyList, (h1, h2) -> {
                    try {
                        int s1 = Integer.parseInt(h1.slot.replaceAll("[^0-9]", ""));
                        int s2 = Integer.parseInt(h2.slot.replaceAll("[^0-9]", ""));
                        return Integer.compare(s1, s2);
                    } catch (Exception e) {
                        return 0; // Jika gagal parsing, biarkan urutannya
                    }
                });

                // Tampilkan data yang sudah diambil
                showHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Gagal memuat data dari database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHistory() {

        historyContainer.removeAllViews();

        for (HistoryParkir item : historyList) {

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(25, 25, 25, 25);
            card.setBackgroundColor(0xFFFFFFFF);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 0, 0, 20);
            card.setLayoutParams(params);

            // Slot
            TextView slot = new TextView(this);
            slot.setText("Slot: " + item.slot);
            slot.setTextColor(0xFF666666);

            // Durasi
            // Waktu Masuk
            TextView masuk = new TextView(this);
            masuk.setText("Masuk : " + item.waktuMasuk);
            masuk.setTextColor(0xFF666666);

            // Waktu Keluar
            TextView keluar = new TextView(this);
            keluar.setText("Keluar : " + item.waktuKeluar);
            keluar.setTextColor(0xFF666666);

            // Total Waktu
            TextView total = new TextView(this);
            total.setText("Total : " + item.totalWaktu);
            total.setTextColor(0xFF2E73C4);
            total.setTextSize(14);
            total.setPadding(0, 10, 0, 0);

            card.addView(slot);
            card.addView(masuk);
            card.addView(keluar);
            card.addView(total);

            historyContainer.addView(card);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}