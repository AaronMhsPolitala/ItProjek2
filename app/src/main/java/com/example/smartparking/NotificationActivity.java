package com.example.smartparking;

import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private ArrayList<HistoryParkir> historyList = new ArrayList<>();
    private LinearLayout historyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // INISIALISASI
        historyContainer = findViewById(R.id.historyContainer);

        // ===============================
        // DATA DUMMY HISTORY
        // ===============================
        historyList.add(new HistoryParkir(

                "A1",
                "08:00",
                "09:20",
                "1 Jam 20 Menit"
        ));

        historyList.add(new HistoryParkir(

                "B2",
                "10:15",
                "11:00",
                "45 Menit"
        ));

        historyList.add(new HistoryParkir(

                "C3",
                "13:10",
                "15:20",
                "2 Jam 10 Menit"
        ));

        // NOTIFIKASI SEMENTARA
        for (HistoryParkir item : historyList) {

            Toast.makeText(
                    this,
                    "Mobil " +
                            " keluar dari slot " + item.slot,
                    Toast.LENGTH_SHORT
            ).show();
        }

        showHistory();

        // Klik Dashboard
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Klik Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
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