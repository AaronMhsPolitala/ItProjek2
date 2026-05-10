package com.example.smartparking;

public class HistoryParkir {


    String slot;
    String waktuMasuk;
    String waktuKeluar;
    String totalWaktu;

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