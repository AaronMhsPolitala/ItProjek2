package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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

    private TextView tvProfileName, tvProfileEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inisialisasi View - Menggunakan ID yang sesuai dengan activity_profile.xml
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        if (currentUser != null) {
            currentUid = currentUser.getUid();
            
            // Set data awal dari Firebase Auth
            if (currentUser.getEmail() != null) {
                String emailParts = currentUser.getEmail().split("@")[0];
                tvProfileName.setText(emailParts);
                tvProfileEmail.setText(currentUser.getEmail());
            }

            // Ambil data real-time dari Database: users/{uid}
            mDatabase.child("users").child(currentUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nameFromDb = snapshot.child("name").getValue(String.class);
                        String emailFromDb = snapshot.child("email").getValue(String.class);
                        
                        if (nameFromDb != null && !nameFromDb.isEmpty()) {
                            tvProfileName.setText(nameFromDb);
                        }
                        if (emailFromDb != null && !emailFromDb.isEmpty()) {
                            tvProfileEmail.setText(emailFromDb);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Tombol Kembali
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tombol Ganti Nama
        findViewById(R.id.btnChangeName).setOnClickListener(v -> showEditNameDialog());

        // Tombol Ganti Password
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Tombol Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Pengganti onBackPressed yang deprecated
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    private void showEditNameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null);
        EditText etNewName = dialogView.findViewById(R.id.etNewName);

        // Isi nama saat ini ke inputan
        if (tvProfileName != null) {
            etNewName.setText(tvProfileName.getText().toString());
            etNewName.setSelection(etNewName.getText().length());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Nama")
                .setMessage("Masukkan nama lengkap baru Anda")
                .setView(dialogView)
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newName = etNewName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateNameInDatabase(newName);
                    } else {
                        Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void updateNameInDatabase(String newName) {
        if (currentUid != null) {
            mDatabase.child("users").child(currentUid).child("name").setValue(newName)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal memperbarui nama", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
