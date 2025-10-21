package com.example.lab5_20203266_iot.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

public class Servicio implements Serializable {
    public enum Periodicidad { UNA_VEZ, MENSUAL, BIMESTRAL, TRIMESTRAL, ANUAL }
    public enum Importancia { ALTA, MEDIA, BAJA }

    private String id;                 // para identificar y cancelar trabajos únicos
    private String nombre;
    private double monto;
    private long fechaVencimientoMs;   // epoch ms
    private Periodicidad periodicidad;
    private Importancia importancia;

    public Servicio() {}

    public Servicio(String nombre, double monto, long fechaVencimientoMs,
                    Periodicidad periodicidad, Importancia importancia) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.monto = monto;
        this.fechaVencimientoMs = fechaVencimientoMs;
        this.periodicidad = periodicidad;
        this.importancia = importancia;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getMonto() { return monto; }
    public long getFechaVencimientoMs() { return fechaVencimientoMs; }
    public Periodicidad getPeriodicidad() { return periodicidad; }
    public Importancia getImportancia() { return importancia; }

    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setFechaVencimientoMs(long ms) { this.fechaVencimientoMs = ms; }
    public void setPeriodicidad(Periodicidad p) { this.periodicidad = p; }
    public void setImportancia(Importancia i) { this.importancia = i; }

    /** Próxima fecha tras pagar (o -1 si UNA_VEZ) */
    public long proximaFechaLuegoDePagar() {
        if (periodicidad == Periodicidad.UNA_VEZ) return -1L;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(fechaVencimientoMs);
        switch (periodicidad) {
            case MENSUAL:    c.add(Calendar.MONTH, 1); break;
            case BIMESTRAL:  c.add(Calendar.MONTH, 2); break;
            case TRIMESTRAL: c.add(Calendar.MONTH, 3); break;
            case ANUAL:      c.add(Calendar.YEAR, 1);  break;
            default: break;
        }
        return c.getTimeInMillis();
    }
}
