package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.com.puli.trade.fdv.adaptadores.ConcursoChatAdapter;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.UsuarioChat;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.clases.ConsultaExterna;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import co.com.puli.trade.fdv.R;

public class ListadoConcursosActivity extends AppCompatActivity {

    private String id_usuario;
    private HashMap<String,String> postParam;
    private String URL_USUARIOS_CHAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_concursos);

        URL_USUARIOS_CHAT = getString( R.string.url_server_backend ) + "consultar_concursos_usuario.jsp";

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor(ListadoConcursosActivity.this, R.color.colorPrimary) );
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
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator(iconBack);
        }catch(NullPointerException e){}
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
        }catch(Exception e){}
        System.gc();
    }

    /**
     * Método encargado de construir la vista con los usuarios
     * usuarios ArrayList con el listado de usuarios encontrados en formato UsuarioChat
     * */
    public void construirUsuarios( ArrayList<UsuarioChat> usuarios)
    {
        //Pestañas (ViewPager + TabLayout)
        ListView lvUsuarios = (ListView) findViewById( R.id.lvUsuarios );
        ConcursoChatAdapter ucAdapter = new ConcursoChatAdapter( this, usuarios );
        lvUsuarios.setAdapter( ucAdapter );
        lvUsuarios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UsuarioChat user = (UsuarioChat) parent.getItemAtPosition( position );
                Intent intent = new Intent( ListadoConcursosActivity.this, ConcursoActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                intent.putExtra( "id_destino", ""+id );
                intent.putExtra( "usuario_chat", user.getNombre() );
                startActivity(intent );
            }
        });
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
            progreso = ProgressDialog.show(ListadoConcursosActivity.this, null, getString( R.string.txt_consultar_concursos_chat), true);
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
                                String perfil = tmp.getString("perfil");
                                UsuarioChat usuario = new UsuarioChat( id_user, id_perfil, nombre );
                                arrayUsuarios.add( usuario );
                            }
                            construirUsuarios(arrayUsuarios);
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(ListadoConcursosActivity.this, "Mensajes", getString(R.string.txt_msg_concursos_chat_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(ListadoConcursosActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;

                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(ListadoConcursosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(ListadoConcursosActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException", "ListadoConcursosActivity.ConsultarUsuariosTask.onPostExecute:"+e.toString());
            }
        }
    }//ConsultarUsuariosTask
}
