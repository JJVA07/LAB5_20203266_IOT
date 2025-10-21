package com.example.lab5_20203266_iot.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.adapters.ServicioAdapter;
import com.example.lab5_20203266_iot.models.Servicio;
import com.example.lab5_20203266_iot.notifications.NotificationHelper;
import com.example.lab5_20203266_iot.storage.PrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServicioAdapter adapter;
    private PrefsManager prefs;
    private Button btnHistorial;
    private TextView tvEmpty;

    // Lanzador de permiso para notificaciones (Android 13+)
    private final ActivityResultLauncher<String> requestNotifPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear canales de notificación y pedir permiso si aplica
        NotificationHelper.createChannels(this);
        pedirPermisoPostNotificationsSiHaceFalta();

        prefs = new PrefsManager(this);

        recyclerView = findViewById(R.id.recyclerServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnHistorial = findViewById(R.id.btnHistorial);
        tvEmpty = findViewById(R.id.tvEmpty);

        cargarLista();

        // Botón flotante para agregar servicio
        FloatingActionButton fab = findViewById(R.id.fabAgregar);
        fab.setOnClickListener(v -> startActivity(new Intent(this, NuevoServicioActivity.class)));

        // Botón para ver historial
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(this, HistorialActivity.class)));
    }

    /** Carga la lista de servicios desde SharedPreferences */
    private void cargarLista() {
        List<Servicio> listaServicios = prefs.cargarServicios();

        adapter = new ServicioAdapter(this, listaServicios);
        recyclerView.setAdapter(adapter);

        // Mostrar mensaje si no hay servicios
        if (listaServicios == null || listaServicios.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** Recargar lista al volver del formulario */
    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    /** Solicita permiso POST_NOTIFICATIONS si es Android 13+ */
    private void pedirPermisoPostNotificationsSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
