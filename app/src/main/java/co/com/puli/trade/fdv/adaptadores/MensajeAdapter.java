package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.Mensaje;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Adaptador para los items de la lista de Mensajes
 * Created by carlos on 26/11/15.
 */
public class MensajeAdapter extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<Mensaje> mensajes;
    private CustomFonts fuentes;

    public MensajeAdapter(Activity actividad, ArrayList<Mensaje> mensajes)
    {
        this.actividad = actividad;
        this.mensajes = mensajes;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public int getCount() {
        return mensajes.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vista = convertView;
        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_mensaje, null, true);
            holder = new ViewHolder();
            holder.tvFecha = (TextView) vista.findViewById( R.id.tvMsgFecha );
            holder.tvTexto = (TextView) vista.findViewById( R.id.tvMsgTexto );
            vista.setTag(holder);
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        //Cambiar color de fondo para las filas pares, position +1 para iniciar desde 0 en vez de 1
        if( position+1 % 2 == 0 )
        {
            vista.setBackgroundColor( ContextCompat.getColor( actividad, R.color.gris ) );
        }

        Mensaje mensaje = mensajes.get(position);
        //Establecer contenido como HTML si lo tiene
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
        {
            holder.tvTexto.setText(Html.fromHtml(mensaje.getTexto(), Html.FROM_HTML_MODE_COMPACT));
        }else {
            holder.tvTexto.setText(Html.fromHtml(mensaje.getTexto()));
        }
        holder.tvTexto.setClickable( true ); //Habilitar click
        holder.tvTexto.setMovementMethod( LinkMovementMethod.getInstance() );
        holder.tvFecha.setTypeface(fuentes.getRobotoThinFont());
        holder.tvTexto.setTypeface( fuentes.getRobotoThinFont() );

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar fecha = Calendar.getInstance();
            fecha.setTime( sdf.parse(mensaje.getFecha()) );
            sdf = new SimpleDateFormat("dd MMM HH:mm");
            holder.tvFecha.setText(sdf.format(fecha.getTime()));
        }catch (ParseException e)
        {
            e.printStackTrace();
        }

        return vista;
    }

    /**
     * Clase que contiene cada uno de los objectos del layout item_lista_mensaje.xml*/
    static class ViewHolder
    {
        TextView tvFecha, tvTexto;
    }

}
