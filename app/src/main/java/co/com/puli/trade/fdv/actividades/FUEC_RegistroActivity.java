package co.com.puli.trade.fdv.actividades;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import static co.com.puli.trade.fdv.R.id.etObjeto;

public class FUEC_RegistroActivity extends AppCompatActivity
{
    String URL_REGISTRAR_SOLICITUD, id_solicitud, id_vehiculo, id_usuario;
    private HashMap<String,String> postParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuec_registro);

        URL_REGISTRAR_SOLICITUD= getString( R.string.url_server_backend ) + "registrar_solicitud_fuec.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary ) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_vehiculo = bundle.getString("id_vehiculo");
        id_solicitud = bundle.getString("id_solicitud");
        id_usuario = bundle.getString("id_usuario");

        EditText etPlaca = (EditText) findViewById( R.id.etPlaca );
        etPlaca.setText( id_vehiculo );



        final EditText etFchIni = (EditText) findViewById( R.id.etFchIniServicio);
        final EditText etFchFin = (EditText) findViewById( R.id.etFchFinServicio);
        etFchIni.setInputType( InputType.TYPE_NULL );
        etFchIni.setTextIsSelectable( true );
        etFchIni.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ) {
                    new Utilidades().ocultarTeclado( FUEC_RegistroActivity.this, etFchIni);
                    Utilidades.mostrarDatePickerEditText(FUEC_RegistroActivity.this, etFchIni, getString(R.string.txt_fecha_inicio_servicio));
                }

            }
        });
        etFchFin.setInputType( InputType.TYPE_NULL );
        etFchFin.setTextIsSelectable( true );
        etFchFin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus )
                {
                    new Utilidades().ocultarTeclado( FUEC_RegistroActivity.this, etFchFin);
                    Utilidades.mostrarDatePickerEditText( FUEC_RegistroActivity.this, etFchFin, getString(R.string.txt_fecha_fin_servicio) +" Fin");
                }
            }
        });

        Button btGuardar = (Button) findViewById( R.id.btGuardar );
        Button btCancelar = (Button) findViewById( R.id.btCancelar );
        btGuardar.setTypeface( fuentes.getBoldFont() );
        btCancelar.setTypeface( fuentes.getBoldFont() );

        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarSolicitud();
            }
        });

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
     * Método encargado de validar e iniciar el registro de la solicitud
     * */
    public void registrarSolicitud()
    {
        EditText etEmpresa = (EditText) findViewById( R.id.etEmpresa );
        Spinner spObjeto = (Spinner) findViewById( etObjeto );
        EditText etOrigen = (EditText) findViewById( R.id.etOrigenRecorrido );
        EditText etDestino = (EditText) findViewById( R.id.etDestinoRecorrido );
        EditText etPlaca = (EditText) findViewById( R.id.etPlaca );
        EditText etNombre = (EditText) findViewById( R.id.etNombreRes );
        EditText etDir = (EditText) findViewById( R.id.etDir );
        EditText etTel = (EditText) findViewById( R.id.etTel);
        EditText etNit = (EditText) findViewById( R.id.etNit);
        EditText etCc = (EditText) findViewById( R.id.etCcResponsable);
        EditText etFchIni = (EditText) findViewById( R.id.etFchIniServicio);
        EditText etFchFin = (EditText) findViewById( R.id.etFchFinServicio);
        EditText etCel = (EditText) findViewById( R.id.etCel);
        EditText etEmail = (EditText) findViewById( R.id.etEmail);
        EditText etValor = (EditText) findViewById( R.id.etValor);

        if( TextUtils.isEmpty( etEmpresa.getText() ) )
        {
            etEmpresa.setError(getString(R.string.txt_campo_requerido));
            etEmpresa.requestFocus();
        }else if( spObjeto.getSelectedItemPosition() == 0 )
        {
            ((TextView)spObjeto.getSelectedView()).setError(getString(R.string.txt_campo_requerido));
            spObjeto.requestFocus();
        }else if( TextUtils.isEmpty( etOrigen.getText() ) )
        {
            etOrigen.setError(getString(R.string.txt_campo_requerido));
            etOrigen.requestFocus();
        }else if( TextUtils.isEmpty( etDestino.getText() ) )
        {
            etDestino.setError(getString(R.string.txt_campo_requerido));
            etDestino.requestFocus();
        }else if( TextUtils.isEmpty( etPlaca.getText() ) )
        {
            etPlaca.setError(getString(R.string.txt_campo_requerido));
            etPlaca.requestFocus();
        }else if( TextUtils.isEmpty( etNombre.getText() ) )
        {
            etNombre.setError(getString(R.string.txt_campo_requerido));
            etNombre.requestFocus();
        }else if( TextUtils.isEmpty( etDir.getText() ) )
        {
            etDir.setError(getString(R.string.txt_campo_requerido));
            etDir.requestFocus();
        }else if( TextUtils.isEmpty( etTel.getText() ) )
        {
            etTel.setError(getString(R.string.txt_campo_requerido));
            etTel.requestFocus();
        }else if( TextUtils.isEmpty( etNit.getText() ) )
        {
            etNit.setError(getString(R.string.txt_campo_requerido));
            etNit.requestFocus();
        }else if( TextUtils.isEmpty( etCc.getText() ) )
        {
            etCc.setError(getString(R.string.txt_campo_requerido));
            etCc.requestFocus();
        }else if( TextUtils.isEmpty( etFchIni.getText() ) )
        {
            etFchIni.setError(getString(R.string.txt_campo_requerido));
            etFchIni.requestFocus();
        }else if( TextUtils.isEmpty( etFchFin.getText() ) )
        {
            etFchFin.setError(getString(R.string.txt_campo_requerido));
            etFchFin.requestFocus();
        }else if( TextUtils.isEmpty( etCel.getText() ) )
        {
            etCel.setError(getString(R.string.txt_campo_requerido));
            etCel.requestFocus();
        }else if( TextUtils.isEmpty( etEmail.getText() ) )
        {
            etEmail.setError(getString(R.string.txt_campo_requerido));
            etEmail.requestFocus();
        }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher( etEmail.getText() ).matches() )
        {
            etEmail.setError( getString(R.string.txt_email_invalido) );
            etEmail.requestFocus();
        }else if( TextUtils.isEmpty( etValor.getText() ) )
        {
            etValor.setError(getString(R.string.txt_campo_requerido));
            etValor.requestFocus();
        }else{
            //Preparar parámetros para el registro
            postParam = new HashMap<>();
            postParam.put("empresa", etEmpresa.getText().toString() );
            postParam.put("objeto", spObjeto.getSelectedItem().toString() );
            postParam.put("origen", etOrigen.getText().toString() );
            postParam.put("destino", etDestino.getText().toString() );
            postParam.put("responsable", etNombre.getText().toString() );
            postParam.put("dir", etDir.getText().toString() );
            postParam.put("tel", etTel.getText().toString() );
            postParam.put("id_usuario", id_usuario );
            postParam.put("placa", etPlaca.getText().toString() );
            postParam.put("nit", etNit.getText().toString() );
            postParam.put("cc", etCc.getText().toString() );
            postParam.put("fecha_ini", etFchIni.getText().toString() );
            postParam.put("fecha_fin", etFchFin.getText().toString() );
            postParam.put("celular", etCel.getText().toString() );
            postParam.put("email", etEmail.getText().toString() );
            postParam.put("valor", etValor.getText().toString() );

            RegistrarSolicitudTask rst = new RegistrarSolicitudTask();
            rst.execute( URL_REGISTRAR_SOLICITUD );
        }
    }

    /**
     * Clase encargada de procesar el registro del usuario*/
    class RegistrarSolicitudTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(FUEC_RegistroActivity.this, null, getString( R.string.txt_registrar_solicitud ), true);
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
                            build = new AlertDialog.Builder(FUEC_RegistroActivity.this);
                        }else{
                            build = new AlertDialog.Builder(FUEC_RegistroActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                        }
                        build.setTitle("Registro Solicitud FUEC");
                        build.setMessage( getString( R.string.txt_msg_solicitud_fuec_registrado) );
                        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        });
                        build.show();
                    } else if (estado.equals("ERROR")) {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(FUEC_RegistroActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        progreso.cancel();
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(FUEC_RegistroActivity.this, "Error registro solicitud FUEC", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(FUEC_RegistroActivity.this, "Error registro solicitud FUEC", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error","Error:"+ result.getInt("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException","LoginActivity.onPostExecute.RegistrarSolicitudTask:"+e.toString());
            }
        }
    }//RegistrarSolicitudTask
}
