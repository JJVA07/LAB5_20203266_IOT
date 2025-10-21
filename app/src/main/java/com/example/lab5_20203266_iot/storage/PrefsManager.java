package com.example.lab5_20203266_iot.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lab5_20203266_iot.models.PagoHistorial;
import com.example.lab5_20203266_iot.models.Servicio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefsManager {
    private static final String PREFS_NAME = "servicios_prefs";
    private static final String K_SERVICIOS = "lista_servicios";
    private static final String K_HISTORIAL = "historial";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public PrefsManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<Servicio> cargarServicios() {
        String json = prefs.getString(K_SERVICIOS, "[]");
        Type t = new TypeToken<ArrayList<Servicio>>(){}.getType();
        return gson.fromJson(json, t);
    }

    public void guardarServicios(List<Servicio> lista) {
        prefs.edit().putString(K_SERVICIOS, gson.toJson(lista)).apply();
    }

    public List<PagoHistorial> cargarHistorial() {
        String json = prefs.getString(K_HISTORIAL, "[]");
        Type t = new TypeToken<ArrayList<PagoHistorial>>(){}.getType();
        return gson.fromJson(json, t);
    }

    public void guardarHistorial(List<PagoHistorial> lista) {
        prefs.edit().putString(K_HISTORIAL, gson.toJson(lista)).apply();
    }
}
