package com.example.lab5_20203266_iot.adapters;

import android.content.Context;
import android.content.Intent;
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
        h.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", s.getMonto()));
        h.tvFecha.setText("Vence: " + SDF.format(new Date(s.getFechaVencimientoMs())));

        // --- BOTÓN EDITAR ---
        h.btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(context, NuevoServicioActivity.class);
            i.putExtra("SERVICIO_EDITAR", s); // enviamos el objeto completo
            context.startActivity(i);
        });

        // --- BOTÓN PAGADO ---
        h.btnPagado.setOnClickListener(v -> {
            long ahora = System.currentTimeMillis();

            // Guardar en historial
            PagoHistorial ph = new PagoHistorial(
                    s.getNombre(), s.getMonto(), ahora, s.getFechaVencimientoMs()
            );
            List<PagoHistorial> hist = prefs.cargarHistorial();
            hist.add(ph);
            prefs.guardarHistorial(hist);

            // Próxima fecha
            long prox = s.proximaFechaLuegoDePagar();
            s.setFechaVencimientoMs(prox);
            prefs.guardarServicios(lista);

            // Reprogramar recordatorio (o cancelar)
            WorkManager.getInstance(context).cancelUniqueWork("recordatorio_" + s.getId());
            if (prox > 0) scheduleReminder(s);

            notifyItemChanged(h.getAdapterPosition());
        });
    }

    private void scheduleReminder(Servicio s) {
        NotificationHelper.createChannels(context);

        String canal = NotificationHelper.CH_MEDIA;
        if (s.getImportancia() == Servicio.Importancia.ALTA) canal = NotificationHelper.CH_ALTA;
        else if (s.getImportancia() == Servicio.Importancia.BAJA) canal = NotificationHelper.CH_BAJA;

        long delay = Math.max(0, s.getFechaVencimientoMs() - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));

        Data data = new Data.Builder()
                .putString("nombre", s.getNombre())
                .putDouble("monto", s.getMonto())
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

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMonto, tvFecha;
        Button btnEditar, btnPagado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre  = itemView.findViewById(R.id.tvNombre);
            tvMonto   = itemView.findViewById(R.id.tvMonto);
            tvFecha   = itemView.findViewById(R.id.tvFecha);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnPagado = itemView.findViewById(R.id.btnPagado);
        }
    }
}
