package com.example.lab5_20203266_iot.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordatorioWorker extends Worker {

    public RecordatorioWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();

        String nombre = getInputData().getString("nombre");
        double monto = getInputData().getDouble("monto", 0);
        String canal = getInputData().getString("canal");
        String periodicidad = getInputData().getString("periodicidad");
        long fechaMs = getInputData().getLong("fechaMs", System.currentTimeMillis());

        if (periodicidad == null || periodicidad.trim().isEmpty()) {
            periodicidad = "Una vez";
        }

        String fechaFormateada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(fechaMs));

        NotificationHelper.createChannels(ctx);

        Intent i = new Intent(ctx, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        var nb = NotificationHelper.buildNotification(
                        ctx, canal, nombre, monto, fechaFormateada, periodicidad)
                .setContentIntent(pi)
                .setColor(ContextCompat.getColor(ctx, R.color.colorPrimary));

        NotificationManagerCompat.from(ctx)
                .notify((int) System.currentTimeMillis(), nb.build());

        return Result.success();
    }
}
