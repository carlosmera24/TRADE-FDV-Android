package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.adaptadores.MensajeAdapter;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Mensaje;
import co.com.puli.trade.fdv.clases.Utilidades;

public class NotificacionesActivity extends AppCompatActivity {
    private String URL_NOTIFICACIONES, id_usuario;
    private HashMap<String,String> postParam;
    private ListView lvNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        URL_NOTIFICACIONES= getString( R.string.url_server_backend ) + "consultar_notificaciones.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor(NotificacionesActivity.this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvTitulo = findViewById( R.id.tvTitulo );
        tvTitulo.setTypeface( fuentes.getRobotoThinFont() );

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");

        lvNotificaciones =  findViewById( R.id.lvNotificaciones );
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator(iconBack);

            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );


            //Consultar Notificaciones
            postParam = new HashMap<>();
            postParam.put("id_usuario", id_usuario );
            ConsultarNotificacionesTask cmt = new ConsultarNotificacionesTask();
            cmt.execute( URL_NOTIFICACIONES );
        }catch(Exception e){}
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
        }catch(Exception e){}
    }

    /**
     * Clase encargada de realizar el proceso de consulta de los mensajes de la BD
     * */
    public class ConsultarNotificacionesTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(NotificacionesActivity.this, null, getString( R.string.txt_consulta_notificaciones), true);
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
                            JSONArray notificaciones = result.getJSONArray("notificaciones");
                            ArrayList<Mensaje> arrayNotificaciones = new ArrayList<>();
                            for( int i=0; i < notificaciones.length(); i++ )
                            {
                                JSONObject tmp = notificaciones.getJSONObject(i);
                                String id = tmp.getString("id");
                                String mensaje = tmp.getString("mensaje");
                                String fecha = tmp.getString("fecha");
                                Mensaje men = new Mensaje(id,fecha,mensaje);
                                arrayNotificaciones.add( men );
                            }

                            MensajeAdapter adaptador = new MensajeAdapter( NotificacionesActivity.this, arrayNotificaciones );
                            lvNotificaciones.setAdapter( adaptador );
                            break;

                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(NotificacionesActivity.this, "Notificaciones", getString(R.string.txt_msg_notificaciones_vacias), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(NotificacionesActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("CNTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(NotificacionesActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(NotificacionesActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("MA-JSONException", "NotificacionesActivity.ConsultarNotificacionesTask.JSONException:"+e.toString());
            }
        }
    }//ConsultarNotificacionesTask
}
