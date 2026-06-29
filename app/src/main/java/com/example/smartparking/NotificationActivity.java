package com.example.smartparking;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private ArrayList<HistoryParkir> historyList = new ArrayList<>();
    private LinearLayout historyContainer;
    private DatabaseReference databaseReference;
    private DatabaseReference parkingReference;
    private ValueEventListener parkingListener;
    private ValueEventListener notificationListener;
    private DataSnapshot latestNotifSnapshot;
    private DataSnapshot latestParkingSnapshot;
    private boolean hasDoneInitialSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Inisialisasi UI
        historyContainer = findViewById(R.id.historyContainer);

        // Inisialisasi Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("SlotNotifications");
        parkingReference = database.getReference("parking");

        // Navigasi
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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
        ambilDataFirebase();
        setupParkingSync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && notificationListener != null) {
            databaseReference.removeEventListener(notificationListener);
        }
        if (parkingReference != null && parkingListener != null) {
            parkingReference.removeEventListener(parkingListener);
        }
        latestParkingSnapshot = null;
        latestNotifSnapshot = null;
        hasDoneInitialSync = false;
    }

    private void setupParkingSync() {
        parkingListener = parkingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latestParkingSnapshot = snapshot;
                if (latestNotifSnapshot != null) {
                    hasDoneInitialSync = true;
                    syncNotifications(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SmartParkingDebug", "NotificationActivity: Parking sync listener cancelled", error.toException());
            }
        });
    }

    private void syncNotifications(DataSnapshot snapshot) {
        Log.d("SmartParkingDebug", "NotificationActivity: syncNotifications triggered");
        DataSnapshot statusSnapshot = snapshot.child("status_per_slot");
        if (!statusSnapshot.exists()) {
            Log.d("SmartParkingDebug", "NotificationActivity: status_per_slot does not exist in snapshot");
            return;
        }

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String[] slotLetters = {"A", "B", "C", "D", "E", "F"};

        for (int i = 1; i <= 6; i++) {
            String slotKey = "slot_" + i;
            Boolean isAvailable = statusSnapshot.child(slotKey).getValue(Boolean.class);
            Log.d("SmartParkingDebug", "NotificationActivity: Checking " + slotKey + ", isAvailable = " + isAvailable);
            if (isAvailable != null) {
                String statusTeks = isAvailable ? "kosong pada pukul" : "terisi pada pukul";
                String slotLetter = slotLetters[i-1];

                boolean statusBerubah = true;
                if (latestNotifSnapshot != null && latestNotifSnapshot.hasChild(slotKey)) {
                    HistoryParkir existingNotif = latestNotifSnapshot.child(slotKey).getValue(HistoryParkir.class);
                    if (existingNotif != null && existingNotif.status != null) {
                        Log.d("SmartParkingDebug", "NotificationActivity: Existing status for " + slotKey + " is '" + existingNotif.status + "', new status is '" + statusTeks + "'");
                        if (existingNotif.status.equals(statusTeks)) {
                            statusBerubah = false;
                        }
                    }
                }

                if (statusBerubah) {
                    Log.d("SmartParkingDebug", "NotificationActivity: Status changed for " + slotKey + ". Writing to Firebase: status=" + statusTeks + ", waktu=" + currentTime);
                    HistoryParkir notifData = new HistoryParkir(slotLetter, statusTeks, currentTime);
                    databaseReference.child(slotKey).setValue(notifData)
                            .addOnSuccessListener(aVoid -> Log.d("SmartParkingDebug", "NotificationActivity: Successfully updated " + slotKey + " in SlotNotifications"))
                            .addOnFailureListener(e -> Log.e("SmartParkingDebug", "NotificationActivity: Failed to update " + slotKey + " in SlotNotifications", e));
                } else {
                    Log.d("SmartParkingDebug", "NotificationActivity: No status change for " + slotKey + ". Skipping update.");
                }
            }
        }
    }

    private void ambilDataFirebase() {
        Log.d("SmartParkingDebug", "NotificationActivity: Starting ambilDataFirebase");
        notificationListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latestNotifSnapshot = snapshot;
                if (latestParkingSnapshot != null && !hasDoneInitialSync) {
                    hasDoneInitialSync = true;
                    syncNotifications(latestParkingSnapshot);
                }
                Log.d("SmartParkingDebug", "NotificationActivity: onDataChange triggered. Snapshot exists: " + snapshot.exists() + ", Children count: " + snapshot.getChildrenCount());
                String[] slotLetters = {"A", "B", "C", "D", "E", "F"};
                HistoryParkir[] defaultSlots = new HistoryParkir[6];
                for (int i = 0; i < 6; i++) {
                    defaultSlots[i] = new HistoryParkir(slotLetters[i], "belum ada data", "-");
                }

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Log.d("SmartParkingDebug", "NotificationActivity: Processing child key=" + itemSnapshot.getKey() + ", value=" + itemSnapshot.getValue());
                    HistoryParkir history = itemSnapshot.getValue(HistoryParkir.class);
                    if (history != null && history.slot != null) {
                        Log.d("SmartParkingDebug", "NotificationActivity: Parsed HistoryParkir: slot=" + history.slot + ", status=" + history.status + ", waktu=" + history.waktu);
                        int index = -1;
                        for (int i = 0; i < 6; i++) {
                            if (slotLetters[i].equals(history.slot)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            defaultSlots[index] = history;
                        }
                    } else {
                        Log.d("SmartParkingDebug", "NotificationActivity: Parsed HistoryParkir is null or slot is null");
                    }
                }

                historyList.clear();
                for (HistoryParkir h : defaultSlots) {
                    historyList.add(h);
                }

                showHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Gagal sinkronisasi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHistory() {
        historyContainer.removeAllViews();

        if (historyList.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Menunggu update data dari sensor...");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, 100, 0, 0);
            emptyText.setTextColor(0xFF666666);
            historyContainer.addView(emptyText);
            return;
        }

        for (HistoryParkir item : historyList) {
            // Gunakan CardView untuk tampilan yang lebih modern dan menarik
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 8, 0, 24);
            card.setLayoutParams(cardParams);
            card.setRadius(24f);
            card.setCardElevation(6f);
            card.setUseCompatPadding(true);

            // Container di dalam card
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setPadding(32, 32, 32, 32);
            innerLayout.setGravity(Gravity.CENTER_VERTICAL);
            innerLayout.setBackgroundColor(0xFFFFFFFF);

            // Ikon indikator status (Bulatan warna)
            ImageView statusIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
            iconParams.setMargins(0, 0, 32, 0);
            statusIcon.setLayoutParams(iconParams);
            
            boolean isTerisi = item.status != null && item.status.contains("terisi");
            boolean isBelumAda = item.status != null && item.status.contains("belum ada");

            if (isBelumAda) {
                statusIcon.setImageResource(R.drawable.circle_green_bg);
                statusIcon.setColorFilter(0xFF888888); // Abu-abu untuk data kosong
            } else {
                statusIcon.setImageResource(isTerisi ? R.drawable.circle_red : R.drawable.circle_green_bg);
                statusIcon.clearColorFilter();
            }

            // Layout teks (Slot & Keterangan)
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Judul Slot (Misal: Slot A)
            TextView tvSlotName = new TextView(this);
            tvSlotName.setText("Slot " + item.slot);
            tvSlotName.setTextSize(18);
            tvSlotName.setTypeface(null, Typeface.BOLD);
            tvSlotName.setTextColor(0xFF2E73C4);

            // Keterangan Lengkap
            TextView tvStatusDesc = new TextView(this);
            String fullStatus;
            if (item.waktu == null || "-".equals(item.waktu) || item.waktu.isEmpty()) {
                fullStatus = item.status;
            } else {
                fullStatus = item.status + " " + item.waktu;
            }
            tvStatusDesc.setText(fullStatus);
            tvStatusDesc.setTextSize(14);
            
            if (isBelumAda) {
                tvStatusDesc.setTextColor(0xFF888888); // Abu-abu
            } else {
                tvStatusDesc.setTextColor(isTerisi ? 0xFFD32F2F : 0xFF388E3C);
            }
            tvStatusDesc.setPadding(0, 4, 0, 0);

            textLayout.addView(tvSlotName);
            textLayout.addView(tvStatusDesc);

            innerLayout.addView(statusIcon);
            innerLayout.addView(textLayout);
            card.addView(innerLayout);

            historyContainer.addView(card);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
