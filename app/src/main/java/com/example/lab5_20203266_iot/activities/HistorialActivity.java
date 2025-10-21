package com.example.lab5_20203266_iot.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.adapters.HistorialAdapter;
import com.example.lab5_20203266_iot.models.PagoHistorial;
import com.example.lab5_20203266_iot.storage.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Toolbar con botón atrás
        MaterialToolbar toolbar = findViewById(R.id.toolbarHistorial);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI
        recyclerView = findViewById(R.id.recyclerHistorial);
        tvEmpty = findViewById(R.id.tvEmpty);
        prefs = new PrefsManager(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cargar historial
        List<PagoHistorial> lista = prefs.cargarHistorial();

        if (lista == null || lista.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new HistorialAdapter(lista));
        }
    }
}
