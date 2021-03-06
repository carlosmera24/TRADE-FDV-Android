package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.BaseAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.DescargarImagenTask;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.Producto;


public class PedidoPDVAdapter extends  BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<Producto> lista_productos, productos_pdv;
    private CustomFonts fuentes;
    String id_fdv;

    public PedidoPDVAdapter(Activity actividad, ArrayList<Producto> lista_productos, String id_fdv)
    {
        this.actividad = actividad;
        this.lista_productos = lista_productos;
        this.id_fdv = id_fdv;
        productos_pdv = lista_productos;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public int getCount() {
        return lista_productos.size();
    }

    @Override
    public Object getItem(int position) {
        return lista_productos.get( position );
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return ( (Producto) getItem(position) ).getCatidad();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Producto producto = (Producto) getItem( position );
        View vista = convertView;

        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_pedido_pdv, null, true);
            holder = new ViewHolder();
            holder.layoutPrincipal = vista.findViewById( R.id.layoutPrincipal );
            holder.tvNombre = vista.findViewById( R.id.tvDescProducto );
            holder.tvCantidad = vista.findViewById( R.id.tvCantidad );
            holder.ibInfo = vista.findViewById( R.id.ibInfo );
            vista.setTag( holder );
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        holder.tvNombre.setTypeface( fuentes.getRobotoThinFont() );
        holder.tvCantidad.setTypeface( fuentes.getRobotoThinFont() );

        holder.tvNombre.setText( producto.getNombre() );
        holder.tvCantidad.setText( "" + producto.getCatidad() );

        //Agregar click sobre el item
        vista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSeleccionCantidad( producto );
            }
        });

        //Agregar click sobre boton info
        holder.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarInformacionProducto( producto );
            }
        });

        return vista;
    }


    /**
     * M??todo encargado de visualizar el dialogo para selecci??n de la cantidad utilizando NumberPicker
     * */
    public void mostrarDialogoSeleccionCantidad(final Producto producto )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( actividad );

        View view = inflater.inflate(R.layout.dialogo_number_picker, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView( view );
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = view.findViewById( R.id.tvTitulo );
        tvTitulo.setText( producto.getNombre() );
        tvTitulo.setTypeface( fuentes.getBoldFont() );

        //Obtener el valor m??ximo para la cantidad desde los par??metros generales
        int maxValue = 100;
        final GlobalParametrosGenerales global_param = (GlobalParametrosGenerales) actividad.getApplicationContext();
        if( global_param.existenParametros() && global_param.getValue("cantidad_maxima_producto_exhibidos", 3) !=  null )
        {
            maxValue = Integer.parseInt( global_param.getValue("cantidad_maxima_producto_exhibidos", 3) );
        }

        final NumberPicker nbPicker = view.findViewById( R.id.nbPicker );
        nbPicker.setMinValue( 0 );
        nbPicker.setMaxValue( maxValue );
        nbPicker.setValue( producto.getCatidad() );

        //Estilo para el NumberPicker
        for( int i=0; i < nbPicker.getChildCount(); i++ )
        {
            View child = nbPicker.getChildAt(i);
            if( child instanceof EditText)
            {
                try
                {
                    Field select = nbPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    select.setAccessible(true);
                    ( (Paint) select.get( nbPicker) ).setColor( ContextCompat.getColor( actividad, R.color.black ) );
                    ( (EditText) child ).setTextColor( ContextCompat.getColor( actividad, R.color.black ) );
                    nbPicker.invalidate();
                }catch(Exception e){ Log.e("NumberPicker","PedidoPDVAdapter.mostrarDialogoSeleccionCantidad: "+e.toString() ); }
            }
        }

        Button btGuardar = view.findViewById(  R.id.btGuardar );
        btGuardar.setTypeface( fuentes.getRobotoThinFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( nbPicker.getValue() != producto.getCatidad() ) {
                    producto.setCatidad( nbPicker.getValue() );
                    notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * M??todo encargado de visualizar la informaci??n del producto
     * */
    public void mostrarInformacionProducto( Producto producto )
    {
        //Descargar imagen usuario
        Bitmap img_producto = null;
        img_producto = getImageProducto( producto.getUrl_imagen() );


        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder( actividad );

        LayoutInflater inflater = actividad.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_info_producto, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final android.app.AlertDialog dialog = builder.create();

        ImageView ivProducto = view.findViewById( R.id.ivProducto );
        if( img_producto != null )
        {
            ivProducto.setImageDrawable( new BitmapDrawable( actividad.getResources(), img_producto) );
        }

        TextView tvDesc = view.findViewById( R.id.tvNombre );
        tvDesc.setTypeface( fuentes.getRobotoThinFont() );
        tvDesc.setText( producto.getNombre() );

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.show();
    }

    /**
     * M??todo encargado de descargar la imagen del producto
     * */
    public Bitmap getImageProducto( String url)
    {
        Bitmap img = null;
        if( url != null )
        {
            DescargarImagenTask dit = new DescargarImagenTask( actividad, actividad.getString( R.string.txt_msg_descarga_imagen ) );
            String URL_IMAGEN = actividad.getString( R.string.url_server_images ) + url;
            try
            {
                img = dit.execute(URL_IMAGEN).get();
            }catch (Exception e)
            {
                Log.e("Exception", "LoginActivity.getImagenUsuario:" + e.toString() );
            }
        }

        return img;
    }

    /**
     * M??todo encargado de filtrar los productos de acuerdo a la categor??a seleccionada
     * */
    public void filtrarProductos( String id_categoria)
    {
        if( id_categoria.equals( "0" ) )//Filtrar todos
        {
            lista_productos = productos_pdv;
        }else{
            ArrayList<Producto> tmp = new ArrayList<>();
            for(  Producto producto : productos_pdv )
            {
                if( producto.getId_categoria().equals( id_categoria ) )
                {
                    tmp.add( producto );
                }
            }
            lista_productos = tmp;
        }
        notifyDataSetChanged();
    }

    /**
     * M??todo encargado de retornar la inspecci??n en formato JSONArray
     * Solo se incluyen productos con cantidades superiores a 0
     * */
    public JSONArray productosToJSONArray()
    {
        JSONArray array = new JSONArray();
        for( Producto producto : productos_pdv )
        {
            JSONObject tmp = new JSONObject();
            try
            {
                //Agregar solo los productos con cantidades superiores a 0
                int cant = producto.getCatidad();
                if( cant > 0 ) {
                    tmp.put("id", "" + producto.getId());
                    tmp.put("cantidad", "" + producto.getCatidad());
                    array.put(tmp);
                }
            } catch (JSONException e) {
                Log.e("PedidoPDVAdapter","PedidoPDVAdapter.productosToJSONArray.JSONException:"+ e.toString() );
            }
        }
        return array;
    }

    /**
     * Clase que contiene cada uno de los objectos del layout item_lista_control_pdv.xml
     * */
    static class ViewHolder
    {
        LinearLayout layoutPrincipal;
        TextView tvNombre, tvCantidad;
        ImageButton ibInfo;
    }
}
