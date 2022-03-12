package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.TipoInspeccion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by carlos on 28/07/16.
 */
public class InspeccionAdapter extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<TipoInspeccion> lista_inspeccion;
    private CustomFonts fuentes;

    public InspeccionAdapter(Activity actividad, ArrayList<TipoInspeccion> lista_inspeccion)
    {
        this.actividad = actividad;
        this.lista_inspeccion = lista_inspeccion;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public int getCount() {
        return lista_inspeccion.size();
    }

    @Override
    public Object getItem(int i) {
        return lista_inspeccion.get(i);
    }

    @Override
    public long getItemId(int i) {
        return ( (TipoInspeccion) getItem(i) ).getId();
    }

    @Override
    public int getItemViewType(int position)
    {
        TipoInspeccion tmp = (TipoInspeccion) getItem( position );
        return tmp.getValoracion();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        TipoInspeccion inspeccion = (TipoInspeccion) getItem(i);
        View vista = view;
        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_inspeccion, null, true);
            holder = new ViewHolder();
            holder.layoutPrincipal = (RelativeLayout) vista.findViewById( R.id.layoutPrincipal );
            holder.tvDescripcion = (TextView) vista.findViewById( R.id.tvDescInspeccion );
            holder.rbBien = (RadioButton) vista.findViewById( R.id.rbBien);
            holder.rbMal = (RadioButton) vista.findViewById( R.id.rbMal );
            vista.setTag(holder);
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        //Establecer fondo del item
        if( i % 2 != 0 )
        {
            holder.layoutPrincipal.setBackgroundResource( R.color.gris2);
        }else{
            holder.layoutPrincipal.setBackgroundResource( R.color.white);
        }

        holder.tvDescripcion.setTypeface( fuentes.getRobotoThinFont() );
        holder.rbBien.setTypeface( fuentes.getRobotoThinFont() );
        holder.rbMal.setTypeface( fuentes.getRobotoThinFont() );

        holder.tvDescripcion.setText( inspeccion.getDescripcion() );

        //Definir item seleccionado
        switch ( getItemViewType( i ) )
        {
            case 1: //Bien
                    holder.rbBien.setChecked( true );
                    holder.rbMal.setChecked( false );
                break;
            case 0: //Mal
                    holder.rbMal.setChecked( true );
                    holder.rbBien.setChecked( false );
                break;
            default: //Eliminar seleccion
                    holder.rbBien.setChecked( false );
                    holder.rbMal.setChecked( false );
                break;
        }

        //Controlar evento del radio button seleccionado
        final int post_item = i;
        holder.rbBien.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               TipoInspeccion tmp = (TipoInspeccion) getItem( post_item );
               tmp.setValoracion(1);
               lista_inspeccion.set(post_item, tmp);
               notifyDataSetChanged();
           }
         });
        holder.rbMal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TipoInspeccion tmp = (TipoInspeccion) getItem( post_item );
                tmp.setValoracion(0);
                lista_inspeccion.set(post_item, tmp);
                notifyDataSetChanged();
            }
        });

        return vista;
    }

    /**
     * Método encargado de validar si todos los campos de la lista han sido seleccionados
     * */
    public boolean inspeccionFinalizada()
    {
        boolean res = true;
        for( TipoInspeccion inspec : lista_inspeccion )
        {
            if( inspec.getValoracion() == 3)
            {
                res = false;
                break;
            }
        }
        return res;
    }

    /**
     * Método encargado de retornar la inspección en formato JSON
     * */
    public JSONArray inspeccionToJSONArray()
    {
        JSONArray array = new JSONArray();
        for( TipoInspeccion inspec : lista_inspeccion )
        {
            JSONObject tmp = new JSONObject();
            try
            {
                tmp.put("id", ""+inspec.getId());
                tmp.put("inspeccion", ""+inspec.getValoracion());
                array.put( tmp );
            } catch (JSONException e) {
                Log.e("InspeccionAadapter","InspeccionAdapter.inspeccionToJSONArray.JSONException:"+ e.toString() );
            }
        }
        return array;
    }

    public ArrayList<TipoInspeccion> getLista_inspeccion()
    {
        return lista_inspeccion;
    }

    class ViewHolder
    {
        TextView tvDescripcion;
        RelativeLayout layoutPrincipal;
        RadioButton rbBien, rbMal;
    }

}
