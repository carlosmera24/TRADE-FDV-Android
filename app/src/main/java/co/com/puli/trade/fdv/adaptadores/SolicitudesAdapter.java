package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.SolicitudesServicio;
import co.com.puli.trade.fdv.actividades.FUEC_RegistroActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Adaptador para los Items de las Solicitudes
 * Created by carlos on 10/10/16.
 */

public class SolicitudesAdapter extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<SolicitudesServicio> solicitudes;
    private CustomFonts fuentes;
    private String id_vehiculo, id_usuario;

    public SolicitudesAdapter(Activity actividad, ArrayList<SolicitudesServicio> solicitudes, String id_vehiculo, String id_usuario)
    {
        this.actividad = actividad;
        this.solicitudes = solicitudes;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
        this.id_vehiculo = id_vehiculo;
        this.id_usuario = id_usuario;
    }

    @Override
    public int getCount() {
        return solicitudes.size();
    }

    @Override
    public Object getItem(int i) {
        return solicitudes.get( i );
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup)
    {
        SolicitudesServicio solicitud = (SolicitudesServicio) getItem( pos );
        View vista = view;
        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_solicitudes, null, true);
            holder = new ViewHolder();
            holder.tvFecha = (TextView) vista.findViewById( R.id.tvFecha );
            holder.tvTipo = (TextView) vista.findViewById( R.id.tvTipo );
            holder.tvDesc = (TextView) vista.findViewById( R.id.tvDesc );
            holder.btSolicitud = (Button) vista.findViewById( R.id.btnSolicitud );
            vista.setTag( holder );
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        holder.tvFecha.setTypeface( fuentes.getRobotoThinFont() );
        holder.tvTipo.setTypeface( fuentes.getBoldFont() );
        holder.tvDesc.setTypeface( fuentes.getRobotoThinFont() );
        holder.btSolicitud.setTypeface( fuentes.getBoldFont() );

        //Fecha
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar fecha = Calendar.getInstance();
            fecha.setTime( sdf.parse( solicitud.getFecha() ) );
            sdf = new SimpleDateFormat("MMM dd yyyy/HH:mm");
            holder.tvFecha.setText( sdf.format(fecha.getTime()) );
        }catch (ParseException e)
        {
            e.printStackTrace();
        }

        holder.tvTipo.setText( solicitud.getDesc_tipo_servicio() );
        holder.tvDesc.setText( solicitud.getDesc() );

        final String id_solicitud = solicitud.getId();
        holder.btSolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(actividad, FUEC_RegistroActivity.class);
                intent.putExtra( "id_vehiculo", id_vehiculo );
                intent.putExtra( "id_usuario", id_usuario );
                intent.putExtra( "id_solicitud", id_solicitud );
                actividad.startActivity( intent );
            }
        });

        return vista;
    }

    class ViewHolder
    {
        TextView tvFecha, tvTipo, tvDesc;
        Button btSolicitud;
    }
}
