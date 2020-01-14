package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.content.Context;
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
import co.com.puli.trade.fdv.adaptadores.MiPedidoAdapter;
import co.com.puli.trade.fdv.clases.Categoria;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Pedido;
import co.com.puli.trade.fdv.clases.Utilidades;

public class MisPedidosActivity extends AppCompatActivity {
    private CustomFonts fuentes;
    private HashMap<String, String> postParam;
    private String URL_LISTA_PDV, URL_LISTA_PEDIDOS,id_fdv;
    private Spinner spPDV;
    private ListView listaPedidos;
    private MiPedidoAdapter adaptadorPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pedidos);

        fuentes = new CustomFonts(getAssets());

        URL_LISTA_PDV = getString(R.string.url_server_backend) + "consultar_pdvs_fdv.jsp";
        URL_LISTA_PEDIDOS = getString(R.string.url_server_backend) + "consultar_pedido.jsp";

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
        listaPedidos = findViewById( R.id.lvListaPedidos );

        Button btConsultar = findViewById( R.id.btConsultar );
        btConsultar.setTypeface( fuentes.getRobotoThinFont() );
        btConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Categoria pdv = (Categoria) spPDV.getSelectedItem();
                TextView tvPDV = (TextView) spPDV.getSelectedView();
                tvPDV.setTextColor( ContextCompat.getColor( MisPedidosActivity.this, R.color.black ) );
                if( pdv.getId() == "0" )
                {
                    tvPDV.setTextColor( Color.RED );
                    tvPDV.setError( getString(R.string.txt_campo_requerido) );
                }else{
                    //Verificar disponibilidad de Red
                    if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
                    {
                        //Consultar pedidos
                        postParam = new HashMap<>();
                        postParam.put("id_pdv", pdv.getId() );
                        postParam.put("id_fdv", id_fdv );

                        ConsultarPedidosTask cpt = new ConsultarPedidosTask();
                        cpt.execute( URL_LISTA_PEDIDOS );

                    }else{
                        new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
                    }
                }
            }
        });

        //Verificar disponibilidad de Red
        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            //Consultar listado de PDV
            postParam = new HashMap<>();
            postParam.put("id_fdv", id_fdv);
            ConsultarListaPdvTask cpdv = new ConsultarListaPdvTask();
            cpdv.execute(URL_LISTA_PDV);

        }else{
            new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
        }
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

        }catch (Exception e) {
            Log.e("Exception","MisPedidosActivity.onResume.Exception:"+e.toString() );
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
     * Método encargado de cargar el listado de PDV
     * */
    public void cargarListaPDV( JSONArray arrayPDV )
    {
        try {
            List<Categoria> arrayPdvs = new ArrayList<>();
            arrayPdvs.add(new Categoria("0", "--Seleccionar PDV--"));
            for (int i = 0; i < arrayPDV.length(); i++)
            {
                JSONObject tmp = arrayPDV.getJSONObject(i);
                String id = tmp.getString("id");
                String nombre = tmp.getString("nombre");

                arrayPdvs.add(new Categoria(id, nombre));
            }

            //Construir spinner PDV
            ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(MisPedidosActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayPdvs);
            spPDV.setAdapter(catAdapter);

        }catch (JSONException e )
        {
            Log.e("JSONException", "MisPedidosActivity.cargarListaPDV.JSONException: "+e.toString());
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
            progreso = ProgressDialog.show(MisPedidosActivity.this, null, getString( R.string.txt_consulta_lista_pdv), true);
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
                            new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Lista PDV", getString(R.string.txt_msg_lista_pdv_vacio), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
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
    public class ConsultarPedidosTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( MisPedidosActivity.this, null, getString( R.string.txt_consulta_pedidos), true);
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
                            JSONArray pedidos = result.getJSONArray("pedidos");
                            ArrayList<Pedido> list_pedidos = new ArrayList<>();
                            for( int i=0; i < pedidos.length(); i++ )
                            {
                                JSONObject tmp = pedidos.getJSONObject(i);
                                Pedido producto = new Pedido( tmp.getString("fecha"), tmp.getInt("cantidad") );
                                list_pedidos.add( producto );
                            }
                            adaptadorPedido = new MiPedidoAdapter( MisPedidosActivity.this, list_pedidos, id_fdv, postParam.get("id_pdv") );
                            listaPedidos.setAdapter( adaptadorPedido );
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Productos control", getString(R.string.txt_msg_pedidos_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(MisPedidosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarPedidosTask
}
