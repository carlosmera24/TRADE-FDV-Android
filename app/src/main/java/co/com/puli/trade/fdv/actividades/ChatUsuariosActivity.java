package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.UsuarioChat;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.adaptadores.ChatUsuarioAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatUsuariosActivity extends AppCompatActivity
{
    private String id_usuario;
    private HashMap<String,String> postParam;
    private String URL_USUARIOS_CHAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_usuarios);

        URL_USUARIOS_CHAT = getString( R.string.url_server_backend ) + "consultar_usuarios_chat.jsp";

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary ) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");

        //Consultar usuarios
        postParam = new HashMap<>();
        postParam.put("id_usuario", id_usuario);
        ConsultarUsuariosTask cut = new ConsultarUsuariosTask();
        cut.execute( URL_USUARIOS_CHAT );
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator(iconBack);
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
     * Método encargado de construir la vista con los usuarios
     * usuarios ArrayList con el listado de usuarios encontrados en formato UsuarioChat
     * */
    public void construirUsuarios( ArrayList<UsuarioChat> usuarios)
    {
        //Pestañas (ViewPager + TabLayout)
        ViewPager vpPage = findViewById( R.id.vpPager );
        ChatUsuarioAdapter vpAdapter = new ChatUsuarioAdapter( getSupportFragmentManager(), this, usuarios, id_usuario);
        vpPage.setAdapter(vpAdapter);
        TabLayout tlTabs = findViewById( R.id.tlTabs );
        tlTabs.setupWithViewPager( vpPage );
    }

    /**
     * Clase encargada de realizar el proceso de consulta de los usuarios disponibles para chat de la BD
     * */
    public class ConsultarUsuariosTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(ChatUsuariosActivity.this, null, getString( R.string.txt_consultar_usuarios_chat), true);
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
                                JSONArray usuarios = result.getJSONArray("usuarios");
                                ArrayList<UsuarioChat> arrayUsuarios = new ArrayList<>();
                                for( int i=0; i < usuarios.length(); i++ )
                                {
                                    JSONObject tmp = usuarios.getJSONObject(i);
                                    int id_user = tmp.getInt("id_usuario");
                                    int id_perfil = tmp.getInt("id_perfil");
                                    String nombre = tmp.getString("nombre");
                                    if( id_perfil == 2 )//Usuario padre
                                    {
                                        if( !tmp.getString("nombre_alumno").equals("null") )
                                        {
                                            nombre += "("+ tmp.getString("nombre_alumno") +")";
                                        }
                                    }
                                    UsuarioChat usuario = new UsuarioChat( id_user, id_perfil, nombre);
                                    arrayUsuarios.add( usuario );
                                }
                                construirUsuarios(arrayUsuarios);
                            break;
                        case "EMPTY":
                                new Utilidades().mostrarSimpleMensaje(ChatUsuariosActivity.this, "Mensajes", getString(R.string.txt_msg_usuarios_chat_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                                new Utilidades().mostrarSimpleMensaje(ChatUsuariosActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                                Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;

                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ChatUsuariosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ChatUsuariosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException", "ChatActivity.ConsultarMensajesTask.onPostExecute:"+e.toString());
            }
        }
    }//ConsultarUsuariosTask
}
