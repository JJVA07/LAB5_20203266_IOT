package com.example.lab5_20203266_iot.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.activities.MainActivity;

public class RecordatorioWorker extends Worker {
    public RecordatorioWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String nombre = getInputData().getString("nombre");
        double monto  = getInputData().getDouble("monto", 0);
        String canal  = getInputData().getString("canal");

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(), 0, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext(), canal)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Pago por vencer: " + nombre)
                .setContentText(String.format("Monto: S/ %.2f (vence mañana)", monto))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManagerCompat.from(getApplicationContext())
                .notify((int) System.currentTimeMillis(), nb.build());

        return Result.success();
    }
}
