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
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import co.com.puli.trade.fdv.clases.Categoria;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Utilidades;


public class PuntoOportunidadActivity extends AppCompatActivity {
    private CustomFonts fuentes;
    private String id_fdv, URL_LISTA_PDV, URL_LIST_CATEGORIAS, URL_CONSULTAR_INFORME;
    private Spinner spPDV, spCategorias;
    private WebView wvInforme;
    private HashMap<String,String> postParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punto_oportunidad);

        fuentes = new CustomFonts(getAssets());

        URL_LISTA_PDV = getString(R.string.url_server_backend) + "consultar_pdvs_fdv.jsp";
        URL_LIST_CATEGORIAS = getString(R.string.url_server_backend) + "consultar_categorias.jsp";
        URL_CONSULTAR_INFORME = getString(R.string.url_server_backend) + "consultar_informe_oportunidad_pdv.jsp?id_pdv=";

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
        wvInforme = findViewById( R.id.wvInforme );
        wvInforme.clearCache( true );
        wvInforme.clearHistory();
        wvInforme.getSettings().setJavaScriptEnabled( true );
        wvInforme.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );

        Button btConsultar = findViewById( R.id.btConsultar );
        btConsultar.setTypeface( fuentes.getRobotoThinFont() );
        btConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validar selectores listas
                Categoria pdv = (Categoria) spPDV.getSelectedItem();
                TextView tvPDV = (TextView) spPDV.getSelectedView();
                tvPDV.setTextColor( ContextCompat.getColor( PuntoOportunidadActivity.this, R.color.black ) );
                Categoria cat = (Categoria) spCategorias.getSelectedItem();

                if( pdv.getId() == "0" )
                {
                    tvPDV.setTextColor( Color.RED );
                    tvPDV.setError( getString(R.string.txt_campo_requerido) );
                }else
                {
                    //Verificar disponibilidad de Red
                    if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
                    {
                        String URL_LOAD = URL_CONSULTAR_INFORME + pdv.getId();
                        if( cat.getId() != "0" )
                        {
                            URL_LOAD += "&id_categoria=" + cat.getId();
                        }
                        wvInforme.loadUrl( URL_LOAD );
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
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
            PuntoOportunidadActivity.ConsultarListaPdvTask cpdv = new PuntoOportunidadActivity.ConsultarListaPdvTask();
            cpdv.execute(URL_LISTA_PDV);

        }else{
            new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
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
            Log.e("Exception","PuntoOportunidadActivity.onResume.Exception:"+e.toString() );
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
     * M??todo encargado de liberar memoria para optimizar el uso de momoria RAM
     */
    public void limpiarMemoria() {
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            System.gc();
        } catch (Exception e) {
        }
    }

    /**
     * M??todo encargado de retornar el nombre de la empresa a partir de los par??metros globales
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
     * M??todo encargado de cargar el listado de PDV
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
            ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(PuntoOportunidadActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayPdvs);
            spPDV.setAdapter(catAdapter);

            //Consultar listado de categor??as
            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                //Consultar listado de categor??as
                postParam = new HashMap<>();
                postParam.put("empresa", getNombreEmpresa() );
                PuntoOportunidadActivity.ConsultarListaCategoriasTask clct = new PuntoOportunidadActivity.ConsultarListaCategoriasTask();
                clct.execute(URL_LIST_CATEGORIAS);

            }else{
                new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            }


        }catch (JSONException e )
        {
            Log.e("JSONException", "PuntoOportunidadActivity.cargarListaPDV.JSONException: "+e.toString());
        }
    }

    /**
     * M??todo encargado de cargar el listado de PDV
     * */
    public void cargarListaCategorias( JSONArray arrayCategorias )
    {
        try {
            List<Categoria> arrayListCategorias = new ArrayList<>();
            arrayListCategorias.add(new Categoria("0", "--Todas las categor??as--"));
            for (int i = 0; i < arrayCategorias.length(); i++)
            {
                JSONObject tmp = arrayCategorias.getJSONObject(i);
                String id = tmp.getString("id");
                String nombre = tmp.getString("nombre");

                arrayListCategorias.add(new Categoria(id, nombre));
            }

            //Construir spinner Categiruas
            ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(PuntoOportunidadActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayListCategorias);
            spCategorias.setAdapter(catAdapter);

        }catch (JSONException e )
        {
            Log.e("JSONException", "PuntoOportunidadActivity.cargarListaPDV.JSONException: "+e.toString());
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
            progreso = ProgressDialog.show(PuntoOportunidadActivity.this, null, getString( R.string.txt_consulta_lista_pdv), true);
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
                            new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Lista PDV", getString(R.string.txt_msg_lista_pdv_vacio), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Conexi??n", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
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
     * Clase encargada de realizar el proceso de consultar la lista de las categor??as
     * */
    private class ConsultarListaCategoriasTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PuntoOportunidadActivity.this, null, getString( R.string.txt_consulta_lista_categorias), true);
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
                            JSONArray categorias = result.getJSONArray("categorias");
                            cargarListaCategorias( categorias );
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Lista Categorias", getString(R.string.txt_msg_lista_categorias_vacio), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Conexi??n", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PuntoOportunidadActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
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



}
