package co.com.puli.trade.fdv.actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Mensaje;
import co.com.puli.trade.fdv.clases.TipoAlerta;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.adaptadores.MensajeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AlertasActivity extends AppCompatActivity implements LocationListener {
    private String URL_ALERTAS, URL_TIPOS_ALERTAS, URL_REGISTRO_ALERTA, id_usuario;
    private HashMap<String, String> postParam;
    private ListView lvAlertas;
    private CustomFonts fuentes;
    private LocationManager lm;
    private final int MIN_TIME_UPDATE = (int) TimeUnit.MINUTES.toMillis(1);
    private TipoAlerta alerta_emitida;
    private final int PERMISION_REQUEST_CALL_PHONE = 1; //Permisos para llamada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);

        URL_ALERTAS = getString(R.string.url_server_backend) + "consultar_alertas.jsp";
        URL_TIPOS_ALERTAS = getString(R.string.url_server_backend) + "consultar_tipos_alertas.jsp";
        URL_REGISTRO_ALERTA = getString(R.string.url_server_backend) + "registrar_alerta.jsp";

        fuentes = new CustomFonts(getAssets());

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvTitulo =  findViewById(R.id.tvTitulo);
        tvTitulo.setTypeface(fuentes.getRobotoThinFont());

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");

        lvAlertas = findViewById(R.id.lvAlertas);
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

            //Consultar Alertas
            postParam = new HashMap<>();
            postParam.put("id_usuario", id_usuario);
            ConsultarAlertasTask cat = new ConsultarAlertasTask();
            cat.execute(URL_ALERTAS);
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

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
     * Método encargado de controlar el evento click del botón agregar nueva alerta
     */
    public void clickNuevaAlerta(View v) {
        //Consultar mensajes
        ConsultarTiposAlertasTask ctat = new ConsultarTiposAlertasTask();
        ctat.execute(URL_TIPOS_ALERTAS);
    }

    /**
     * Método encargado de procesar y gestionar la creación de la nueva alerta
     *
     * @param tipo_alertas List<String>
     */
    public void mostrarNuevaAlerta(List<TipoAlerta> tipo_alertas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlertasActivity.this);

        //Layout nueva alerta
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_nueva_alerta, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = (TextView) view.findViewById(R.id.tvTituloAlerta);
        tvTitulo.setTypeface(fuentes.getBoldFont());

        final Spinner spAlertas = (Spinner) view.findViewById(R.id.spAlert);
        ArrayAdapter<TipoAlerta> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipo_alertas);
        spAlertas.setAdapter(adaptador);

        Button btCancelar = (Button) view.findViewById(R.id.btCancelar);
        btCancelar.setTypeface(fuentes.getRobotoThinFont());
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btGuardar = (Button) view.findViewById(R.id.btGuardar);
        btGuardar.setTypeface(fuentes.getRobotoThinFont());
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alerta_emitida = (TipoAlerta) spAlertas.getSelectedItem();
                //Verificar disponibilidad de Red
                if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    Location loc = getLocation();
                    if (loc != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //Registrar Alerta
                        postParam = new HashMap<>();
                        postParam.put("id_usuario", id_usuario);
                        postParam.put("id_alerta", "" + alerta_emitida.getId());
                        postParam.put("lat", "" + loc.getLatitude());
                        postParam.put("lng", "" + loc.getLongitude());
                        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        postParam.put("desc_alerta", alerta_emitida.getDescripcion());
                        postParam.put("id_nivel", ""+alerta_emitida.getNivel() );

                        RegistrarAlertaTask rat = new RegistrarAlertaTask(dialog);
                        rat.execute( URL_REGISTRO_ALERTA );
                    }
                } else {
                    new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error red", getString(R.string.txt_msg_error_red), true);
                }
            }
        });

        dialog.show();
    }

    /**
     * Función encargada de procesar la alerta generada después del registro en la BD (RegistrarAlertaTask)
     * Si el nivel de la alerta es 4 se visualiza mensaje de confirmación para llamar a emergencias,
     * De lo contrario se visualiza mensaje de confirmación
     */
    public void procesarAlerta()
    {
        if ( alerta_emitida.getNivel() == 4 )
        {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            if ( parametros.existenParametros() )
            {
                final String num_emergencia = parametros.getValue("nro_llamada_emergencia", 1);
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                {
                    //Visualizar dialogo para llamada de emergencia
                    AlertDialog.Builder build;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        build = new AlertDialog.Builder(AlertasActivity.this);
                    }else{
                        build = new AlertDialog.Builder(AlertasActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                    }
                    build.setCancelable(false);
                    build.setTitle("Alerta");
                    build.setMessage(R.string.txt_msg_llamar_emergencia);
                    build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Llamar al numero de emergencia
                            try {
                                Intent telIntent = new Intent(Intent.ACTION_CALL);
                                telIntent.setData(Uri.parse("tel:" + num_emergencia));
                                startActivity(telIntent);
                            }catch(SecurityException e){
                                Log.i("SecuritiException","AlertasActivity.procesarAlerta.SecuritiException:"+ e.toString() );
                            }
                        }
                    });
                    build.setNegativeButton(R.string.txt_cancelar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            //Visualizar mensaje de registro exitoso
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, getString(R.string.txt_nueva_alerta), getString(R.string.txt_registro_alerta_ok), true);
                            onResume();
                        }
                    });
                    build.show();
                }else{
                    //Conceder permisos para utilizar el telefono
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.CALL_PRIVILEGED}, PERMISION_REQUEST_CALL_PHONE);
                }
            }else{
                //Visualizar mensaje de registro exitoso
                confirmarRegistroNuevaAlerta();
            }
        }else
        {
            //Visualizar mensaje de registro exitoso
            confirmarRegistroNuevaAlerta();
        }
    }

    /**
     * Método encargado de visualizar y procesar la confirmación del registro de la nueva alerta
     * */
    public void confirmarRegistroNuevaAlerta()
    {
        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, getString(R.string.txt_nueva_alerta), getString(R.string.txt_registro_alerta_ok), true);
        onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISION_REQUEST_CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    procesarAlerta();
                } else {
                    new Utilidades().mostrarSimpleMensaje(this, "Alertas", getString(R.string.txt_msg_permiso_telefono_denegado), true);
                }
                return;
        }
    }

    @SuppressWarnings("ResourceType")
    /**
     * Método encargado de retornar la localización del usuario a partir de NetWork o GPS
     * @return location*/
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
        AlertDialog.Builder build = new AlertDialog.Builder(AlertasActivity.this);
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
     * Clase encargada de realizar el proceso de consulta de las alertas de la BD
     */
    public class ConsultarAlertasTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AlertasActivity.this, null, getString(R.string.txt_consulta_alertas), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
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
                            JSONArray alertas = result.getJSONArray("alertas");
                            ArrayList<Mensaje> arrayAlertas = new ArrayList<>();
                            for (int i = 0; i < alertas.length(); i++) {
                                JSONObject tmp = alertas.getJSONObject(i);
                                String id = tmp.getString("id");
                                String descripcion = tmp.getString("descripcion");
                                String fecha = tmp.getString("fecha");
                                Mensaje men = new Mensaje(id, fecha, descripcion);
                                arrayAlertas.add(men);
                            }

                            MensajeAdapter adaptador = new MensajeAdapter(AlertasActivity.this, arrayAlertas);
                            lvAlertas.setAdapter(adaptador);
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Lista alertas", getString(R.string.txt_msg_alertas_vacios), true);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarAlertasTask

    /**
     * Clase encargada de realizar el proceso de consulta de los tipos de alertas
     */
    public class ConsultarTiposAlertasTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AlertasActivity.this, null, getString(R.string.txt_consulta_alertas), true);
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
                            JSONArray tipo_alertas = result.getJSONArray("alertas");
                            List<TipoAlerta> list_alertas = new ArrayList<>();
                            for (int i = 0; i < tipo_alertas.length(); i++) {
                                JSONObject tmp = tipo_alertas.getJSONObject(i);
                                TipoAlerta ta = new TipoAlerta(tmp.getInt("id"), tmp.getString("descripcion"), tmp.getInt("nivel"));
                                list_alertas.add(ta);
                            }
                            mostrarNuevaAlerta(list_alertas);
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Lista Pasajeros", getString(R.string.txt_msg_alertas_vacios), true);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarTiposAlertasTask

    /**
     * Clase encargada de realizar el registro del alerta en la BD
     */
    public class RegistrarAlertaTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;
        AlertDialog dialog;

        public RegistrarAlertaTask(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AlertasActivity.this, null, getString(R.string.txt_registro_alerta), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
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
                            dialog.dismiss();
                            procesarAlerta();
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, getString(R.string.txt_nueva_alerta), getString(R.string.txt_msg_alertas_empty), true);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AlertasActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarAlertaTask
}
