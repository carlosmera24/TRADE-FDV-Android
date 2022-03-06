package co.com.puli.trade.fdv.actividades;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import co.com.puli.trade.fdv.BuildConfig;
import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.adaptadores.PDVRutaAdapter;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.DescargarImagenTask;
import co.com.puli.trade.fdv.clases.GPSServices;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.LocationMonitoringService;
import co.com.puli.trade.fdv.clases.NetworkBroadcastReceiver;
import co.com.puli.trade.fdv.clases.PDV;
import co.com.puli.trade.fdv.clases.TipoAlerta;
import co.com.puli.trade.fdv.clases.TipoInspeccion;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.adaptadores.InspeccionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrincipalActivity extends AppCompatActivity implements ServiceConnection
{
    private GPSServices gpsServ;
    private boolean gps_serv_conectado = false;//Conectado al servicio GPSServices
    private MapaFragment mf;
    private ImageSwitcher isFooter;
    private LinearLayout contentHeader;
    private SharedPreferences sharedPref;
    private HashMap<String,String> postParam;
    private Bundle bundle;
    private String URL_INICIO_RUTA,  URL_LISTA_PDV, URL_FIN_RUTA, URL_TIPO_INSPECCION, URL_REGISTRAR_INSPECCION, URL_IMAGEN_FOOTER, URL_REGISTRO_ALERTA, URL_CERRAR_SESION;
    private String URL_TIPOS_ALERTAS_SOS, id_vehiculo, id_ruta, nombre_usuario, id_usuario, id_perfil, id_fdv, img_url_usuario;
    private DrawerLayout drawer_layout;
    private NavigationView navigation_view;
    private CustomFonts fuentes;
    private Button btFinRuta;
    private final int RUTA_INICIADA = 1;
    private final int RUTA_NO_INICIADA = 0;
    private final int RUTA_FINALIZADA = 2;
    private final int BANNER_DURACION = 3000;
    private Timer timer_banner = null;
    private ArrayList<Bitmap> imgs_banner = null;
    private int pos_banner;
    private final int PERMISION_REQUEST_CALL_PHONE = 1; //Permisos para llamada
    private static final int REQUEST_PERMISSIONS_LOCATION = 101;
    private static final int REQUEST_PERMISSIONS_BACKGROUND_LOCATION = 103;
    private int state_request_permision_location = 0;
    private CircleImageView civUsuario;
    private boolean load_images = false; //Control carga imagenes banner y usuario
    private int OPCION_RES_SOCKECT_GPS = 0;//Variable de control para evaluación de respuestas del servidor
    private Button btn_activo = null; //Botón activo en operaciones para control de activación/desactivación
    private ProgressDialog progress_activo = null;//Dialogo de progreso activo, utilizado para inicio/fin ruta
    private NetworkBroadcastReceiver networkBroadcast = new NetworkBroadcastReceiver();
    private static final String TAG = PrincipalActivity.class.getSimpleName();
    private boolean mAlreadyStartedService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Bind al servicio gps (GPSServices)
        Intent intent = new Intent(this, GPSServices.class);
        getApplicationContext().bindService(intent,this, Context.BIND_AUTO_CREATE );

        URL_INICIO_RUTA = getString( R.string.url_server_backend ) + "registro_inicio_ruta.jsp";
        URL_FIN_RUTA = getString( R.string.url_server_backend ) + "registro_fin_ruta.jsp";
        URL_LISTA_PDV = getString( R.string.url_server_backend ) + "lista_pdv_fdv.jsp";
        URL_TIPO_INSPECCION = getString( R.string.url_server_backend ) + "consultar_tipos_inspeccion_empresa.jsp";
        URL_REGISTRAR_INSPECCION = getString( R.string.url_server_backend ) + "registrar_inspeccion.jsp";
        URL_IMAGEN_FOOTER = getString( R.string.url_server_backend ) + "consultar_imagenes_banner.jsp";
        URL_REGISTRO_ALERTA = getString(R.string.url_server_backend) + "registrar_alerta.jsp";
        URL_CERRAR_SESION = getString(R.string.url_server_backend) + "cerrar_sesion.jsp";
        URL_TIPOS_ALERTAS_SOS = getString(R.string.url_server_backend) + "consultar_tipos_alertas_panico.jsp";

        fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById( R.id.toolbar );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contentHeader = findViewById( R.id.layoutContentHeader );

        //Obtener la instancia del Objeto MapaFragment para interactuar con sus métodos
        mf = (MapaFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);

        Button btInspeccion = (Button) findViewById( R.id.btInspeccion );
        btInspeccion.setTypeface(fuentes.getBoldFont());

        //ImageSwitcher banner footer
        isFooter = findViewById( R.id.isFooter );
        isFooter.setFactory(() -> new ImageView( PrincipalActivity.this ));

        sharedPref = getSharedPreferences(getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);

        //Obtener datos extras enviados en el Intent
        bundle = getIntent().getExtras();
        id_vehiculo = bundle.getString("id_fdv");
        id_ruta = bundle.getString("id_ruta");
        nombre_usuario = bundle.getString("nombre_usuario");
        id_usuario = bundle.getString("id_usuario");
        id_perfil = bundle.getString("id_perfil");
        id_fdv = bundle.getString("id_fdv");
        id_perfil = bundle.getString("id_perfil");
        img_url_usuario = bundle.getString("imagen");

        //Drawer Menu
        drawer_layout = findViewById( R.id.drawer_layout );
        navigation_view = findViewById( R.id.navigationView );
        View layout_navigation_drawer = findViewById( R.id.view_drawer_navigation );
        TextView tv_label_notificacion = layout_navigation_drawer.findViewById( R.id.tvLabelNotificacion );
        tv_label_notificacion.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_ruta = layout_navigation_drawer.findViewById( R.id.tvLabelRuta );
        tv_label_ruta.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_msg = layout_navigation_drawer.findViewById( R.id.tvLabelMensajes );
        tv_label_msg.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_alertas = layout_navigation_drawer.findViewById( R.id.tvLabelAlertas );
        tv_label_alertas.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_chat =  layout_navigation_drawer.findViewById( R.id.tvLabelChat );
        tv_label_chat.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_control_pdv = layout_navigation_drawer.findViewById( R.id.tvLabelControlPDV );
        tv_label_control_pdv.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_agendamiento = layout_navigation_drawer.findViewById( R.id.tvLabelAgendamiento );
        tv_label_agendamiento.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_concurso = layout_navigation_drawer.findViewById( R.id.tvLabelConcurso );
        tv_label_concurso.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_label_logout = layout_navigation_drawer.findViewById( R.id.tvLabelLogout );
        tv_label_logout.setTypeface( fuentes.getRobotoThinFont() );
        TextView tv_nombre_usuario = layout_navigation_drawer.findViewById( R.id.tvNameUser );
        tv_nombre_usuario.setText( nombre_usuario );
        TextView tvEmpresa = layout_navigation_drawer.findViewById( R.id.tvEmpresa );
        tvEmpresa.setText( getNombreEmpresa() );

        civUsuario = layout_navigation_drawer.findViewById( R.id.civImageUsuario );

        //Datos para testing-No incluir en liberación
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("fch_ruta", "2018-06-13");
//        editor.putString("fin_ruta", "NO");
//        editor.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            if( !load_images ) {
                //Descargar imagenes para el footer
                try
                {
                    if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))
                    {
                        postParam = new HashMap<>();
                        postParam.put("id_perfil", id_perfil);
                        ConsultarImagenesBannerTask cibt = new ConsultarImagenesBannerTask();
                        cibt.execute(URL_IMAGEN_FOOTER);
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            validarInicioInspeccionRuta();
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconMenu = new BitmapDrawable(getResources(), imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_menu, displaymetrics.widthPixels, 0));
            getSupportActionBar().setHomeAsUpIndicator(iconMenu);

            //Restaurar banner
            iniciarBanner();

            //Si se ha iniciado la comunicación con el Servicio GPSService
            validarGPSServicio();

        }catch (Exception e)
        {
            Log.e("Exception", "PrincipalActivity.onResume.Exception:"+ e.toString() );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerBanner();
    }

    private void startStep1() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {

            //Passing null to indicate that it is executing for the first time.
            startStep2(null);

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.txt_google_play_no_disponible), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    private Boolean startStep2(DialogInterface dialog) {
        /*ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }*/

        if (dialog != null) {
            dialog.dismiss();
        }

        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startStep3();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    private void startStep3(){

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        /*       if (mAlreadyStartedService) {*/

        //Start location sharing service to app server.........
        Intent intent = new Intent(this, LocationMonitoringService.class);
        startService(intent);

        mAlreadyStartedService = true;
        //Ends................................................
        /* }*/
    }


    //-----Gestión de la ubicación y sus permisos---------
    public boolean checkPermissions()
    {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED )
        {
            return false;
        }

        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissions()
    {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q )
        {
            boolean shouldProvideRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION);

            boolean shouldProvideRationale2 =
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION);

            boolean shouldProvideRationalBackgroudLocation = ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION);

            if (shouldProvideRationale || shouldProvideRationale2 || shouldProvideRationalBackgroudLocation)
            {
                String msg = null;
                //Permissions para ubiación en tiempo real o mientras el app este en uso
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                        || ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
                {
                    msg = getString(R.string.msg_use_location);

                }else{ //Permissions para background
                    msg = getString(R.string.msg_use_location_background);
                }
                new Utilidades().mostrarMensajeBotonOK(this,
                        getString(R.string.txt_acceso_ubicacion),
                        msg,
                        getString(R.string.txt_aceptar),
                        (dialog, which) -> {
                            setRequestPermission();
                        });
            }else{
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the img_user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        REQUEST_PERMISSIONS_LOCATION);
            }
        }else if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED
        )
        {
            showMsgLocation(true);
        }
    }

    private void showMsgLocation( boolean backgroudLocation )
    {
        if( backgroudLocation )
        {
            AlertDialog.Builder build;
            String msg = null;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                build = new AlertDialog.Builder(this);
            }else{
                build = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            }

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q )
            {
                msg = backgroudLocation
                        ? getString(R.string.msg_use_location_background)
                        : getString(R.string.msg_use_location);
            }else{
                //Versiones anteiores a Android 10/Q
                msg = getString(R.string.msg_use_general_location );
            }

            build.setCancelable(false);
            build.setTitle( getString(R.string.txt_acceso_ubicacion) );
            build.setMessage( msg );
            build.setPositiveButton(getString(R.string.txt_btn_conceder_ubicacion),
                    (dialog, which) -> setRequestPermission());
            build.setNegativeButton(getString(R.string.txt_btn_denegar_ubiacion), (dialog, which) -> {
                dialog.cancel();
                if( backgroudLocation )
                {
                    new Utilidades().mostrarSimpleMensaje(this,
                            getString(R.string.txt_acceso_ubicacion),
                            getString( R.string.txt_permission_background_location_denied ),
                            true);
                }else{
                    new Utilidades().mostrarSimpleMensaje(this,
                            getString(R.string.txt_acceso_ubicacion),
                            getString( R.string.txt_permission_location_denied ),
                            true);
                }
            });
            build.show();
        }else{
            setRequestPermission();
        }
    }

    private void setRequestPermission()
    {
        String[] permissions = null;
        int request_code = 0;

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
            //Permissions para ubiación en tiempo real o mientras el app este en uso
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                permissions = new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                request_code = REQUEST_PERMISSIONS_LOCATION;
            }
            //Permissions para ubicación en segundo plano
            else if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                permissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                request_code = REQUEST_PERMISSIONS_BACKGROUND_LOCATION;
            }
        }else{
            //Versiones anteriores a android10/Q
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
            request_code = REQUEST_PERMISSIONS_LOCATION;
        }
        state_request_permision_location = request_code;
        ActivityCompat.requestPermissions(this, permissions, request_code);
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public int getStateRequestPermisionLocation(){
        return state_request_permision_location;
    }
    //-----Gestión de la ubicación y sus permisos---------


    @SuppressWarnings("ResourceType")
    @Override
    protected void onStop() {
        super.onStop();
        detenerBanner();
        limpiarMemoria();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarMemoria();
    }

    /*---Métodos para la conexión con el servicio---*/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        GPSServices.LocalGPSBinder binder = (GPSServices.LocalGPSBinder) service;
        gpsServ = binder.getService();
        gps_serv_conectado = true;

        //Registrar BrodcastReceiver para controlar la conexión a Internet y la reconexión al GPS Socket
        registerReceiver(networkBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION) );

        //validar servicio GPS para que se incialicen los paŕametros requeridos
        //Ya que solo ha este nivel se acaba de conectar el GPSServices
        validarGPSServicio();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        gps_serv_conectado = false;
    }
    /*---End Métodos para la conexión con el servicio---*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
                    drawer_layout.openDrawer( GravityCompat.START );
                return true;
            case R.id.miSos:
                //Consultar tipos de alertas SOS
                    ConsultarTiposAlertasTask ctat = new ConsultarTiposAlertasTask();
                    ctat.execute( URL_TIPOS_ALERTAS_SOS );
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Método encargado de validar el estado del servicio GPS
     * -Si hay conexión con el Servicio GPSServices (conectado) valida si es necesario inciar los páramentros básicos
     * y los ejecuta de ser necesario.
     * -inicia la ubicación de ser necesario y establece el estado de la ruta
     * */
    private void validarGPSServicio()
    {
        if( gps_serv_conectado )
        {
            //Iniciar los párametros básicos y establecerlos
            gpsServ.initGPSServices(PrincipalActivity.this, id_ruta, id_usuario );
            //Procesar para asegurarse que el GPS y sus métodos están activos (onLocationChaged y demás)
            // Incluso despues de un restablecimiento del GPS
            if( gpsServ.isCanGetLocation() )
            {
                gpsServ.iniciarLocation();
            }
            gpsServ.setESTADO_RUTA( getEstadoRuta() );
        }
    }

    /**
     * Método encargado de procesar las imagenes del banner
     * @param imagenes JSONArray con las imagenes a visualizar
     * */
    public void cargarImagenesBanner( JSONArray imagenes )
    {
        try
        {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Bitmap img = new Utilidades().imagenString64ToBitmap(imagenes.getString(0), displaymetrics.widthPixels, displaymetrics.heightPixels);
            if( img != null )
            {
                //Establecer imagen inicial del banner
                isFooter.setImageDrawable( new BitmapDrawable( getResources(), img ));
                pos_banner = 0;
                //Agregar imagenes al array de BitMap
                imgs_banner = new ArrayList<>();
                imgs_banner.add(img); //Agregar primera imagen ya decodificada
                //Recorrer las imagenes omitiendo la primera pocisión ya decodificada y agregada
                for( int i=1; i < imagenes.length(); i++ )
                {
                    img = new Utilidades().imagenString64ToBitmap( imagenes.getString(i), displaymetrics.widthPixels, displaymetrics.heightPixels );
                    if( img != null )
                    {
                        imgs_banner.add( img );
                    }
                }

                if( imgs_banner.size() > 1 )
                {
                    iniciarBanner();
                }

            }else
            {
                Log.e("ImagenBanner","Imagen null para banner");
            }
        }catch(JSONException e)
        {
            Log.e("JSONException","PincipalActivity.cargarImagenesBanner.JSONException:"+ e.toString() );
        }
    }

    /**
     * Método encargado de iniciar el Hilo de ejcución para el banner
     * */
    public void inicarTimerBanner()
    {
        timer_banner = new Timer();
        timer_banner.schedule(new TimerTask() {
            @Override
            public void run() {
                pos_banner++;
                if( pos_banner == imgs_banner.size() )
                {
                    pos_banner = 0;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isFooter.setImageDrawable( new BitmapDrawable( getResources(), imgs_banner.get( pos_banner ) ) );
                    }
                });
            }
        }, 0, BANNER_DURACION);
    }

    /**
     * Método encargado de procesar el inicio de la transición del banner,
     * Solo sí el array imgs_banner es mayor a 1
     * */
    public void iniciarBanner()
    {
        if( imgs_banner != null && imgs_banner.size() > 1 ) {
            if (timer_banner != null) {
                timer_banner.cancel();
            }
            pos_banner = 0;
            inicarTimerBanner();
        }
    }

    /**
     * Método encargado de detener la transición del banner
     * */
    public void detenerBanner()
    {
        if( timer_banner != null )
        {
            timer_banner.cancel();
            timer_banner = null;
        }
    }

    /**
     * Método encargado de evaluar y procesar la consutla de la imagen del usuario
     * */
    public void descargarImagenUsuario()
    {
        startStep1();
        //Descargar imagen usuario
        Bitmap img_usuario = null;
        if( !img_url_usuario.equals("NULL") )
        {
            img_usuario = getImagenUsuario( img_url_usuario );
            if( img_usuario != null )
            {
                civUsuario.setImageDrawable( new BitmapDrawable( getResources(), img_usuario) );
            }
        }
    }

    /**
     * Método encargado de realizar la descarga de la imagen del usuario
     * */
    public Bitmap getImagenUsuario( String img )
    {
        DescargarImagenTask dit = new DescargarImagenTask( this, getString( R.string.txt_msg_descarga_imagen ) );
        String URL_IMAGEN = getString( R.string.url_server_images ) + img;
        Bitmap img_usuario = null;
        try
        {
            img_usuario = dit.execute(URL_IMAGEN).get();
        }catch (Exception e)
        {
            Log.e("Exception", "LoginActivity.getImagenUsuario:" + e.toString() );
        }
        return img_usuario;
    }

    /**
     * Método encargado de cargar el mapa
     * */
    public void cargarMapa( Location location)
    {
        try {
            if (location != null) {
                mf.moverCamara( location.getLatitude(), location.getLongitude(), 18);
                mf.moverMarcador(location.getLatitude(), location.getLongitude(), BitmapFactory.decodeResource( getResources(), R.drawable.ic_marker_my_position), null );
            }
        }catch(Exception e){
            Log.e("Exception","PrincipalActivity.cargarMapa.Exception:"+ e.toString() );
        }
    }

    /**
     * Método encargado de validar el estado de inspección de la ruta para la fecha actual
     * La fecha es recuperada de la BBDD registrada localmente en GlobalParametrosGenerales
     * */
    public boolean inspeccionRealizada()
    {
        boolean res = false;
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        String fch_inspeccion = parametros.getFecha_inspeccion();
        if( fch_inspeccion != null )
        {
            String fch_actual = new SimpleDateFormat("yyyy-MM-dd").format( Calendar.getInstance().getTime() );
            if( fch_actual.equals( fch_inspeccion ) )
            {
                res = true;
            }
        }
        return res;
    }

    /**
     * Método encargado de validar la ruta
     * @return contante RUTA_NO_INICIADA|RUTA_FINALIZADA|RUTA_INICIADA
     * */
    private int getEstadoRuta()
    {
        int retorno = RUTA_NO_INICIADA;
        String fch_ruta = sharedPref.getString("fch_ruta", null);
        if( fch_ruta != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fch_actual = sdf.format(Calendar.getInstance().getTime());
            if( fch_ruta.equals( fch_actual ) )
            {
                //Validar FIN ruta
                String str_fin_ruta = sharedPref.getString("fin_ruta", null);
                if( str_fin_ruta != null &&  str_fin_ruta.equals("SI"))
                {
                    retorno = RUTA_FINALIZADA;
                }else{
                    retorno = RUTA_INICIADA;
                }
            }
        }

        return retorno;
    }

    /**
     * Método encargado de validar la ruta
     * 1- Verifica si la inspección ya se realizó
     * 2- Verifica si la ruta ha iniciado
     * 3- Visualiza el botón de acción necesario "Iniciar Ruta" o "Acciones lista alumnos"
     * */
    public void validarInicioInspeccionRuta()
    {
        if( inspeccionRealizada() )
        {
            switch( getEstadoRuta() )
            {
                case RUTA_INICIADA:
                    cambiarInicioRuta( 1 );
                    break;
                case RUTA_NO_INICIADA:
                    cambiarInicioRuta( 2 );
                    break;
                case RUTA_FINALIZADA:
                    cambiarInicioRuta( 2 ); //Mostrar botón inicia ruta
                    break;
                default:
                    limpiarDatosRuta();
                    break;
            }
        }
    }

    /**
     * Método encargado de limpiar el registro del inicio de ruta
     * */
    public void limpiarDatosRuta()
    {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("fch_ruta");
        editor.remove("fin_ruta");
        editor.apply();
    }

    /**
     * Método encargado de cargar layout ruta reemplazando el botón "Iniciar Ruta"
     * @param opcion:
     *              [1] Visualizar botón listado de alumnos y nombre conductor. Ocultar cerrar sesión
     *              [2] Visualizar botón iniciar ruta. Visualizar cerrar sesión
     * */
    public void cambiarInicioRuta( int opcion)
    {
        //Item DrawerMenu Cerrar Sesión
        LinearLayout logout = drawer_layout.findViewById( R.id.lItemLogout);

        switch (opcion)
        {
            case 1: //Botón listado de PDV
                    View vNew = getLayoutInflater().inflate( R.layout.layout_header_principal, null, false );
                    vNew.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    TextView tvUsuario = vNew.findViewById( R.id.tvNombrePasajero );
                    tvUsuario.setText(nombre_usuario);
                    Button btLista = vNew.findViewById( R.id.btListaPasajeros );
                    btLista.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Verificar disponibilidad de Red
                            if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                                if (id_fdv != null && !id_fdv.equals("")) {
                                    postParam = new HashMap<>();
                                    postParam.put("id_fdv", id_fdv);

                                    ConsultarListaPdvTask cpdv = new ConsultarListaPdvTask();
                                    cpdv.execute(URL_LISTA_PDV);
                                } else {
                                    new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", getString(R.string.txt_msg_error_id_ruta), true);
                                }
                            } else {
                                new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error red", getString(R.string.txt_msg_error_red), true);
                            }
                        }
                    });
                    btFinRuta = vNew.findViewById( R.id.btFinRuta );
                    btFinRuta.setTypeface(fuentes.getBoldFont());
                    btFinRuta.setVisibility(View.GONE);//Ocultar hasta que todos los alumnos esten OUT

                    contentHeader.removeAllViews();
                    contentHeader.addView(vNew);

                    //Deshabilitar menú cerrar sesión
                    logout.setVisibility(View.GONE);
                break;
            case 2: //Botón inicio de ruta
                    View viewIni = getLayoutInflater().inflate( R.layout.layout_iniciar_ruta, null, false );
                    viewIni.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    Button bt_iniciar = viewIni.findViewById( R.id.btIniciarRuta );
                    bt_iniciar.setTypeface(fuentes.getBoldFont());
                    bt_iniciar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clickIniciarRuta( view );
                        }
                    });

                    contentHeader.removeAllViews();
                    contentHeader.addView(viewIni);

                    //Habilitar menú cerrar sesión
                    logout.setVisibility(View.VISIBLE);
                break;
        }

        //Restaurar botón activo
        if( btn_activo != null ) {
            btn_activo.setEnabled(true);
            btn_activo = null;
        }
    }

    /**
     * Método encargado de conrolar el click sobre el botón inspección previa
     * */
    public void clickInspeccionPrevia( View view )
    {
       mostrarDialogoDeclaracionInspeccionPrevia();
    }

    /**
     * Click sobre el boton iniciar ruta
     * */
    public void clickIniciarRuta(View view)
    {
        //Deshabilitar botón
        btn_activo = (Button) view;
        btn_activo.setEnabled( false );
        //Verificar disponibilidad de Red
        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            if( id_vehiculo != null && !id_vehiculo.equals("") )
            {
                if (gpsServ.isCanGetLocation()) {
                    Location location = gpsServ.getLocation();
                    if (location != null)
                    {
                        if (gpsServ.conectadoSocket() && gpsServ.puedeSocketEnviarMensajes())
                        {
                            try {
                                //Mostrar dialogo
                                progress_activo = ProgressDialog.show(PrincipalActivity.this, null, getString(R.string.txt_registro_ruta), true);
                                progress_activo.setCancelable(false);
                                //Preparar parametros
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                //Enviar mensaje de inicio de ruta a través del Socket
                                JSONObject msg = new JSONObject();
                                msg.put("id_ruta", id_fdv);
                                msg.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                                msg.put("lat", "" + location.getLatitude());
                                msg.put("lng", "" + location.getLongitude());
                                msg.put("tipo", "INI");

                                //No manejar el registro en la BBDD desde el socket, sino desde el WebServices
                                //El registro del inicio de ruta se realiza en procesarRespuestaServidorSockectGPS();
                                msg.put("guardar_bbdd", "NO");
                                msg.put("enviar_notif", "NO");
                                if (gpsServ.enviarMensajeSocket(msg)) //Esperar respuesta del Sockect
                                {
                                    //Establecer espera de respuesta que será enviada desde GPSServices por el Socket Servidor GPS
                                    OPCION_RES_SOCKECT_GPS = 1;

                                } else//El socket no puede enviar mensajes
                                {
                                    new Utilidades().mostrarSimpleMensaje(this, "Inicio de ruta", getString(R.string.txt_msg_error_registro_inicio_ruta), true);
                                    //Restaurar botón
                                    btn_activo.setEnabled(true);
                                    btn_activo = null;
                                    //cancelar dialogo
                                    progress_activo.cancel();
                                    progress_activo = null;
                                }
                            } catch (JSONException e) {
                                Log.e("JSONException", "PrincipalActivity.clickIniciarRuta.JSONException:" + e.toString());
                                //cancelar dialogo
                                progress_activo.cancel();
                                progress_activo = null;
                                //Procesar inicio de ruta con el WebServices
                                registrarInicioRutaWebServices(location);
                            }
                        } else //Procesar inicio de ruta con el WebServices
                        {
                            registrarInicioRutaWebServices(location);
                        }
                    }
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", getString(R.string.txt_msg_error_id_vehiculo), true);
                //Restaurar botón
                btn_activo.setEnabled( true );
                btn_activo = null;
            }
        }else{
            new Utilidades().mostrarSimpleMensaje(this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            //Restaurar botón
            btn_activo.setEnabled( true );
            btn_activo = null;
        }
    }

    /**
     * Método encargado de procesar e iniciar el registro del inicio de la ruta utilizando el WebServices
     * */
    public void registrarInicioRutaWebServices( Location location)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        postParam = new HashMap<>();
        postParam.put("lat", "" + location.getLatitude());
        postParam.put("lng", "" + location.getLongitude());
        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
        postParam.put("id_ruta", id_ruta);

        IniciarRutaTask irt = new IniciarRutaTask();
        irt.execute(URL_INICIO_RUTA);
    }

    /**
     * Método encargado de finalizar el proceso de registro de inicio de ruta
     * -Registra datos en SharedPreferences
     * -Asigna el estado de la ruta para el GPSServices
     * -Cambia la visualiación del botón en la pantalla principal a Listado de alumnos
     * */
    public void finalizarRegistroInicioRuta()
    {
        //Registrar fecha de ruta
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fch_actual = sdf.format(Calendar.getInstance().getTime());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fch_ruta", fch_actual);
        editor.putString("fin_ruta", "NO");
        editor.apply();
        gpsServ.setESTADO_RUTA( RUTA_INICIADA ); //Difinir estado de ruta para el servicio GPS

        //Limpiar listado PDV con CheckIn
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        parametros.limpiarPDVCheckIn();

        cambiarInicioRuta(1);
    }

    /**
     * Click sobre el boton finalizar ruta
     * */
    public void clickFinalizarRuta(View view)
    {
        //Deshabilitar botón
        btn_activo = (Button) view;
        btn_activo.setEnabled( false );
        //Verificar disponibilidad de Red
        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            if( id_vehiculo != null && !id_vehiculo.equals("") )
            {
                if( gpsServ.isCanGetLocation() )
                {
                    Location location = gpsServ.getLocation();
                    if( location != null )
                    {
                        if( gpsServ.conectadoSocket() && gpsServ.puedeSocketEnviarMensajes() ) {
                            try {
                                //Mostrar dialogo
                                progress_activo = ProgressDialog.show(PrincipalActivity.this, null, getString( R.string.txt_registro_fin_ruta), true);
                                progress_activo.setCancelable(false);
                                //Preparar parámetros
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                //Enviar mensaje de fin de ruta a través del Socket
                                JSONObject msg = new JSONObject();
                                msg.put("id_ruta", id_ruta);
                                msg.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                                msg.put("lat", "" + location.getLatitude());
                                msg.put("lng", "" + location.getLongitude());
                                msg.put("tipo", "FIN");

                                //No manejar el registro en la BBDD desde el socket, sino desde el WebServices
                                //El registro del inicio de ruta se realiza en procesarRespuestaServidorSockectGPS();
                                msg.put("guardar_bbdd", "NO");
                                msg.put("enviar_notif", "NO");
                                if( gpsServ.enviarMensajeSocket(msg) )
                                {
                                    //Establecer espera de respuesta que será enviada desde GPSServices por el Socket Servidor GPS
                                    OPCION_RES_SOCKECT_GPS = 2;
                                }else//El socket no puede enviar mensajes
                                {
                                    new Utilidades().mostrarSimpleMensaje(this, "Fin de ruta", getString(R.string.txt_msg_error_registro_fin_ruta), true);
                                    //Restaurar botón
                                    btn_activo.setEnabled( true );
                                    btn_activo = null;
                                    //cancelar dialogo
                                    progress_activo.cancel();
                                    progress_activo = null;
                                }
                            } catch (JSONException e) {
                                Log.e("JSONException", "PrincipalActivity.clickFinalizarRuta.JSONException:" + e.toString());
                                //cancelar dialogo
                                progress_activo.cancel();
                                progress_activo = null;
                                //Procesar final de ruta con el WebServices
                                registrarFinalRutaWebServices( location );
                            }
                        }else //Procesar final de ruta con el WebServices
                        {
                            registrarFinalRutaWebServices( location );
                        }
                    }
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", getString(R.string.txt_msg_error_id_vehiculo), true);
                //Restaurar botón
                btn_activo.setEnabled( true );
                btn_activo = null;
            }
        }else{
            new Utilidades().mostrarSimpleMensaje(this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            //Restaurar botón
            btn_activo.setEnabled( true );
            btn_activo = null;
        }
    }

    /**
     * Método encargado de gestionar la respuesta del servidor Socket GPS
     * Aplicará después de enviar un mensaje, su llamado deberá realizarse desde GPSServices
     * */
    public void procesarRespuestaServidorSockectGPS(JSONObject resBBDD )
    {
        if( resBBDD != null ) {
            switch (OPCION_RES_SOCKECT_GPS) {
                case 1: //Inicio de ruta
                    try
                    {
                        switch( resBBDD.getString("estado") )
                        {
                            case "OK":
                                //Procesar el fin del inicio de la ruta
                                finalizarRegistroInicioRuta();
                                break;
                            case "PENDIENTE": //No se registro en la BBDD, está pendiente su registro
                                //Registrar el inicio de ruta a través del WebService
                                if( gpsServ.isCanGetLocation() ){
                                    Location location = gpsServ.getLocation();
                                    if (location != null) {
                                        registrarInicioRutaWebServices(location);
                                    }
                                }
                                break;
                            default:
                                new Utilidades().mostrarSimpleMensaje(this, "Inicio de ruta", getString(R.string.txt_msg_error_registro_inicio_ruta), true);
                                break;
                        }
                    } catch (JSONException e) {
                        Log.e("JSONExcepton", "PrincipalActivity.procesarRespuestaServidorSockectGPS.Case1.JSONException:" + e.toString());
                        new Utilidades().mostrarSimpleMensaje(this, "Inicio de ruta", getString(R.string.txt_msg_error_registro_inicio_ruta), true);
                    }
                    break;
                case 2: //Fin de ruta
                    try {
                        switch( resBBDD.getString("estado") )
                        {
                            case "OK":
                                //Procesar el fin del inicio de la ruta
                                finalizarRegistroFinalRuta();
                                break;
                            case "PENDIENTE": //No se registro en la BBDD, está pendiente su registro
                                //Registrar el fin de ruta a través del WebService
                                if( gpsServ.isCanGetLocation() ){
                                    Location location = gpsServ.getLocation();
                                    if (location != null) {
                                        registrarFinalRutaWebServices(location);
                                    }
                                }
                                break;
                            default:
                                new Utilidades().mostrarSimpleMensaje(this, "Fin de ruta", getString(R.string.txt_msg_error_registro_fin_ruta), true);
                                break;
                        }
                    } catch (JSONException e) {
                        Log.e("JSONExcepton", "PrincipalActivity.procesarRespuestaServidorSockectGPS.Case1.JSONException:" + e.toString());
                        new Utilidades().mostrarSimpleMensaje(this, "Find de ruta", getString(R.string.txt_msg_error_registro_fin_ruta), true);
                    }
                    break;
                default:
                    break;
            }
        }
        //Restaurar opción de control
        OPCION_RES_SOCKECT_GPS = 0;
        //cancelar dialogo
        if( progress_activo != null ) {
            progress_activo.cancel();
            progress_activo = null;
        }
    }

    /**
     * Método encargado de procesar e iniciar el registro del final de la ruta utilizando el WebServices
     * */
    public void registrarFinalRutaWebServices(Location location)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        postParam = new HashMap<>();
        postParam.put("lat", "" + location.getLatitude());
        postParam.put("lng", "" + location.getLongitude());
        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
        postParam.put("id_ruta", id_ruta);

        FinalizarRutaTask frt = new FinalizarRutaTask();
        frt.execute(URL_FIN_RUTA);
    }

    /**
     * Método encargado de finalizar el proceso de registro para el fin de la ruta
     * -Registra el fin de la ruta en SharedPreferences
     * -Limpia el rutagrama a descartar en párametros generales
     * -Resetea el boton principal a Inicio Visita
     * -Llama a onResumen() para cargar datos necesarios
     * */
    public void finalizarRegistroFinalRuta()
    {
        //Registrar fin de ruta
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fin_ruta", "SI");
        editor.apply();
        gpsServ.setESTADO_RUTA(RUTA_FINALIZADA); //Definir estado de ruta para el servicio GPS

        //Limpiar Rutagrama para descartar en parámetros generarles
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        parametros.limpiarRutaGramaDescarte();

        //Reset Btn Inicio
        cambiarInicioRuta( 1 );
        onResume();
    }

    /**
     * Método encargado de obtener el objeto Location a partir de GPSServices
     * return Location del usuario
     * */
    public Location getLocationGPS()
    {
        Location locGPS = null;
        if( gpsServ.isCanGetLocation() ) {
            locGPS = gpsServ.getLocation();
        }
        return locGPS;
    }

    /**
     * Método encargado de retornar el nombre de la empresa a partir de los parámetros globales
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
     * Método encargado de mostrar dialogo declaración de inpección previa
     * */
    public void mostrarDialogoDeclaracionInspeccionPrevia()
    {
        //Consultar Parámetros globales
        final GlobalParametrosGenerales global_param = (GlobalParametrosGenerales) getApplicationContext();
        if( global_param.existenParametros() && global_param.getValue( "texto_inspeccion_previa", 2) != null )
        {
            final String empresa = getNombreEmpresa();
            if( empresa != null ) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrincipalActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialogo_declaracion_inspeccion_previa, null, true);

                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                builder.setView(view);
                final AlertDialog dialog = builder.create();

                TextView tvTitulo = (TextView) view.findViewById(R.id.tvTitulo);
                tvTitulo.setTypeface(fuentes.getBoldFont());

                TextView tvMsgDecla = (TextView) view.findViewById(R.id.tvMsgDecla);
                tvMsgDecla.setText(global_param.getValue("texto_inspeccion_previa", 2));
                tvMsgDecla.setTypeface(fuentes.getRegularFont());

                final Button btContinuar = (Button) view.findViewById(R.id.btContinuar);
                btContinuar.setEnabled(false);
                btContinuar.setTypeface(fuentes.getRobotoThinFont());
                btContinuar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //cerrar dialogo
                        dialog.dismiss();
                        //Verificar disponibilidad de Red
                        if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                            //Consultar los datos de la inspección previa y visualizar el dialogo
                            postParam = new HashMap<String, String>();
                            postParam.put("empresa", empresa);
                            ConsultarDatosInspeccionTask ctnot = new ConsultarDatosInspeccionTask();
                            ctnot.execute(URL_TIPO_INSPECCION);
                        } else {
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error red", getString(R.string.txt_msg_error_red), true);
                        }
                    }
                });

                Switch swAceptar = (Switch) view.findViewById(R.id.swDeclaracion);
                swAceptar.setTypeface(fuentes.getRegularFont());
                swAceptar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        btContinuar.setEnabled(isChecked);
                    }
                });

                Button btCancelar = (Button) view.findViewById(R.id.btCancelar);
                btCancelar.setTypeface(fuentes.getRobotoThinFont());
                btCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }else{
                new Utilidades().mostrarSimpleMensaje( PrincipalActivity.this, "Inspección previa", getString( R.string.txt_msg_error_nombre_colegio), false );
            }
        }else{
            new Utilidades().mostrarSimpleMensaje( PrincipalActivity.this, "Inspección previa", getString( R.string.txt_no_parametros_globales), false );
        }
    }

    /**
     * Método encargado de visualizar el dialogo con el listado de inspección     *
     */
    public void mostrarDialogoInspeccion(ArrayList<TipoInspeccion> list_inspeccion)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( PrincipalActivity.this );

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_inspeccion_previa, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView( view );
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = view.findViewById( R.id.tvTitulo );
        tvTitulo.setTypeface( fuentes.getBoldFont() );

        final InspeccionAdapter adapter = new InspeccionAdapter( PrincipalActivity.this, list_inspeccion );
        ListView lista =  view.findViewById( R.id.lvListaInspeccion );
        lista.setAdapter( adapter );

        Button btGuardar = view.findViewById( R.id.btGuardar );
        btGuardar.setTypeface( fuentes.getRobotoThinFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( adapter.inspeccionFinalizada() )
                {
                    postParam = new HashMap<>();
                    String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    postParam.put("fecha", fecha);
                    postParam.put("id_fdv", id_fdv);
                    postParam.put("inspeccion", adapter.inspeccionToJSONArray().toString());

                    //Enviar registro a la BDD
                    RegistrarInspeccionTask rit = new RegistrarInspeccionTask(dialog);
                    rit.execute( URL_REGISTRAR_INSPECCION );
                }else{
                    new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this,"Inspección previa",getString( R.string.txt_msg_error_inspeccion_incompleta),true);
                }
            }
        });

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Método encargado de motrar el Dialogo con el listado de los PDV
     * */
    public void mostrarListaPDV( ArrayList<PDV> pdvs )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( PrincipalActivity.this );

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_lista_alumnos_ruta, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView tvTitulo =  view.findViewById( R.id.tvTitulo );
        tvTitulo.setTypeface( fuentes.getBoldFont() );

        PDVRutaAdapter adaptador = new PDVRutaAdapter( PrincipalActivity.this, pdvs, this, id_fdv );
        ListView lista =  view.findViewById( R.id.lvListaPasajeros );
        lista.setAdapter(adaptador);

        builder.setView(view);
        builder.show();
    }

    /**
     * Método encargado de validar si todos los alumnos estan con Check OUTs para habilitar el botón final de ruta
     * Se considera OUT o AUS como un mismo estado.
     * @param arrayPDV Listado de puntos de venta clase PDV
     * */
    public void validarOUTsFinalRuta( ArrayList<PDV> arrayPDV)
    {
        int total_outs = 0;
        for( int i=0; i < arrayPDV.size(); i++)
        {
            PDV ra = arrayPDV.get( i );
            if( ra.isCheckOut() || ra.isCheckAusente() )
            {
                total_outs ++;
            }
        }
        if( total_outs == arrayPDV.size() )
        {
            btFinRuta.setVisibility( View.VISIBLE );
        }
    }

    /**
     * Método encargado de cargar el listado de PDV, filtrando solo los que tienen CheckIn realizado,
     * Adicionalmente consulta los productos de la empresa*/
    public void cargarListaPDV( JSONArray arrayPDV ) {
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        try
        {
            ArrayList<PDV> arrayPdvs = new ArrayList<>();
            for( int i=0; i < arrayPDV.length(); i++ )
            {
                JSONObject tmp = arrayPDV.getJSONObject(i);
                String id = tmp.getString("id");
                String nombre = tmp.getString("nombre");
                String nombre_contacto = tmp.getString("nombre_contacto");
                String apellido_contacto = tmp.getString("apellido_contacto");
                String direccion = tmp.getString("direccion");
                String telefono = tmp.getString("telefono");
                String celular = tmp.getString("celular");
                String email = tmp.getString("email");
                String lng = tmp.getString("lng");
                String lat = tmp.getString("lat");
                String zona = tmp.getString("zona");
                int in = tmp.getInt("estado_in");
                int out = tmp.getInt("estado_out");
                int aus  = tmp.getInt("estado_ausente");
                PDV pdv = new PDV(id, nombre, nombre_contacto, apellido_contacto, direccion, telefono, celular, email, lat, lng, zona, in,out,aus);
                arrayPdvs.add( pdv );

                //Validar CheckIn del PDV y agregarlo si es neceario al listado general
                if( in > 0 ){
                    parametros.agregarPDVCheckIn( pdv );
                }
            }
            mostrarListaPDV(arrayPdvs);
            validarOUTsFinalRuta(arrayPdvs);
        } catch (JSONException e) {
        Log.e("JSONException", "NuevoPedidoActivity.cargarListaPDV.JSONException: " + e.toString());
        }


    }

    /**
     * Método encargado de inicializar y visualizar alerta SOS
     * */
    public void mostrarAlertaSOS( List<TipoAlerta> tipo_alertas )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrincipalActivity.this);

        //Layout nueva alerta
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogo_alerta_sos, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = view.findViewById(R.id.tvTituloAlerta);
        tvTitulo.setTypeface(fuentes.getBoldFont());

        final Spinner spAlertas = view.findViewById(R.id.spAlert);
        ArrayAdapter<TipoAlerta> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipo_alertas);
        spAlertas.setAdapter(adaptador);

        Button btCancelar = view.findViewById(R.id.btCancelar);
        btCancelar.setTypeface(fuentes.getRobotoThinFont());
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btGuardar = view.findViewById(R.id.btGuardar);
        btGuardar.setTypeface(fuentes.getRobotoThinFont());
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificar disponibilidad de Red
                if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                    Location loc = getLocationGPS();
                    if (loc != null) {
                        TipoAlerta alerta_emitida = (TipoAlerta) spAlertas.getSelectedItem();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //Registrar Alerta
                        postParam = new HashMap<>();
                        postParam.put("id_usuario", id_usuario);
                        postParam.put("id_alerta", "" + alerta_emitida.getId());
                        postParam.put("lat", "" + loc.getLatitude());
                        postParam.put("lng", "" + loc.getLongitude());
                        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        postParam.put("desc_alerta", alerta_emitida.getDescripcion());
                        postParam.put("id_nivel", ""+ alerta_emitida.getNivel() );
                        postParam.put("id_fdv", id_fdv );

                        RegistrarAlertaTask rat = new RegistrarAlertaTask(dialog);
                        rat.execute( URL_REGISTRO_ALERTA );
                    }
                } else {
                    new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error red", getString(R.string.txt_msg_error_red), true);
                }
            }
        });

        dialog.show();
    }

    /**
     * Función encargada de procesar la alerta generada después del registro en la BD (RegistrarAlertaTask)
     * Se visualiza mensaje de confirmación para llamar a emergencias,
     * De lo contrario se visualiza mensaje de confirmación
     */
    public void procesarAlerta()
    {
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
        if ( parametros.existenParametros() )
        {
            final String num_emergencia = parametros.getValue("nro_llamada_emergencia", 1);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
            {
                new Utilidades().mostrarMensajeBotonOK(PrincipalActivity.this, "Alerta", getString(R.string.txt_msg_llamar_emergencia), getString(R.string.txt_aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Llamar al numero de emergencia
                        try {
                            Intent telIntent = new Intent(Intent.ACTION_DIAL );
                            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                                telIntent.setData(Uri.parse( "tel:" + PhoneNumberUtils.normalizeNumber( num_emergencia ) ) );
                            }else{
                                telIntent.setData( Uri.parse( "tel:" + num_emergencia ) );
                            }
                            startActivity(telIntent);
                        }catch(SecurityException e){
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Llamar contacto", getString( R.string.txt_error_llamada ) + e.toString(), false );
                            Log.i("SecuritiException","PrincipalActivity.procesarAlerta.SecuritiException:"+ e.toString() );
                        }
                    }
                });
            }else{
                //Conceder permisos para utilizar el telefono
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.CALL_PRIVILEGED}, PERMISION_REQUEST_CALL_PHONE);
            }
        }else{
            //Visualizar mensaje de registro exitoso
            confirmarRegistroNuevaAlerta();
        }
    }

    /**
     * Método encargado de visualizar y procesar la confirmación del registro de la nueva alerta
     * */
    public void confirmarRegistroNuevaAlerta()
    {
        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, getString(R.string.txt_nueva_alerta), getString(R.string.txt_registro_alerta_ok), true);
        onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_LOCATION:
                if (grantResults.length <= 0) {
                    // If img_user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                }else if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                        requestPermissions(); //solicitar para pasar a Background location
                    }
                    startStep3();
                }else{
                    // Permission denied.
                    // Notify the img_user via a SnackBar that they have rejected a core permission for the
                    // app, which makes the Activity useless. In a real app, core permissions would
                    // typically be best requested during a welcome-screen flow.
                    // Additionally, it is important to remember that a permission might have been
                    // rejected without asking the img_user for permission (device policy or "Never ask
                    // again" prompts). Therefore, a img_user interface affordance is typically implemented
                    // when permissions are denied. Otherwise, your app could appear unresponsive to
                    // touches or interactions which have required permissions.
                    showSnackbar(R.string.permission_denied_explanation,
                            R.string.settings, view -> {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                }
                break;
            case REQUEST_PERMISSIONS_BACKGROUND_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    new Utilidades().mostrarSimpleMensaje(this,
                            getString(R.string.txt_acceso_ubicacion),
                            getString( R.string.txt_permission_background_location_denied ),
                            true);
                }else{
                    startStep3();
                    validarGPSServicio();
                }
                break;
            case PERMISION_REQUEST_CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    procesarAlerta();
                } else {
                    new Utilidades().mostrarSimpleMensaje(this, "Alerta S.O.S.", getString(R.string.txt_msg_permiso_telefono_denegado), true);
                }
                return;
            default:
                startStep3();
                validarGPSServicio();
                break;
        }
    }

    /**
     * Método encargado de cerrar la sesión activa del usuario
     * */
    public void cerrarSesion()
    {
        AlertDialog.Builder build;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        build = new AlertDialog.Builder(this);
        }else{
            build = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        }
        build.setTitle( getString( R.string.txt_msg_titulo_cerrar_sesion ) );
        build.setMessage( getString( R.string.txt_msg_cerrar_sesion ) );
        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postParam = new HashMap<>();
                postParam.put("id_usuario",id_usuario);
                CerrarSesionTask cst = new CerrarSesionTask();
                cst.execute( URL_CERRAR_SESION );
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
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     * */
    public void limpiarMemoria()
    {
//        ivFooter.setImageBitmap(null);
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
        }catch(NullPointerException e)
        {
            Log.e("PA-NullPointerException","PrincipalActivity.limpiarMemoria.NullPointerException:"+e.toString());
        }
        System.gc();
    }

    /**
     * Método encargado de controlar Click en los items del Menu Drawer
     * */
    public void onClickItemDrawerMenu(View v)
    {
        Intent intent;
        switch ( v.getId() )
        {
            case R.id.lItemNotificaciones: //Abrir actividad Notificaciones
                intent = new Intent(PrincipalActivity.this, NotificacionesActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                startActivity( intent );
                break;
            case R.id.lItemMensajes: //Abrir actividad Mensajes
                intent = new Intent(PrincipalActivity.this, MensajesActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                startActivity( intent );
                break;
            case R.id.lItemAlertas: //Abrir actividad Alertas
                intent = new Intent(PrincipalActivity.this, AlertasActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                startActivity( intent );
                break;
            case R.id.lItemChat: //Abrir actividad Chat
                intent = new Intent(PrincipalActivity.this, ChatUsuariosActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                startActivity( intent );
                break;
            case R.id.lItemControlPDV: //Abrir actividad Control PDV
                intent = new Intent(PrincipalActivity.this, ControlPDVActivity.class);
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
            case R.id.lItemAgendamiento: //Abrir actividad Agendamiento
                intent = new Intent(PrincipalActivity.this, AgendamientoActivity.class);
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
            case R.id.lItemConcurso: //Abrir actividad Concursos
                intent = new Intent( PrincipalActivity.this, ListadoConcursosActivity.class );
                intent.putExtra( "id_usuario", id_usuario );
                startActivity( intent );
                break;
            case R.id.lItemLogout: //Cerrar sesion
                cerrarSesion();
                break;
        }
    }

    /**
     * Clase encargada de procesar la consulta de los tipos de inspección
     * */
    private class ConsultarImagenesBannerTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( PrincipalActivity.this, null, getString( R.string.txt_consulta_imagenes), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0],postParam);
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
                    switch( estado)
                    {
                        case "OK":
                            JSONArray imagenes = result.getJSONArray("imagenes");
                            cargarImagenesBanner(imagenes);
                            break;
                        case "EMPTY":
                            Log.i("ImagenesBanner","No se encontraron images para el usuario "+ id_usuario);
//                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Inspección", getString(R.string.txt_msg_inspeccion_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
                //cambiar el estado de la carga de imagenes
                load_images = true;
                //consultar imagen de usuario
                descargarImagenUsuario();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }catch (Exception e )
            {
                progreso.cancel();
                Log.e("AA-Exception", e.toString());
            }
        }
    }//ConsultarImagenesBannerTask

    /**
     * Clase encargada de procesar la consulta de los tipos de inspección
     * */
    private class ConsultarDatosInspeccionTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( PrincipalActivity.this, null, getString( R.string.txt_consulta_inspeccion), true);
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
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            JSONArray tipo_inspeccion = result.getJSONArray("inspeccion");
                            ArrayList<TipoInspeccion> list_inspeccion = new ArrayList<>();
                            for( int i=0; i < tipo_inspeccion.length(); i++ )
                            {
                                JSONObject tmp = tipo_inspeccion.getJSONObject(i);
                                TipoInspeccion ta = new TipoInspeccion( tmp.getInt("id"), tmp.getString("desc"), 3 );
                                list_inspeccion.add( ta );
                            }
                            mostrarDialogoInspeccion( list_inspeccion );
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Inspección", getString(R.string.txt_msg_inspeccion_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarDatosInspeccionTask

    /**
     * Clase encargada de realizar el proceso de registrar la inspección previa en la BDD
     * */
    private class RegistrarInspeccionTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        AlertDialog alert;

        public RegistrarInspeccionTask(AlertDialog alert)
        {
            this.alert = alert;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString( R.string.txt_registro_inspeccion), true);
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
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Registrar fecha de la inspeccion
                        String fch_actual = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
                        parametros.setFecha_inspeccion( fch_actual );
                        cambiarInicioRuta(2);
                        alert.dismiss();
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, getString(R.string.txt_inspeccion), getString(R.string.txt_registro_inspeccion_ok), true);
                    }else{
                        String fch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("PA-JSONException", e.toString());
            }
        }
    }//RegistrarInspeccionTask

    /**
     * Clase encargada de realizar el proceso de registrar el inicio de la ruta en la BD
     * */
    private class IniciarRutaTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString( R.string.txt_registro_ruta), true);
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
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        finalizarRegistroInicioRuta();
                    }else{
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("PA-JSONException", e.toString());
            }
        }
    }//IniciarRutaTask

    /**
     * Clase encargada de realizar el proceso de consultar la lista de los PDVs
     * */
    private class ConsultarListaPdvTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString( R.string.txt_consulta_lista_pdv), true);
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
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Lista PDV", getString(R.string.txt_msg_lista_pdv_vacia), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
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
     * Clase encargada de realizar el proceso de registrar el final de la ruta en la BD
     * */
    private class FinalizarRutaTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString( R.string.txt_registro_fin_ruta), true);
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
                    if( estado.equals("OK") )
                    {
                        finalizarRegistroFinalRuta();
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    String fch = sdf.format( Calendar.getInstance().getTime() );
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("PA-JSONException", e.toString());
            }
        }
    }//FinalizarRutaTask

    /**
     * Clase encargada de realizar el registro del alerta en la BD
     */
    private class RegistrarAlertaTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;
        AlertDialog dialog;

        public RegistrarAlertaTask(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString(R.string.txt_registro_alerta), true);
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
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, getString(R.string.txt_nueva_alerta), getString(R.string.txt_msg_alertas_empty), true);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarAlertaTask

    /**
     * Clase encargada de realizar el cierre de sesión en la BBDD y procesar cierre local
     */
    private class CerrarSesionTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString(R.string.txt_cerrando_sesion), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
        }
        @SuppressLint("WrongConstant")
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK"))
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("ERROR") )
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_error_cierre_sesion_bd), true);
                        Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                    }
                }else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                //Limpiar datos registrados del usuario
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(PrincipalActivity.this, LoginActivity.class );
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 11) {
                    intent.addFlags(0x8000);
                }
                //detener el servicio
                gpsServ.detenerServicio();
                //Desvincular broadcast receiver network
                unregisterReceiver(networkBroadcast);
                //desconectar socket y servicio GPS
                if( gps_serv_conectado )
                {
                    //Desconectar socket
                    gpsServ.desconectarSocket();
                    gps_serv_conectado = false;
                }
                //Cerrar progress dialog
                progreso.cancel();
                //Abrir actividad para login
                startActivity(intent);
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//CerrarSesionTask

    /**
     * Clase encargada de realizar el proceso de consulta de los tipos de alertas
     */
    public class ConsultarTiposAlertasTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(PrincipalActivity.this, null, getString(R.string.txt_consulta_alertas), true);
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
                            mostrarAlertaSOS( list_alertas );
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Lista Alertas", getString(R.string.txt_msg_alertas_vacios), true);
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(PrincipalActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarTiposAlertasTask
}
