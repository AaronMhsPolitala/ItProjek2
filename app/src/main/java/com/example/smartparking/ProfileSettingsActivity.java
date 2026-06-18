package com.example.smartparking;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileSettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser currentUser;

    private TextView tvProfileName, tvProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ambil komponen TextView dari XML untuk menampilkan data realtime
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // Jika user ternyata belum login, pindahkan ke LoginActivity dengan aman
        if (currentUser == null) {
            Toast.makeText(this, "Sesi habis, silakan login kembali", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Hubungkan ke Firebase Database berdasarkan UID pengguna
        mUserRef = FirebaseDatabase.getInstance("https://itprojek2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(currentUser.getUid());

        // Tampilkan Email langsung dari Firebase Auth
        if (tvProfileEmail != null && currentUser.getEmail() != null) {
            tvProfileEmail.setText(currentUser.getEmail());
        }

        // Ambil Nama Pengguna secara Realtime dari Database
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && tvProfileName != null) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        tvProfileName.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Gagal mengambil data
            }
        });

        // Setup Aksi Tombol-Tombol
        if (findViewById(R.id.btnChangeName) != null) {
            findViewById(R.id.btnChangeName).setOnClickListener(v -> showChangeNameDialog());
        }
        if (findViewById(R.id.btnChangePassword) != null) {
            findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        }
        if (findViewById(R.id.btnLogout) != null) {
            findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
        }

        // Tombol Back di Header
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        }

        // Setup klik navigasi bawah di halaman profil (opsional jika dibutuhkan)
        setupBottomNavigation();
    }

    private void showChangeNameDialog() {
        EditText input = new EditText(this);
        input.setPadding(32, 32, 32, 32);
        if (tvProfileName != null) {
            input.setText(tvProfileName.getText().toString());
        }

        new AlertDialog.Builder(this)
                .setTitle("Ganti Nama")
                .setMessage("Masukkan nama baru Anda:")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        mUserRef.child("name").setValue(newName)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(ProfileSettingsActivity.this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                    if (tvProfileName != null) {
                                        tvProfileName.setText(newName); // Memaksa UI memperbarui nama secara instan
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProfileSettingsActivity.this, "Gagal mengubah nama", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showChangePasswordDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Masukkan password baru");
        input.setPadding(32, 32, 32, 32);

        new AlertDialog.Builder(this)
                .setTitle("Ganti Password")
                .setMessage("Silakan masukkan password baru Anda langsung di bawah ini:")
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String newPassword = input.getText().toString().trim();
                    if (newPassword.length() >= 6) {
                        if (currentUser != null) {
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfileSettingsActivity.this, "Password berhasil diubah di aplikasi", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ProfileSettingsActivity.this, "Gagal ganti password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Password minimal harus 6 karakter!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Aplikasi")
                .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void setupBottomNavigation() {
        if (findViewById(R.id.bottomNav) != null) {
            findViewById(R.id.bottomNav).setOnClickListener(v -> {
                // Aksi navigasi ke dashboard bisa ditambahkan di sini jika layout bottomNav diisi komponen
            });
        }
    }
}