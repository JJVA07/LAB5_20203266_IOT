package com.example.lab5_20203266_iot.adapters;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.activities.NuevoServicioActivity;
import com.example.lab5_20203266_iot.models.PagoHistorial;
import com.example.lab5_20203266_iot.models.Servicio;
import com.example.lab5_20203266_iot.notifications.NotificationHelper;
import com.example.lab5_20203266_iot.notifications.RecordatorioWorker;
import com.example.lab5_20203266_iot.storage.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ViewHolder> {

    private final Context context;
    private final List<Servicio> lista;
    private final PrefsManager prefs;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ServicioAdapter(Context context, List<Servicio> lista) {
        this.context = context;
        this.lista = lista;
        this.prefs = new PrefsManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Servicio s = lista.get(position);

        h.tvNombre.setText(s.getNombre());
        h.tvMonto.setText(String.format(Locale.getDefault(), "Monto: S/ %.2f", s.getMonto()));
        h.tvFecha.setText("Vence: " + SDF.format(new Date(s.getFechaVencimientoMs())));

        String periodicidadStr = s.getPeriodicidad() != null
                ? formatearEnum(s.getPeriodicidad().name())
                : "Una vez";
        h.tvPeriodicidad.setText("Periodicidad: " + periodicidadStr);

        // --- BOTÓN EDITAR ---
        h.btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(context, NuevoServicioActivity.class);
            i.putExtra("SERVICIO_EDITAR", s);
            context.startActivity(i);
        });

        // --- BOTÓN PAGADO ---
        h.btnPagado.setOnClickListener(v -> {
            long ahora = System.currentTimeMillis();
            PagoHistorial ph = new PagoHistorial(
                    s.getNombre(), s.getMonto(), ahora, s.getFechaVencimientoMs()
            );
            List<PagoHistorial> hist = prefs.cargarHistorial();
            hist.add(ph);
            prefs.guardarHistorial(hist);

            // Calcular nueva fecha
            long prox = s.proximaFechaLuegoDePagar();
            s.setFechaVencimientoMs(prox);
            prefs.guardarServicios(lista);

            // Cancelar recordatorio viejo y crear nuevo si corresponde
            WorkManager.getInstance(context).cancelUniqueWork("recordatorio_" + s.getId());
            if (prox > 0) scheduleReminder(s);

            Toast.makeText(context, "Pago registrado", Toast.LENGTH_SHORT).show();
            notifyItemChanged(h.getAdapterPosition());
        });

        // --- BOTÓN ELIMINAR ---
        h.btnEliminar.setOnClickListener(v -> {
            WorkManager.getInstance(context).cancelUniqueWork("recordatorio_" + s.getId());
            lista.remove(position);
            prefs.guardarServicios(lista);
            notifyItemRemoved(position);
            Toast.makeText(context, "Servicio eliminado", Toast.LENGTH_SHORT).show();
        });
    }

    private void scheduleReminder(Servicio s) {
        NotificationHelper.createChannels(context);

        String canal = NotificationHelper.CH_MEDIA;
        if (s.getImportancia() == Servicio.Importancia.ALTA) canal = NotificationHelper.CH_ALTA;
        else if (s.getImportancia() == Servicio.Importancia.BAJA) canal = NotificationHelper.CH_BAJA;

        long delay = Math.max(0, s.getFechaVencimientoMs() - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));

        // ✅ aseguramos que no sea null
        String periodicidadFormateada = s.getPeriodicidad() != null
                ? formatearEnum(s.getPeriodicidad().name())
                : "Una vez";

        Data data = new Data.Builder()
                .putString("nombre", s.getNombre())
                .putDouble("monto", s.getMonto())
                .putString("periodicidad", periodicidadFormateada)
                .putLong("fechaMs", s.getFechaVencimientoMs())
                .putString("canal", canal)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(RecordatorioWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "recordatorio_" + s.getId(),
                ExistingWorkPolicy.REPLACE,
                work
        );
    }

    private String formatearEnum(String valor) {
        String s = valor.replace("_", " ").toLowerCase(Locale.getDefault());
        return s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMonto, tvFecha, tvPeriodicidad;
        Button btnEditar, btnPagado, btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPeriodicidad = itemView.findViewById(R.id.tvPeriodicidad);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnPagado = itemView.findViewById(R.id.btnPagado);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
