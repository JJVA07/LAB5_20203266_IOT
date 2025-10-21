package com.example.lab5_20203266_iot.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.models.Servicio;
import com.example.lab5_20203266_iot.notifications.NotificationHelper;
import com.example.lab5_20203266_iot.notifications.RecordatorioWorker;
import com.example.lab5_20203266_iot.storage.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NuevoServicioActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilMonto, tilFecha;
    private TextInputEditText etNombre, etMonto, etFecha;
    private Spinner spPeriodicidad, spImportancia;
    private MaterialButton btnGuardar;
    private PrefsManager prefs;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Servicio servicioEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_servicio);

        // Toolbar con botón atrás
        MaterialToolbar toolbar = findViewById(R.id.toolbarNuevo);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Enlaces UI
        tilNombre = findViewById(R.id.tilNombre);
        tilMonto = findViewById(R.id.tilMonto);
        tilFecha = findViewById(R.id.tilFecha);
        etNombre = findViewById(R.id.etNombre);
        etMonto = findViewById(R.id.etMonto);
        etFecha = findViewById(R.id.etFecha);
        spPeriodicidad = findViewById(R.id.spPeriodicidad);
        spImportancia = findViewById(R.id.spImportancia);
        btnGuardar = findViewById(R.id.btnGuardar);
        prefs = new PrefsManager(this);

        // Spinner: nombres formateados sin guiones
        spPeriodicidad.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                formatearEnum(Servicio.Periodicidad.values())
        ));
        spImportancia.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                formatearEnum(Servicio.Importancia.values())
        ));

        // DatePicker
        etFecha.setFocusable(false);
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etFecha.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Si es modo edición
        servicioEditar = (Servicio) getIntent().getSerializableExtra("SERVICIO_EDITAR");
        if (servicioEditar != null) {
            precargarDatos(servicioEditar);
            toolbar.setTitle("Editar Servicio");
        }

        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void precargarDatos(Servicio s) {
        etNombre.setText(s.getNombre());
        etMonto.setText(String.valueOf(s.getMonto()));
        etFecha.setText(sdf.format(new java.util.Date(s.getFechaVencimientoMs())));
        if (s.getPeriodicidad() != null)
            spPeriodicidad.setSelection(s.getPeriodicidad().ordinal());
        if (s.getImportancia() != null)
            spImportancia.setSelection(s.getImportancia().ordinal());
    }

    private void guardar() {
        String nombre = safeText(etNombre);
        String montoStr = safeText(etMonto);
        String fechaStr = safeText(etFecha);

        if (nombre.isEmpty() || montoStr.isEmpty() || fechaStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        long fechaMs;
        try {
            sdf.setLenient(false);
            fechaMs = sdf.parse(fechaStr).getTime();
        } catch (ParseException e) {
            Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Convertimos correctamente el valor seleccionado del Spinner al enum
        Servicio.Periodicidad per = Servicio.Periodicidad.values()[spPeriodicidad.getSelectedItemPosition()];
        Servicio.Importancia imp = Servicio.Importancia.values()[spImportancia.getSelectedItemPosition()];

        List<Servicio> lista = prefs.cargarServicios();

        if (servicioEditar == null) {
            Servicio s = new Servicio(nombre, monto, fechaMs, per, imp);
            lista.add(s);
            prefs.guardarServicios(lista);
            programarRecordatorio(s);
            Toast.makeText(this, "Servicio guardado correctamente", Toast.LENGTH_SHORT).show();
        } else {
            for (Servicio s : lista) {
                if (s.getId().equals(servicioEditar.getId())) {
                    s.setNombre(nombre);
                    s.setMonto(monto);
                    s.setFechaVencimientoMs(fechaMs);
                    s.setPeriodicidad(per);
                    s.setImportancia(imp);
                    prefs.guardarServicios(lista);
                    WorkManager.getInstance(this).cancelUniqueWork("recordatorio_" + s.getId());
                    programarRecordatorio(s);
                    Toast.makeText(this, "Servicio editado correctamente", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
        finish();
    }

    private void programarRecordatorio(Servicio s) {
        NotificationHelper.createChannels(this);
        long ahora = System.currentTimeMillis();
        long delay = Math.max(0, s.getFechaVencimientoMs() - ahora - TimeUnit.DAYS.toMillis(1));

        String canal = NotificationHelper.CH_MEDIA;
        if (s.getImportancia() == Servicio.Importancia.ALTA) canal = NotificationHelper.CH_ALTA;
        else if (s.getImportancia() == Servicio.Importancia.BAJA) canal = NotificationHelper.CH_BAJA;

        // ✅ Añadimos periodicidad y fecha al recordatorio
        Data data = new Data.Builder()
                .putString("nombre", s.getNombre())
                .putDouble("monto", s.getMonto())
                .putString("periodicidad", s.getPeriodicidad().name())
                .putLong("fechaMs", s.getFechaVencimientoMs())
                .putString("canal", canal)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(RecordatorioWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueueUniqueWork(
                "recordatorio_" + s.getId(),
                ExistingWorkPolicy.REPLACE,
                work
        );
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private String[] formatearEnum(Enum<?>[] valores) {
        String[] result = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            String nombre = valores[i].name().replace("_", " ").toLowerCase(Locale.getDefault());
            result[i] = nombre.substring(0, 1).toUpperCase(Locale.getDefault()) + nombre.substring(1);
        }
        return result;
    }
}
