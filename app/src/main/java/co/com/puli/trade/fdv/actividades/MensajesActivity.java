package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Mensaje;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.adaptadores.MensajeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MensajesActivity extends AppCompatActivity
{
    private String URL_MENSAJES, id_usuario;
    private HashMap<String,String> postParam;
    private ListView lvMensaje;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);

        URL_MENSAJES= getString( R.string.url_server_backend ) + "consultar_mensajes.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary ) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvTitulo = (TextView) findViewById( R.id.tvTitulo );
        tvTitulo.setTypeface( fuentes.getRobotoThinFont() );

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");

        lvMensaje = (ListView) findViewById( R.id.lvMensajes );
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator( iconBack );

            //Consultar mensajes
            postParam = new HashMap<>();
            postParam.put("id_usuario", id_usuario );
            ConsultarMensajesTask cmt = new ConsultarMensajesTask();
            cmt.execute( URL_MENSAJES );

            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );
        }catch (Exception e){}
    }

    @Override
    protected void onStop() {
        super.onStop();
        limpiarMemoria();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarMemoria();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     * */
    public void limpiarMemoria()
    {
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            System.gc();
        }catch (Exception e){}

    }

    /**
     * Clase encargada de realizar el proceso de consulta de los mensajes de la BD
     * */
    public class ConsultarMensajesTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(MensajesActivity.this, null, getString( R.string.txt_consulta_mensajes), true);
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
                    switch( estado )
                    {
                        case "OK":
                                JSONArray mensajes = result.getJSONArray("mensajes");
                                ArrayList<Mensaje> arrayMensajes = new ArrayList<>();
                                for( int i=0; i < mensajes.length(); i++ )
                                {
                                    JSONObject tmp = mensajes.getJSONObject(i);
                                    String id = tmp.getString("id");
                                    String mensaje = tmp.getString("mensaje");
                                    String fecha = tmp.getString("fecha");
                                    Mensaje men = new Mensaje(id,fecha,mensaje);
                                    arrayMensajes.add( men );
                                }

                                MensajeAdapter adaptador = new MensajeAdapter( MensajesActivity.this, arrayMensajes );
                                lvMensaje.setAdapter(adaptador);
                            break;
                        case "EMPTY":
                                new Utilidades().mostrarSimpleMensaje(MensajesActivity.this, "Mensajes", getString(R.string.txt_msg_mensajes_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                                new Utilidades().mostrarSimpleMensaje(MensajesActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                                Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;

                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(MensajesActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(MensajesActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("MA-JSONException", e.toString());
            }
        }
    }//ConsultarMensajesTask
}
