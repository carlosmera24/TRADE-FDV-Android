package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.adaptadores.PedidoPDVAdapter;
import co.com.puli.trade.fdv.clases.Categoria;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.PDV;
import co.com.puli.trade.fdv.clases.Producto;
import co.com.puli.trade.fdv.clases.Utilidades;

public class NuevoPedidoActivity extends AppCompatActivity {
    private CustomFonts fuentes;
    private HashMap<String, String> postParam;
    private String URL_LISTA_PDV, URL_LISTA_PRODUCTOS, URL_REGISTRO_PEDIDO_PDV, id_fdv;
    private Spinner spPDV, spCategorias;
    private ListView listaProductos;
    private PedidoPDVAdapter adaptadorPedidoPDV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pedido);

        fuentes = new CustomFonts(getAssets());

        URL_LISTA_PDV = getString(R.string.url_server_backend) + "lista_pdv_fdv.jsp";
        URL_LISTA_PRODUCTOS = getString(R.string.url_server_backend) + "consultar_productos_empresa.jsp";
        URL_REGISTRO_PEDIDO_PDV = getString(R.string.url_server_backend) + "registrar_pedido_pdv.jsp";

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_fdv = bundle.getString("id_fdv");

        spPDV = findViewById( R.id.spPDV );
        spCategorias = findViewById( R.id.spCategorias );
        listaProductos = findViewById( R.id.lvListaProductos );

        Button btFiltrar = findViewById( R.id.btFiltrar );
        btFiltrar.setTypeface( fuentes.getRobotoThinFont() );
        btFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Categoria cat = (Categoria) spCategorias.getSelectedItem();
                adaptadorPedidoPDV.filtrarProductos( cat.getId() );
            }
        });

        Button btCancelar = findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button btGuardar = findViewById( R.id.btGuardar );
        btGuardar.setTypeface( fuentes.getRobotoThinFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Categoria pdv = (Categoria) spPDV.getSelectedItem();
                TextView tvPDV = (TextView) spPDV.getSelectedView();
                tvPDV.setTextColor( ContextCompat.getColor( NuevoPedidoActivity.this, R.color.black ) );
                if( pdv.getId() == "0" )
                {
                    tvPDV.setTextColor( Color.RED );
                    tvPDV.setError( getString(R.string.txt_campo_requerido) );
                }else{
                    JSONArray productos = adaptadorPedidoPDV.productosToJSONArray();
                    if( productos.length() > 0 )//Enviar registro de pedido
                    {
                        //Verificar disponibilidad de Red
                        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
                        {
                            //Preparar los parámetros para el registro del pedido
                            postParam = new HashMap<>();
                            postParam.put("id_pdv", pdv.getId() );
                            postParam.put("id_fdv", id_fdv );
                            postParam.put("pedido", productos.toString() );
                            RegistrarPedidoPDVTask rpt = new RegistrarPedidoPDVTask();
                            rpt.execute( URL_REGISTRO_PEDIDO_PDV );
                        }else{
                            new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
                        }
                    }else//No se asignó cantidad a ningún producto
                    {
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Pedido PDV", getString( R.string.txt_msg_productos_control_pdv_no_cantidad ), false);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable(getResources(), imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0));
            getSupportActionBar().setHomeAsUpIndicator(iconBack);

            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                //Consultar listado de PDV
                if (id_fdv != null && !id_fdv.equals("")) {
                    postParam = new HashMap<>();
                    postParam.put("id_fdv", id_fdv);

                    ConsultarListaPdvTask cpdv = new ConsultarListaPdvTask();
                    cpdv.execute(URL_LISTA_PDV);
                } else {
                    new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", getString(R.string.txt_msg_error_id_ruta), true);
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            }

        }catch (Exception e) {
            Log.e("Exception","NuevoPedidoActivity.onResume.Exception:"+e.toString() );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarMemoria();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     */
    public void limpiarMemoria() {
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            System.gc();
        } catch (Exception e) {
        }
    }

    /**
     * Método encargado de retornar el nombre de la empresa a partir de los parámetros globales
     * @return String nombre de la empresa, null si hay error
     * */
    public String getNombreEmpresa()
    {
        try {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            JSONObject empresa = parametros.getEmpresa();
            if (empresa != null) {
                return empresa.getString("nombre");
            }else{
                return null;
            }
        }catch( JSONException e )
        {
            Log.e("JSONException","PDVRutaAdapter.getNombreColegio.JSONException:"+ e.toString() );
            return null;
        }catch(Exception e )
        {
            Log.e("Exception","PDVRutaAdapter.getNombreColegio.Exception:"+ e.toString() );
            return null;
        }
    }

    /**
     * Método encargado de cargar y visualizar el listado de categorías y productos
     * */
    public  void cargarListas( ArrayList<Producto> list_productos, List<Categoria> list_categorias )
    {
        //Construir spinner categorías
        ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(NuevoPedidoActivity.this, android.R.layout.simple_spinner_dropdown_item, list_categorias );
        spCategorias.setAdapter( catAdapter );

        adaptadorPedidoPDV = new PedidoPDVAdapter( NuevoPedidoActivity.this, list_productos, id_fdv);
        listaProductos.setAdapter(adaptadorPedidoPDV);
    }

    /**
     * Método encargado de cargar el listado de PDV, filtrando solo los que tienen CheckIn realizado,
     * Adicionalmente consulta los productos de la empresa*/
    public void cargarListaPDV( JSONArray arrayPDV )
    {
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        ArrayList<PDV> pdvCheckIn = parametros.getPdvCheckIn();

        if( pdvCheckIn.isEmpty() )//No tiene PDV con CheckIn
        {
            new Utilidades().mostrarMensajeBotonSoloOK(NuevoPedidoActivity.this, "Pedidos PDV", getString(R.string.txt_msg_no_pdv_checkin), getString(R.string.txt_aceptar),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    });
        }else
        {
            try {
                List<Categoria> arrayPdvs = new ArrayList<>();
                arrayPdvs.add(new Categoria("0", "--Seleccionar PDV--"));
                for (int i = 0; i < arrayPDV.length(); i++)
                {
                    JSONObject tmp = arrayPDV.getJSONObject(i);
                    String id = tmp.getString("id");
                    String nombre = tmp.getString("nombre");
                    String nombre_contacto = tmp.getString("nombre_contacto");
                    String apellido_contacto = tmp.getString("apellido_contacto");
                    String direccion = tmp.getString("direccion");
                    String telefono = tmp.getString("telefono");
                    String celular = tmp.getString("celular");
                    String email = tmp.getString("email");
                    String lng = tmp.getString("lng");
                    String lat = tmp.getString("lat");
                    String zona = tmp.getString("zona");
                    PDV pdv = new PDV(id, nombre, nombre_contacto, apellido_contacto, direccion, telefono, celular, email, lat, lng, zona, 0,0,0);

                    //Validar CheckIn para el PDV, solo así se listará
                    if( pdvCheckIn.contains( pdv) )//PDV con CheckIn agregarlo al listado para su visualización
                    {
                        arrayPdvs.add(new Categoria(id, nombre));
                    }
                }

                //Construir spinner PDV
                ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(NuevoPedidoActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayPdvs);
                spPDV.setAdapter(catAdapter);

                //Consultar productos
                postParam = new HashMap<>();
                postParam.put("empresa", getNombreEmpresa());

                ConsultarProductosTask cpt = new ConsultarProductosTask();
                cpt.execute(URL_LISTA_PRODUCTOS);
            }catch (JSONException e )
            {
                Log.e("JSONException", "NuevoPedidoActivity.cargarListaPDV.JSONException: "+e.toString());
            }
        }
    }

    /**
     * Clase encargada de realizar el proceso de consultar la lista de los PDVs
     * */
    private class ConsultarListaPdvTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(NuevoPedidoActivity.this, null, getString( R.string.txt_consulta_lista_pdv), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam );
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch ( estado )
                    {
                        case "OK":
                            JSONArray pdvs = result.getJSONArray("pdv");
                            cargarListaPDV( pdvs );
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Lista PDV", getString(R.string.txt_msg_lista_pdv_pedido_vacia), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("PA-JSONException", e.toString());
            }catch ( Exception e )
            {
                progreso.cancel();
                Log.e("PA-Exception", e.toString());
            }
        }
    }//ConsultarListaPdvTask

    /**
     * Clase encargada de realizar el proceso de consulta de los productos por empresa
     * */
    public class ConsultarProductosTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;       

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( NuevoPedidoActivity.this, null, getString( R.string.txt_consulta_productos), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam );
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            JSONObject content_productos = result.getJSONObject("productos");
                            if( content_productos.getString("estado").equals("OK") )
                            {
                                //Construir productos
                                JSONArray productos = content_productos.getJSONArray("productos");
                                ArrayList<Producto> list_productos = new ArrayList<>();
                                for( int i=0; i < productos.length(); i++ )
                                {
                                    JSONObject tmp = productos.getJSONObject(i);
                                    Producto producto = new Producto(
                                            tmp.getString("id"),
                                            tmp.getString("nombre"),
                                            tmp.getString("imagen"),
                                            tmp.getString("id_empresa"),
                                            tmp.getString("empresa"),
                                            tmp.getString("id_categoria"),
                                            tmp.getString("categoria"),
                                            0
                                    );
                                    list_productos.add( producto );
                                }
                                //Constuir categorias
                                JSONObject content_categorias = result.getJSONObject("categorias");
                                List<Categoria> list_categorias = new ArrayList<>();
                                if( content_categorias.getString("estado").equals("OK") )
                                {
                                    list_categorias.add( new Categoria("0", "Todos") );
                                    JSONArray categorias = content_categorias.getJSONArray("categorias");
                                    for( int i=0; i < categorias.length(); i++ )
                                    {
                                        JSONObject tmp = categorias.getJSONObject(i);
                                        Categoria categoria = new Categoria( tmp.getString("id"), tmp.getString("nombre") );
                                        list_categorias.add( categoria );
                                    }
                                }else //EMPTY
                                {
                                    list_categorias.add( new Categoria("0", "--Sin categorías--") );
                                }
                                cargarListas(list_productos, list_categorias);
                            }else //EMPTY
                            {
                                new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Productos control", getString(R.string.txt_msg_productos_vacios), true);
                            }
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarProductosTask

    /**
     * Clase encargada de realizar el registro del nuevo pedido
     * */
    public class RegistrarPedidoPDVTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(NuevoPedidoActivity.this, null, getString( R.string.txt_registro_pedido_pdv), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            new Utilidades().mostrarMensajeBotonSoloOK(NuevoPedidoActivity.this, "Control PDV", getString(R.string.txt_registro_pedido_pdv_ok), getString(R.string.txt_aceptar),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                        }
                                    });
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Conexión",fch+"\n"+  getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+ getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPedidoActivity.this, "Error", fch+"\n"+ getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarPedidoPDVTask
}
