package com.example.lab5_20203266_iot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20203266_iot.R;
import com.example.lab5_20203266_iot.models.PagoHistorial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private final List<PagoHistorial> lista;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistorialAdapter(List<PagoHistorial> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PagoHistorial p = lista.get(position);

        h.tvServicio.setText(p.getNombreServicio());
        h.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", p.getMonto()));
        h.tvFechaPago.setText("Pago: " + SDF.format(new Date(p.getFechaPagoMs())));
        h.tvVencAnt.setText("Venc. anterior: " + SDF.format(new Date(p.getFechaVencimientoAnteriorMs())));
        h.tvAnticipacion.setText(formatearAnticipacion(p.getAnticipacionMs()));
    }

    private String formatearAnticipacion(long anticipacionMs) {
        long dias = TimeUnit.MILLISECONDS.toDays(Math.abs(anticipacionMs));
        if (anticipacionMs > 0) {
            return "Pagado " + dias + (dias==1 ? " día" : " días") + " antes";
        } else if (anticipacionMs < 0) {
            return "Pagado " + dias + (dias==1 ? " día" : " días") + " después";
        } else {
            return "Pagado el mismo día";
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServicio, tvMonto, tvFechaPago, tvVencAnt, tvAnticipacion;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServicio     = itemView.findViewById(R.id.tvServicio);
            tvMonto        = itemView.findViewById(R.id.tvMonto);
            tvFechaPago    = itemView.findViewById(R.id.tvFechaPago);
            tvVencAnt      = itemView.findViewById(R.id.tvVencAnt);
            tvAnticipacion = itemView.findViewById(R.id.tvAnticipacion);
        }
    }
}
