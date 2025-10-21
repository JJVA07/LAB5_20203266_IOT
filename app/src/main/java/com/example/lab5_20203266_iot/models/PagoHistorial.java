package com.example.lab5_20203266_iot.models;

public class PagoHistorial {
    private String nombreServicio;
    private double monto;
    private long fechaPagoMs;
    private long fechaVencimientoAnteriorMs;
    private long anticipacionMs; // positivo si pagó antes

    public PagoHistorial() {}

    public PagoHistorial(String nombreServicio, double monto, long fechaPagoMs, long fechaVencimientoAnteriorMs) {
        this.nombreServicio = nombreServicio;
        this.monto = monto;
        this.fechaPagoMs = fechaPagoMs;
        this.fechaVencimientoAnteriorMs = fechaVencimientoAnteriorMs;
        this.anticipacionMs = fechaVencimientoAnteriorMs - fechaPagoMs;
    }

    public String getNombreServicio() { return nombreServicio; }
    public double getMonto() { return monto; }
    public long getFechaPagoMs() { return fechaPagoMs; }
    public long getFechaVencimientoAnteriorMs() { return fechaVencimientoAnteriorMs; }
    public long getAnticipacionMs() { return anticipacionMs; }
}
