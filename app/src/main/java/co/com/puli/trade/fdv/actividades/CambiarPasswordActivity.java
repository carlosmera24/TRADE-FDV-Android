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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class CambiarPasswordActivity extends AppCompatActivity
{
    private LinearLayout content_layout;
    private String URL_ACTUALIZAR_PASSWORD, id_usuario;
    private HashMap<String,String> postParam;
    private EditText etNewPass, etRepeatPass;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        URL_ACTUALIZAR_PASSWORD = getString( R.string.url_server_backend ) + "actualizar_password.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundDrawable( null );
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Obtener datos extras enviados en el Intent
        bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");

        content_layout = (LinearLayout) findViewById( R.id.contentLayout );

        etNewPass = (EditText) findViewById( R.id.etNewPass );
        etRepeatPass = (EditText) findViewById( R.id.etRepeatPass );

        etRepeatPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if( actionId == EditorInfo.IME_ACTION_DONE )
            {
                new Utilidades().ocultarTeclado( CambiarPasswordActivity.this, etRepeatPass );//Ocultar teclado
                //Procesar actualización password
                actualizarPassword();
                return true;
            }
            return false;
        }
    });

        Button btGuardar = (Button) findViewById( R.id.btGuardar );
        btGuardar.setTypeface( fuentes.getBoldFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Procesar actualización password
                actualizarPassword();
            }
        });

        Button btCancelar = (Button) findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getBoldFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
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

            content_layout.setBackgroundDrawable(new BitmapDrawable(img));

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
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            content_layout.setBackgroundResource(0);
            System.gc();
        }catch(Exception e)
        {
            Log.e("Exception", "CambiarPasswordActivity.LimpiarMemoria.Exception:"+ e.toString() );
        }
    }

    /**
     * Método encargado de procesar el registro de la actualización de la contraseña.
     * Valida los campos requeridos
     * */
    public void actualizarPassword()
    {
        if( TextUtils.isEmpty( etNewPass.getText() ) )
        {
            etNewPass.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etRepeatPass.getText() ) )
        {
            etRepeatPass.setError(getString(R.string.txt_campo_requerido));
        }else if( !etNewPass.getText().toString().equals( etRepeatPass.getText().toString() )  )
        {
            etRepeatPass.setError(getString(R.string.txt_pass_no_match ));
        }else
        {
            //Verificar disponiblidad de Internet
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                if( id_usuario != null ) {
                    postParam = new HashMap<>();
                    postParam.put("id_user", id_usuario);
                    postParam.put("password", etNewPass.getText().toString());
                    ActualizarPasswordTask apt = new ActualizarPasswordTask();
                    apt.execute( URL_ACTUALIZAR_PASSWORD );
                }else{
                    new Utilidades().mostrarSimpleMensaje(this,"Actualización contraseña",getString( R.string.txt_error_id_usuario),true);
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
            }
        }
    }

    /**
     * Clase encargada de procesar el registro de la nueva contraseña
     * */
    class ActualizarPasswordTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(CambiarPasswordActivity.this, null, getString( R.string.txt_actualizar_password ), true);
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
                            build = new AlertDialog.Builder(CambiarPasswordActivity.this);
                        }else{
                            build = new AlertDialog.Builder(CambiarPasswordActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                        }
                        build.setTitle("Registro usuario");
                        build.setMessage( getString( R.string.txt_msg_password_actualizado) );
                        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        });
                        build.show();
                    } else if (estado.equals("ERROR")) {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(CambiarPasswordActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        progreso.cancel();
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(CambiarPasswordActivity.this, "Error actualizar contraseña", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(CambiarPasswordActivity.this, "Error actualizar contraseña", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error","Error:"+ result.getInt("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException","LoginActivity.onPostExecute.ValidarInicioSessionTask:"+e.toString());
            }
        }
    }//ActualizarPasswordTask
}
