package com.example.lab5_20203266_iot.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {
    public static final String CH_ALTA  = "canal_alta";
    public static final String CH_MEDIA = "canal_media";
    public static final String CH_BAJA  = "canal_baja";

    public static void createChannels(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);

            NotificationChannel alta = new NotificationChannel(CH_ALTA, "Pagos Importantes",
                    NotificationManager.IMPORTANCE_HIGH);
            alta.setDescription("Recordatorios urgentes de servicios");

            NotificationChannel media = new NotificationChannel(CH_MEDIA, "Pagos Normales",
                    NotificationManager.IMPORTANCE_DEFAULT);
            media.setDescription("Recordatorios estándar");

            NotificationChannel baja = new NotificationChannel(CH_BAJA, "Pagos Menores",
                    NotificationManager.IMPORTANCE_LOW);
            baja.setDescription("Recordatorios silenciosos");

            nm.createNotificationChannel(alta);
            nm.createNotificationChannel(media);
            nm.createNotificationChannel(baja);
        }
    }
}
