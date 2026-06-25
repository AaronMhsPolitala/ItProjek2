package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
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
            if (currentUser.getEmail() != null) {
                String emailParts = currentUser.getEmail().split("@")[0];
                tvName.setText(emailParts);
                tvEmail.setText(currentUser.getEmail());
            }

            // Klik Edit Nama
            findViewById(R.id.btnEditName).setOnClickListener(v -> {
                Log.d(TAG, "Tombol Edit Nama diklik");
                showEditNameDialog(uid);
            });

            // Klik Ubah Password
            findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
                Log.d(TAG, "Tombol Ubah Password diklik");
                startActivity(new Intent(this, ChangePasswordActivity.class));
            });

            // Ambil data asli dari Realtime Database: users/{uid}
            // Menggunakan ValueEventListener agar UI terupdate otomatis saat data di Firebase berubah
            mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameFromDb = snapshot.child("name").getValue(String.class);
                        String emailFromDb = snapshot.child("email").getValue(String.class);
                        
                        if (nameFromDb != null && !nameFromDb.isEmpty()) {
                            tvName.setText(nameFromDb);
                            Log.d(TAG, "Nama berhasil dimuat dari database: " + nameFromDb);
                        }
                        if (emailFromDb != null && !emailFromDb.isEmpty()) {
                            tvEmail.setText(emailFromDb);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Database Error: " + error.getMessage());
                    Toast.makeText(ProfileActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Klik Dashboard
        findViewById(R.id.navDashboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            Log.d(TAG, "Proses Logout");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void showEditNameDialog(String uid) {
        // Persiapkan View dari dialog_edit_name.xml
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null);
        EditText etNewName = dialogView.findViewById(R.id.etNewName);
        
        // Set nama saat ini ke EditText sebagai default
        etNewName.setText(tvName.getText().toString());

        // Buat Material Alert Dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Listener tombol Simpan di dalam dialog
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String newName = etNewName.getText().toString().trim();
            
            if (newName.isEmpty()) {
                etNewName.setError("Nama tidak boleh kosong");
                return;
            }

            Log.d(TAG, "Mencoba memperbarui nama menjadi: " + newName);

            // Simpan ke Firebase Realtime Database: users/{uid}/name
            mDatabase.child("users").child(uid).child("name").setValue(newName)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Berhasil memperbarui nama di Firebase");
                        Toast.makeText(ProfileActivity.this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Gagal memperbarui nama: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Gagal memperbarui: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Listener tombol Batal di dalam dialog
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            Log.d(TAG, "Dialog Edit Nama dibatalkan");
            dialog.dismiss();
        });

        dialog.show();
        Log.d(TAG, "Dialog Edit Nama ditampilkan");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
