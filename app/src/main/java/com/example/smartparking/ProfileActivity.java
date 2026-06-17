package com.example.smartparking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    // Konstanta untuk SharedPreferences
    private static final String PREF_NAME = "SmartParkingPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_PASSWORD = "user_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inisialisasi View
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // A. Memuat Nama dari SharedPreferences saat startup
        loadUserName();

        if (currentUser != null) {
            tvProfileEmail.setText(currentUser.getEmail());
            // Jika SharedPreferences kosong, gunakan inisial dari email sebagai default
            if (sharedPreferences.getString(KEY_NAME, "").isEmpty()) {
                String emailParts = currentUser.getEmail().split("@")[0];
                tvProfileName.setText(emailParts);
                saveUserName(emailParts);
            }
        }

        // Tombol Kembali
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // B. Fitur Ganti Nama (Local Persistence)
        findViewById(R.id.btnChangeName).setOnClickListener(v -> showEditNameDialog());

        // C. Fitur Ganti Password (Local Persistence)
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

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

    // --- FUNGSI GANTI NAMA ---

    private void showEditNameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null);
        EditText etNewName = dialogView.findViewById(R.id.etNewName);

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
                        saveUserName(newName);
                        tvProfileName.setText(newName);
                        Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void saveUserName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    private void loadUserName() {
        String savedName = sharedPreferences.getString(KEY_NAME, "");
        if (!savedName.isEmpty()) {
            tvProfileName.setText(savedName);
        }
    }

    // --- FUNGSI GANTI PASSWORD ---

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Password")
                .setView(dialogView)
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmNewPassword.getText().toString();

                    if (!validateOldPassword(oldPass)) {
                        Toast.makeText(this, "Password lama salah!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPass.length() < 6) {
                        Toast.makeText(this, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    savePassword(newPass);
                    Toast.makeText(this, "Password berhasil diperbarui secara lokal", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private boolean validateOldPassword(String inputOldPass) {
        // Password default jika belum pernah ganti (misal: 123456)
        String currentSavedPass = sharedPreferences.getString(KEY_PASSWORD, "123456");
        return inputOldPass.equals(currentSavedPass);
    }

    private void savePassword(String newPass) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PASSWORD, newPass);
        editor.apply();
    }
}
