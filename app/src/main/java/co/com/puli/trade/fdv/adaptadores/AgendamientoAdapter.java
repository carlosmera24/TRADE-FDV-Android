package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.TipoInspeccion;

public class AgendamientoAdapter  extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<TipoInspeccion> lista_pdv;
    private CustomFonts fuentes;

    public AgendamientoAdapter(Activity actividad, ArrayList<TipoInspeccion> lista_pdv)
    {
        this.actividad = actividad;
        this.lista_pdv = lista_pdv;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public int getCount() {
        return lista_pdv.size();
    }

    @Override
    public Object getItem(int i) {
        return lista_pdv.get(i);
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
        TipoInspeccion pdv = (TipoInspeccion) getItem(i);
        View vista = view;
        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_agendamiento, null, true);
            holder = new ViewHolder();
            holder.layoutPrincipal = vista.findViewById( R.id.layoutPrincipal );
            holder.tvDesc = vista.findViewById( R.id.tvDescPDV );
            holder.swVisita = vista.findViewById( R.id.swVisita );
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

        holder.tvDesc.setTypeface( fuentes.getRobotoThinFont() );
        holder.swVisita.setTypeface( fuentes.getRobotoThinFont() );

        holder.tvDesc.setText( pdv.getDescripcion() );

        //Definir item seleccionado
        switch ( getItemViewType( i ) )
        {
            case 1: //Seleccionado
                holder.swVisita.setChecked( true );
                break;
            default: //No seleccionado
                holder.swVisita.setChecked( false );
                break;
        }

        //Controlar evento del swtch
        final int post_item = i;
        holder.swVisita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cambiar estado de la valoración, se modifica a su estado contrario.
                TipoInspeccion tmp = (TipoInspeccion) getItem( post_item );
                tmp.setValoracion( tmp.getValoracion() == 1 ? 0 : 1 );
                lista_pdv.set(post_item, tmp);
                notifyDataSetChanged();
            }
        });

        return vista;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.i("agenda", lista_pdv.toString() );
    }

    /**
     * Método encargado de retornar el agendamiento en formato JSON con los ID de los PDV seleccionados
     * */
    public JSONArray agendamientoToJSONArray()
    {
        JSONArray array = new JSONArray();
        for( TipoInspeccion pdv : lista_pdv )
        {
            JSONObject tmp = new JSONObject();
            try
            {
                if( pdv.getValoracion() == 1  )
                {
                    tmp.put("id", "" + pdv.getId());
                    array.put(tmp);
                }
            } catch (JSONException e) {
                Log.e("InspeccionAadapter","InspeccionAdapter.inspeccionToJSONArray.JSONException:"+ e.toString() );
            }
        }
        return array;
    }

    class ViewHolder{
        TextView tvDesc;
        Switch swVisita;
        LinearLayout layoutPrincipal;

    }
}
