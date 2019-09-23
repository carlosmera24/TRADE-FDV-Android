package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.actividades.ControlPDVActivity;
import co.com.puli.trade.fdv.actividades.DetallePedidoActivity;
import co.com.puli.trade.fdv.actividades.NuevoPedidoActivity;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.Pedido;

public class MiPedidoAdapter extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<Pedido> lista_pedidos;
    private CustomFonts fuentes;
    String id_fdv, id_pdv;

    public MiPedidoAdapter(Activity actividad, ArrayList<Pedido> lista_pedidos, String id_fdv, String id_pdv )
    {
        this.actividad = actividad;
        this.lista_pedidos = lista_pedidos;
        this.id_fdv = id_fdv;
        this.id_pdv = id_pdv;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public int getCount() {
        return lista_pedidos.size();
    }

    @Override
    public Object getItem(int position) {
        return lista_pedidos.get( position );
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return ( (Pedido) getItem(position) ).getCantidad();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Pedido pedido = (Pedido) getItem( position );
        View vista = convertView;

        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_pedido_pdv, null, true);
            holder = new ViewHolder();
            holder.layoutPrincipal = vista.findViewById( R.id.layoutPrincipal );
            holder.tvFecha = vista.findViewById( R.id.tvDescProducto );
            holder.tvCantidad = vista.findViewById( R.id.tvCantidad );
            holder.ibInfo = vista.findViewById( R.id.ibInfo );
            vista.setTag( holder );
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        holder.tvFecha.setTypeface( fuentes.getRobotoThinFont() );
        holder.tvCantidad.setTypeface( fuentes.getRobotoThinFont() );

        holder.tvFecha.setText( pedido.getFecha() );
        holder.tvCantidad.setText( "" + pedido.getCantidad() );

        //Agregar click sobre el item
        vista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInformacionPedido( pedido );
            }
        });

        //Agregar click sobre boton info
        holder.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInformacionPedido( pedido );
            }
        });

        return vista;
    }

    /**
     * Método encargado de visualizar la información del pedido
     * */
    public void mostrarInformacionPedido( Pedido pedido )
    {
        Intent intent = new Intent( actividad, DetallePedidoActivity.class);
        intent.putExtra( "id_fdv", id_fdv );
        intent.putExtra( "id_pdv", id_pdv );
        intent.putExtra( "fecha", pedido.getFecha() );
        actividad.startActivity( intent );
    }

    /**
     * Clase que contiene cada uno de los objectos del layout item_lista_control_pdv.xml
     * */
    static class ViewHolder
    {
        LinearLayout layoutPrincipal;
        TextView tvFecha, tvCantidad;
        ImageButton ibInfo;
    }
}
