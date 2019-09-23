package co.com.puli.trade.fdv.actividades;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.concurrent.TimeUnit;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.Ciudad;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Utilidades;

public class NuevoPDVActivity extends AppCompatActivity implements LocationListener {
    String URL_CIUDADES, URL_REGISTRO, id_fdv;
    private CustomFonts fuentes;
    private HashMap<String, String> postParam;
    private LinearLayout content_layout;
    private final int MIN_TIME_UPDATE = (int) TimeUnit.MINUTES.toMillis(1);
    private LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pdv);

        URL_CIUDADES = getString(R.string.url_server_backend) + "consultar_datos_nuevo_pdv.jsp";
        URL_REGISTRO = getString(R.string.url_server_backend) + "registrar_pdv.jsp";

        fuentes = new CustomFonts(getAssets());

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Obtener datos extras enviados en el Intent
        Bundle bundle = getIntent().getExtras();
        id_fdv = bundle.getString("id_fdv");

        content_layout = findViewById( R.id.contentLayout );

        final EditText etEmail = findViewById( R.id.etEmail );
        etEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    new Utilidades().ocultarTeclado( NuevoPDVActivity.this, etEmail );//Ocultar teclado
                    //Procesar registro
                    guardarPDV();
                    return true;
                }
                return false;
            }
        });

        Button btGuardar = findViewById( R.id.btGuardar );
        Button btCancelar = findViewById( R.id.btCancelar );
        btGuardar.setTypeface( fuentes.getBoldFont() );
        btCancelar.setTypeface( fuentes.getBoldFont() );

        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarPDV();
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
            Drawable iconBack = new BitmapDrawable(getResources(), imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0));
            getSupportActionBar().setHomeAsUpIndicator(iconBack);

            Bitmap img = imgbm.decodificarImagen(getResources(), R.drawable.background_route_gray, displaymetrics.widthPixels, 0);

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            {
                content_layout.setBackground( new BitmapDrawable( getResources(), img ));
            }else{
                content_layout.setBackgroundDrawable(new BitmapDrawable( getResources(), img ));
            }

            //Consultar Alertas
            ConsultarListasTask cct = new ConsultarListasTask();
            cct.execute(URL_CIUDADES);
        } catch (Exception e) {
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
     * */
    public void limpiarMemoria()
    {
        getSupportActionBar().setHomeAsUpIndicator(null);
        content_layout.setBackgroundResource(0);
        System.gc();
    }

    /*
    * Método encargado de validar y procesar el registro del nuevo PDV
    * */
    public void guardarPDV()
    {
        EditText etNombreComercial = findViewById( R.id.etNombreComercial );
        EditText etNombre = findViewById( R.id.etNombreContacto );
        EditText etApellido = findViewById( R.id.etApellidoContacto );
        EditText etDir = findViewById( R.id.etDireccion );
        EditText etTel = findViewById( R.id.etTelefono );
        EditText etCel = findViewById( R.id.etCelular);
        Spinner spCiudad = findViewById( R.id.spCiudad );
        TextView tvCiudad = (TextView) spCiudad.getSelectedView();
        tvCiudad.setTextColor( ContextCompat.getColor( NuevoPDVActivity.this, R.color.black ) );
        Ciudad ciudad = (Ciudad) spCiudad.getSelectedItem();
        EditText etEmail = findViewById( R.id.etEmail );
        Spinner spZona = findViewById( R.id.spZonas );
        TextView tvZona = (TextView) spZona.getSelectedView();
        tvZona.setTextColor( ContextCompat.getColor( NuevoPDVActivity.this, R.color.black ) );
        Ciudad zona = (Ciudad) spZona.getSelectedItem();

        if( TextUtils.isEmpty( etNombreComercial.getText() ) )
        {
            etNombreComercial.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etNombre.getText() ) )
        {
            etNombre.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etApellido.getText() ) )
        {
            etApellido.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etDir.getText() ) )
        {
            etDir.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etTel.getText() ) )
        {
            etTel.setError(getString(R.string.txt_campo_requerido));
        }else if( TextUtils.isEmpty( etCel.getText() ) )
        {
            etCel.setError(getString(R.string.txt_campo_requerido));
        }else if( ciudad.getId() == 0 ){
            tvCiudad.setTextColor( Color.RED );
            tvCiudad.setError( getString(R.string.txt_campo_requerido) );
        }else if( TextUtils.isEmpty( etEmail.getText() ) )
        {
            etEmail.setError(getString(R.string.txt_campo_requerido));
        }else if( !android.util.Patterns.EMAIL_ADDRESS.matcher( etEmail.getText() ).matches() )
        {
            etEmail.setError( getString(R.string.txt_email_invalido) );
        }else if( zona.getId() == 0 ){
            tvZona.setTextColor( Color.RED );
            tvZona.setError( getString(R.string.txt_campo_requerido) );
        }else{
            //Verificar disponiblidad de Internet
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                Location loc = getLocation();
                if (loc != null) {
                    //Preparar parámetros para envío de registro
                    postParam = new HashMap<>();
                    postParam.put("comercio", etNombreComercial.getText().toString());
                    postParam.put("nombre", etNombre.getText().toString());
                    postParam.put("apellido", etApellido.getText().toString());
                    postParam.put("dir", etDir.getText().toString());
                    postParam.put("tel", etTel.getText().toString());
                    postParam.put("cel", etCel.getText().toString());
                    postParam.put("ciudad",  "" + ciudad.getId() );
                    postParam.put("email", etEmail.getText().toString());
                    postParam.put("lat", "" + loc.getLatitude() );
                    postParam.put("lng", "" + loc.getLongitude() );
                    postParam.put("zona", ""+ zona.getId() );
                    postParam.put("id_fdv", ""+ id_fdv );
                    RegistrarPDVTask rpdv = new RegistrarPDVTask();
                    rpdv.execute( URL_REGISTRO );
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
            }
        }

    }

    /**
     * Método encargado de cargar el listado de ciuades y zonas al spinner
     * @param result JSONObject con datos de la consulta*/
    public void cargarListas( JSONObject result ) {
        try
        {
            //Preparar lista ciudades
            JSONArray ciudades = result.getJSONObject("ciudades").getJSONArray("datos");
            List<Ciudad> list_ciudades = new ArrayList<>();
            //Agregar ciudad null para visualizar texto
            list_ciudades.add(new Ciudad(0, "--Seleccionar Ciudad--"));
            for (int i = 0; i < ciudades.length(); i++) {
                JSONObject tmp = ciudades.getJSONObject(i);
                Ciudad ta = new Ciudad(tmp.getInt("id"), tmp.getString("nombre"));
                list_ciudades.add(ta);
            }

            //preparar lista zonas
            JSONArray zonas = result.getJSONObject("zonas").getJSONArray("datos");
            List<Ciudad> list_zonas = new ArrayList<>();
            //Agregar ciudad null para visualizar texto
            list_zonas.add(new Ciudad(0, "--Seleccionar Zona--"));
            for (int i = 0; i < zonas.length(); i++) {
                JSONObject tmp = zonas.getJSONObject(i);
                Ciudad zona = new Ciudad(tmp.getInt("id"), tmp.getString("nombre"));
                list_zonas.add(zona);
            }

            //Agregar listas a los spinner's
            final Spinner spCiudades = findViewById(R.id.spCiudad);
            ArrayAdapter<Ciudad> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list_ciudades);
            spCiudades.setAdapter(adaptador);
            final Spinner spZonas = findViewById(R.id.spZonas);
            adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list_zonas);
            spZonas.setAdapter(adaptador);

        } catch (JSONException e) {

        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Método encargado de retornar la localización del usuario a partir de NetWork o GPS
     * @return location*/
    @SuppressLint("MissingPermission")
    public Location getLocation() {
        Location location = null;
        try {
            lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean GPSenabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean NetWorkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!GPSenabled || !NetWorkEnabled) {
                mostrarDialogoGPS();
            } else {
                //Obtener ubicación desde NetWork
                if (NetWorkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE, 0, this);
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                //Obtener ubicacion desde GPS
                if (GPSenabled) {
                    if (location == null) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATE, 0, this);
                        if (lm != null) {
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Método encargado de mostrar el dialogo de información para gestión del GPS
     */
    public void mostrarDialogoGPS() {
        AlertDialog.Builder build = new AlertDialog.Builder(NuevoPDVActivity.this);
        build.setCancelable(false);
        build.setTitle("Configurar GPS");
        build.setMessage(R.string.txt_msg_gps_error);
        build.setPositiveButton(R.string.txt_configurar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        build.setNegativeButton(R.string.txt_cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        build.show();
    }


    /**
     * Clase encargada de realizar el proceso de consulta de las listas necesarias
     */
    public class ConsultarListasTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(NuevoPDVActivity.this, null, getString(R.string.txt_consulta_lista_control_pdv), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttp(url[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK")) {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch (estado) {
                        case "OK":
                            cargarListas(result);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarCiudadesTask
    
    /**
     * Clase encargada de procesar el registro del nuevo PDV
     * */
    class RegistrarPDVTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(NuevoPDVActivity.this, null, getString( R.string.txt_registrar_nuevo_pdv ), true);
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
                        android.support.v7.app.AlertDialog.Builder build;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            build = new android.support.v7.app.AlertDialog.Builder(NuevoPDVActivity.this);
                        }else{
                            build = new android.support.v7.app.AlertDialog.Builder(NuevoPDVActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                        }
                        build.setTitle("Registro PDV");
                        build.setMessage( getString( R.string.txt_msg_pdv_registrado) );
                        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        });
                        build.show();
                    } else if (estado.equals("ERROR")) {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        progreso.cancel();
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Error registro PDV", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(NuevoPDVActivity.this, "Error registro PDV", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error","Error:"+ result.getInt("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException","NuevoPDVActivity.onPostExecute.RegistrarUsuarioTask:"+e.toString());
            }
        }
    }//RegistrarUsuarioTask
}
