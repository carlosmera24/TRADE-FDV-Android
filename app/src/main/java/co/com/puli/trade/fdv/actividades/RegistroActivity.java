package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Utilidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RegistroActivity extends AppCompatActivity
{
    private LinearLayout content_layout;
    private String URL_REGISTRAR_USUARIO;
    private HashMap<String,String> postParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        URL_REGISTRAR_USUARIO = getString( R.string.url_server_backend ) + "registrar_conductor.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        content_layout = findViewById( R.id.contentLayout );

        final EditText etMarca = findViewById( R.id.etMarca );
        etMarca.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    new Utilidades().ocultarTeclado( RegistroActivity.this, etMarca );//Ocultar teclado
                    //Procesar registro
                    guardarRegistro();
                    return true;
                }
                return false;
            }
        });

        Button btGuardar = findViewById( R.id.btGuardar );
        btGuardar.setTypeface( fuentes.getBoldFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarRegistro();
            }
        });

        Button btCancelar = findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getBoldFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator(iconBack);

            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            Bitmap img = imgbm.decodificarImagen(getResources(), R.drawable.background_route_gray, displaymetrics.widthPixels, 0);

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            {
                content_layout.setBackground( new BitmapDrawable( getResources(), img ));
            }else{
                content_layout.setBackgroundDrawable(new BitmapDrawable( getResources(), img ));
            }
        }catch (Exception e){

        }
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
        getSupportActionBar().setHomeAsUpIndicator(null);
        content_layout.setBackgroundResource(0);
        System.gc();
    }

    /**
     * Método encargado de procesar el registro
     * */
    public void guardarRegistro()
    {
        EditText etDoc = findViewById( R.id.etDoc );
        EditText etNombres = findViewById( R.id.etNombres );
        EditText etApellidos = findViewById( R.id.etApellidos );
        EditText etCel = findViewById( R.id.etCel );
        EditText etEmail = findViewById( R.id.etEmail );
        EditText etPlaca = findViewById( R.id.etPlaca );
        EditText etMarca = findViewById( R.id.etMarca );
        EditText etBarrio = findViewById( R.id.etBarrio );

        if( TextUtils.isEmpty( etDoc.getText() ) )
        {
            etDoc.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etNombres.getText() ) )
        {
            etNombres.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etApellidos.getText() ) )
        {
            etApellidos.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etCel.getText() ) )
        {
            etCel.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etEmail.getText() ) )
        {
            etEmail.setError(getString(R.string.txt_campo_requerido));
        }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher( etEmail.getText() ).matches() )
        {
            etEmail.setError( getString(R.string.txt_email_invalido) );
        }else if( TextUtils.isEmpty( etPlaca.getText() ) )
        {
            etPlaca.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etMarca.getText() ) )
        {
            etMarca.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etBarrio.getText() ) )
        {
            etBarrio.setError(getString(R.string.txt_campo_requerido));
        }else
        {
            //Verificar disponiblidad de Internet
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                //Preparar parámetros para envío de registro
                postParam = new HashMap<>();
                postParam.put("doc", etDoc.getText().toString() );
                postParam.put("nombres", etNombres.getText().toString() );
                postParam.put("apellidos", etApellidos.getText().toString() );
                postParam.put("cel", etCel.getText().toString() );
                postParam.put("email", etEmail.getText().toString() );
                postParam.put("placa", etPlaca.getText().toString() );
                postParam.put("marca", etMarca.getText().toString() );
                postParam.put("barrio", etBarrio.getText().toString() );
                RegistrarUsuarioTask rut = new RegistrarUsuarioTask();
                rut.execute( URL_REGISTRAR_USUARIO );
            }else{
                new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
            }
        }
    }

    /**
     * Clase encargada de procesar el registro del usuario*/
    class RegistrarUsuarioTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(RegistroActivity.this, null, getString( R.string.txt_registrar_usuario ), true);
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
                    if (estado.equals("OK"))
                    {
                        progreso.cancel();
                        //Visualizar Dialogo informativo
                        AlertDialog.Builder build;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            build = new AlertDialog.Builder(RegistroActivity.this);
                        }else{
                            build = new AlertDialog.Builder(RegistroActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                        }
                        build.setTitle("Registro usuario");
                        build.setMessage( getString( R.string.txt_msg_usuario_registrado) );
                        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        });
                        build.show();
                    } else if (estado.equals("ERROR")) {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(RegistroActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        progreso.cancel();
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(RegistroActivity.this, "Error registro usuario", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(RegistroActivity.this, "Error registro usuario", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error","Error:"+ result.getInt("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException","LoginActivity.onPostExecute.ValidarInicioSessionTask:"+e.toString());
            }
        }
    }//RegistrarUsuarioTask
}
