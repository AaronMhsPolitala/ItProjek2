package com.example.smartparking;

public class HistoryParkir {

    public String slot;
    public String status;
    public String waktu;

    // Konstruktor kosong wajib untuk Firebase
    public HistoryParkir() {
    }

    public HistoryParkir(String slot, String status, String waktu) {
        this.slot = slot;
        this.status = status;
        this.waktu = waktu;
    }
}