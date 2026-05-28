package com.example.smartparking;

public class HistoryParkir {

    public String slot;
    public String waktuMasuk;
    public String waktuKeluar;
    public String totalWaktu;

    // Konstruktor kosong wajib untuk Firebase
    public HistoryParkir() {
    }

    public HistoryParkir(
                         String slot,
                         String waktuMasuk,
                         String waktuKeluar,
                         String totalWaktu) {

        this.slot = slot;
        this.waktuMasuk = waktuMasuk;
        this.waktuKeluar = waktuKeluar;
        this.totalWaktu = totalWaktu;
    }
}