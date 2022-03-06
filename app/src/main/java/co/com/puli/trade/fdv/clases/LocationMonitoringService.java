package co.com.puli.trade.fdv.clases;

import static android.os.Build.VERSION_CODES.M;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.SharedPrefrences.SessionManager;
import co.com.puli.trade.fdv.actividades.NotificationActivity;
import co.com.puli.trade.fdv.actividades.PrincipalActivity;
import co.com.puli.trade.fdv.database.DatabaseHelper;
import co.com.puli.trade.fdv.database.models.MvtoRastreo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;


/**
 * Created by devdeeds.com on 27-09-2017.
 */

public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    Double lati,lngi;



//    COPY FROM OLD

    private final Binder binderServices = new LocalGPSBinder();//Binder para las actividades clientes
    private boolean init_gps_services = false;
    private PrincipalActivity activity = null;
    private boolean GPSEnabled = false; //Bandera estado GPS
    private boolean NetWorkEnabled = false; //Bandera estado Red
    private Location location;
    private LocationManager lm;
    private final int MIN_TIME_UPDATE = (int) TimeUnit.SECONDS.toMillis(1); //Tiempo minimo de actualización, 1s
    private final int MIN_DISTANCE_UPDATE = 5; //Distancia minima de actualización (5 Metros)
    private int ESTADO_RUTA = 0; //Determina si la ruta ha iniciado (1), finalizado (2) o No ha iniciado (0)
    private int RUTA_INICIADA = 1;
    private String URL_REGISTRO_RASTREO, URL_REGISTRO_LLEGADA_COLEGIO, id_ruta, id_usuario;
    private HashMap<String, String> postParam;
    private boolean SERVICES_ACTIVE = false;//Bandera servicio activo;
    private AlertDialog dialogRutagrama = null;
    private AlertDialog dialogErrorRed = null;
    private Socket s = null;
    private PrintWriter output = null;
    private boolean socket_leer = true;//Abrir lectura de datos para el socket
    private ConexionSocketGPSRuta csgpsr = null;
    private Thread hilo_socket = null;
    private boolean CONECTANDO = false;//Bandera de control para nueva conexión
    PowerManager.WakeLock cpuWakeLock;

    public static final String PRIMARY_CHANNEL = "default";
    private static final int FIRST_RUN_TIMEOUT_MILISEC = 1 * 100;
    private static final int SERVICE_STARTER_INTERVAL_MILISEC = 1 * 100;
    private static final int SERVICE_TASK_TIMEOUT_SEC = 10;
    private final int REQUEST_CODE = 1;

    private AlarmManager serviceReStarterAlarmManager = null;

    public static final String NOTIFICATION_REPLY = "NotificationReply";
    public static final String CHANNNEL_ID = "channel_01";
    public static final int NOTIFICATION_ID = 1;
    private NotificationChannel mChannel;
    private NotificationManager notificationManager;
    public static final int GPS_NOTIFICATION = 1;
    private boolean isForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try
        {
            id_ruta = SessionManager.GetSharedPreference("id_ruta",LocationMonitoringService.this);
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mLocationClient.connect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        id_ruta = SessionManager.GetSharedPreference("id_ruta",LocationMonitoringService.this);
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);


        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();
        if (!isForeground)
        {
            if( getActividadActual() != null
            && ( (PrincipalActivity) getActividadActual() ).checkPermissions())
            {
                Log.v(TAG, "Starting the " + this.getClass().getSimpleName());

                startForeground(LocationMonitoringService.GPS_NOTIFICATION,
                        notifyUserThatLocationServiceStarted());
                // startInForeground();
                isForeground = true;
                // connect to google api client
                mLocationClient.connect();
                // acquire wakelock
            }
            Log.i("LocationMonitorService", "PrincipalActivity "+ getActividadActual() );
        }
        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

  /*  @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    /*
     * LOCATION CALLBACKS
     */
    public class LocalGPSBinder extends Binder {
        /**
         * Método encargado de retornar la instancia y asi acceder a los métodos*/
        public LocationMonitoringService getService() {
            return LocationMonitoringService.this;
        }
    }

    @Override
    public void onDestroy() {

        Log.v(TAG, "Stopping the " + this.getClass().getSimpleName());

        stopForeground(true);
        isForeground = false;

        // disconnect from google api client
        mLocationClient.disconnect();


        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        //Retornar el binder para los clientes
        return binderServices;
    }

    @Override
    public void onConnected(Bundle dataBundle)
    {
        try
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Log.d(TAG, "== Error On onConnected() Permission not granted");
                //Permission not granted by user so cancel the further execution.

                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

            Log.d(TAG, "Connected to Google API");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }


  /*  //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");



        if (location != null) {
            Log.d(TAG, "== location != null");
            lati = location.getLatitude();
            lngi = location.getLongitude();
            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

            new hitLoginApi().execute();
        }

    }*/
  private Notification notifyUserThatLocationServiceStarted() {

      Notification notification=null;
      NotificationCompat.Builder builder;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      {

          try
          {
              Intent intent = new Intent(this,NotificationActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              PendingIntent   pendingIntent = PendingIntent.getActivity(this,0, intent, 0);
         /* int importance = NotificationManager.IMPORTANCE_HIGH;

          if (mChannel == null) {

              AudioAttributes attributes = new AudioAttributes.Builder()
                      .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                      .build();

              mChannel = new NotificationChannel("0", "puli", importance);
              mChannel.setDescription("mychannel");
              mChannel.enableVibration(true);
              mChannel.enableLights(true);
              // mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),attributes);
//                mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
              notificationManager .createNotificationChannel(mChannel);
          }*/

              NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, getString(R.string.noti_channel_default), NotificationManager.IMPORTANCE_DEFAULT);
              chan1.setLightColor(Color.GREEN);
              chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
              getManager().createNotificationChannel(chan1);
              builder = new NotificationCompat.Builder(this,PRIMARY_CHANNEL);
              builder.setContentTitle( getString( R.string.txt_vehiculo_ubicacion ) );
              builder.setContentText( getString( R.string.txt_actualizando_ubicacion));
              builder.setContentIntent(pendingIntent);
              // builder.addAction(action);
              builder.setPriority(Notification.PRIORITY_HIGH);
              builder.setAutoCancel(true);
              // builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
              builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
              builder.setSmallIcon(R.mipmap.ic_launcher);
              notification = builder.build();
              getManager().notify(NOTIFICATION_ID,notification);
          }

          catch (Exception e)
          {
             e.printStackTrace();
          }
      }

      else
      {
          try
          {
              Intent intent = new Intent(this, NotificationActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);
              notification = new NotificationCompat.Builder(this)
                      .setContentTitle(getString( R.string.txt_vehiculo_ubicacion ) )
                      .setContentIntent(pendingIntent)
                      .setTicker(getString( R.string.txt_vehiculo_ubicacion ))
                      .setContentText( getString( R.string.txt_actualizando_ubicacion) )
                      .setSmallIcon(R.mipmap.ic_launcher)
                      .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                      .setOngoing(true).build();
          }
          catch (Exception e)
          {
              e.printStackTrace();
          }

      }


      return  notification;
  }

    private NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private void sendMessageToUI(String lat, String lng) {

        Log.d(TAG, "Sending info...");

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");

    }

    private class hitLoginApi extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
//            pDialog = new ProgressDialog(GPSServices.this);
//            pDialog.setMessage("wait");
//            pDialog.setCancelable(false);
//            pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
//            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            OkHttpClient client;
            client = new OkHttpClient();
            // String regIds=PrefManager.GetSharedPreference("RefreshToken",GPSServices.this);
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            //  RequestBody body = RequestBody.create(mediaType, "message&user_id=" + user_id + "&group_id=" + group_id + "&message_text=" + messageText + "&attach=" + "" + "");
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("http://mobileandwebsitedevelopment.com/chatsrun/api/latlong?lat="+lati+"&lng="+lngi)

                    .get()
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "fbb72eb5-733d-6610-9145-8e81e3a0654c")
                    .build();


            okhttp3.Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                e.getMessage();
            }
            try {
                if (response != null) {
                    return response.body().string();
                } else {
                    return "";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String response) {

//            pDialog.dismiss();

            if (response.equals("")) {
                Toast.makeText(LocationMonitoringService.this, "can_not_connect_to_internet", Toast.LENGTH_SHORT).show();

            } else {
                try {

//                    pDialog.dismiss();

                    JSONObject jsonObject=new JSONObject(response);
                    String success = jsonObject.getString("success");

                    if (success.equals("true")) {

                        // JSONArray jsonArray = jsonObject.getJSONArray("record");

                        // JSONObject object = jsonArray.getJSONObject(0);

                    } else {
//                        pDialog.dismiss();
                        Toast.makeText(LocationMonitoringService.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
//                    pDialog.dismiss();
                } catch (Exception e) {
//                    pDialog.dismiss();
                    if (e.getMessage() != null) {
                        Log.d("Error", e.getMessage());
                    }
                }
            }
        }
    }

/* COPY FROM GPS SERVICES*/

    /**
     * Método encargado de detener el servicio
     * */
    public void detenerServicio() {
        stopSelf();
    }

    //---End Métodos Service

    //  Métodos LocationListener
    @Override
    public void onLocationChanged(Location location_changed) {
      /*  if (isCanGetLocation()) {
            if (location_changed != location) {
                location = location_changed;
                if (location != null) {
                    activity.cargarMapa(location);

                    //Notificar cambio de posición*/
        if (location_changed != null) {
            Log.d(TAG, "== location != null");
            lati = location_changed.getLatitude();
            lngi = location_changed.getLongitude();
            notificarUbicacionRastreo(location_changed);
           // new hitLoginApi().execute();
            //Validar si la ruta ha iniciado
                 /*   if (getESTADO_RUTA() == RUTA_INICIADA) {
                        //Validar Rutagrama
                        validarRutaGrama(location);
                        //Validar llegada colegio
                        validarLlegadaColegio(location_changed);
                    }
                }
            }
        } else {
         //   stopUseGPS();
        }*/
        }
    }

//  End Métodos LocationListener

    @SuppressWarnings("ResourceType")

    /**
     * Método encargado de retornar la actividad actual en ejecución
     * */
    private Activity getActividadActual() {
        if(activity == null) {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            activity = (PrincipalActivity) parametros.getActividadActual();
        }

        return activity;
    }

    public void initGPSServices(final PrincipalActivity activity, String id_ruta, String id_usuario) {
        this.activity = activity;
        this.id_ruta = id_ruta;
        this.id_usuario = id_usuario;
        URL_REGISTRO_RASTREO = activity.getString(R.string.url_server_backend) + "registro_rastreo.jsp";
        URL_REGISTRO_LLEGADA_COLEGIO = activity.getString(R.string.url_server_backend) + "registrar_control_fin_ruta.jsp";
        init_gps_services = true;
    }

    /**
     * Método encargado de inicializar la localización del usuario a partir de NetWork o GPS
     * **/
    public void iniciarLocation() {
        try {
            if (isCanGetLocation()) {
                //Obtener ubicación desde NetWork
                if (NetWorkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                   /* lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }*/
                }
                //Obtener ubicacion desde GPS
                if (GPSEnabled) {
                    if (location == null) {
                        /*lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
                        if (lm != null) {
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }*/
                    }
                }
                SERVICES_ACTIVE = true;
                //Cargar mapa
                activity.cargarMapa(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método encargado de establecer la conexión con el servidor GPS Ruta Sockect
     * -Verifica si no hay conexión activa y en tal caso se conecta
     * */
    public void conectarSocket()
    {
        if (!CONECTANDO) {
            boolean conectar = true;
            if (s != null) {
                if (conectadoSocket()) {
                    conectar = false;
                }
            }

            if (conectar) {
                CONECTANDO = true;
                csgpsr = new ConexionSocketGPSRuta();
                hilo_socket = new Thread(csgpsr);
                hilo_socket.start();
            }
        }
    }

    /**
     * Métdo encargado de realizar la reconoxión con el SocketServer
     * Finaliza cualquier flujo abierto para su nueva inicialización
     * */
    public void reconectarSocket() {
        if (!CONECTANDO) {
            CONECTANDO = true;
            csgpsr =new ConexionSocketGPSRuta();
            hilo_socket = new Thread(csgpsr);
            hilo_socket.start();
        }
    }

    public void desconectarSocket() {
        try {
            if (s != null) {
                output.close();
                s.close();
                if (hilo_socket != null) {
                    hilo_socket.interrupt();
                    hilo_socket = null;
                }
                socket_leer = false;
                s = null;
                output = null;
            }
        } catch (Exception e) {
            Log.e("Exception", "GPSServices.desconectarSocket.Exception:" + e.getMessage());
        }
    }

    /**
     * Método encargado de enviar el mensaje al socket
     * @param msg JSONObjecto con el mensaje a enviar
     * @return true | false
     * */
    public boolean enviarMensajeSocket(final JSONObject msg) {
        if (puedeSocketEnviarMensajes()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        output.println(msg.toString());
                        output.flush();
                    } catch (final Exception e) {
                        //Ocurrió una excepción al tratar de enviar el mensaje al Socket
                        Log.e("Exception", "GPSServices.enviarMensajeSocket.Exception:" + e.toString());
                        //Enviar a la actividad principal, en su hilo principal, la ejecución de la respuesta
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject res = new JSONObject();
                                try {
                                    res.put("estado", "ERROR");
                                    res.put("msg", "Error al enviar el mensaje, Exception:" + e.toString());
                                } catch (JSONException e) {
                                    Log.e("Exception", "GPSServices.enviarMensajeSocket.Exception.JSONException:" + e.toString());
                                }
                                activity.procesarRespuestaServidorSockectGPS(res);
                            }
                        });
                    }
                }
            }).start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica y retorna el estado de conexión
     * */
    public boolean conectadoSocket() {
        return (s != null && s.isConnected() && !s.isClosed() && !CONECTANDO);
    }

    /**
     * Verifica si es posible enviar mensajes utilizando el socket*/
    public boolean puedeSocketEnviarMensajes() {
        return (s != null && !s.isClosed() && output != null && !CONECTANDO);
    }

    @SuppressWarnings("ResourceType")
    /**
     * Método encargado de detener el uso del GPS
     * */
/*    public void stopUseGPS() {
        if (lm != null) {
            lm.removeUpdates(LocationMonitoringService.this);
        }
    }*/

    private void startServiceReStarter() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, this.REQUEST_CODE, intent, 0);

        if (pendingIntent == null) {
            Toast.makeText(this, "Some problems with creating of PendingIntent", Toast.LENGTH_LONG).show();
        } else {
            if (serviceReStarterAlarmManager == null) {
                serviceReStarterAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                serviceReStarterAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + FIRST_RUN_TIMEOUT_MILISEC,
                        SERVICE_STARTER_INTERVAL_MILISEC, pendingIntent);

            }
        }
    }

    /**
     * Método encargado de mostrar el dialogo de información para gestión del GPS
     * */
    public void mostrarDialogoGPS() {
        AlertDialog.Builder build = new AlertDialog.Builder(getActividadActual());
        build.setCancelable(false);
        build.setTitle( getActividadActual().getString( R.string.txt_configure_gps ) );
        build.setMessage(R.string.txt_msg_gps_error);
        build.setPositiveButton(R.string.txt_configurar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActividadActual().startActivity(intent);
            }
        });
        build.setNegativeButton(R.string.txt_cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //Visualizar alerta si la actividad actual no ha finalizado
        if (getActividadActual() != null) {
            if (!getActividadActual().isFinishing()) {
                build.show();
            }
        }
    }

    /**
     * Método encargado de evaluar si es posible obtener datos de la ubicación
     * */
    public boolean isCanGetLocation() {
        //Validar si es android M o superior
        if (Build.VERSION.SDK_INT >= M) {
            //Validar permisos de ubicacipon
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Solicitar permiso de ubicación
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        GPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        NetWorkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!GPSEnabled || !NetWorkEnabled) {
            if( getActividadActual() != null
                    && ( (PrincipalActivity) getActividadActual()).getStateRequestPermisionLocation() > 0)
            {
                mostrarDialogoGPS();
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Método encargado de evaluar el estado del GPS
     * */
    public boolean isGPSEnabled() {
        return GPSEnabled;
    }

    /**
     * Método encargado de evaluar el estado de la red (NetWork)
     * */
    public boolean isNetWorkEnabled() {
        return NetWorkEnabled;
    }

    /**
     * Método encargado de retornar objerto Location
     * */
    public Location getLocation() {
        return location;
    }

    public int getESTADO_RUTA() {
        return ESTADO_RUTA;
    }

    public void setESTADO_RUTA(int ESTADO_RUTA) {
        this.ESTADO_RUTA = ESTADO_RUTA;
    }

    public boolean isSERVICES_ACTIVE() {
        return SERVICES_ACTIVE;
    }

    /**
     * Retorna el estado de inicialización de los parámetros básicos para el servicio establecidos por el método initGPSServices()*/
    public boolean isInitGPSServices() {
        return init_gps_services;
    }

    /**
     * Método encargado de notificar el cambio de posición para el registro del rastreo
     * */
    public void notificarUbicacionRastreo(Location ubicacion) {
        if( ubicacion != null && new Utilidades().isRutaIniciada( activity.getApplicationContext() )
            && !new Utilidades().isRutaFinalizada( activity.getApplicationContext() ) )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Verificar disponibilidad de Red
            if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                //verificar si hay conexión con el socket y se puede enviar mensajes por el mismo
                if (conectadoSocket() && puedeSocketEnviarMensajes())//Enviar mensaje utilizando el socket
                {
                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("id_ruta", id_ruta);
                        msg.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        msg.put("lat", "" + ubicacion.getLatitude());
                        msg.put("lng", "" + ubicacion.getLongitude());
                        msg.put("tipo", "MVTO");
                        msg.put("guardar_bbdd", "YES");
                        msg.put("enviar_notif", "YES");
                        enviarMensajeSocket(msg);
                    } catch (JSONException e) {
                        Log.e("JSONException", "GPSServices.notificarUbicacionRastreo.JSONException:" + e.toString());
                    }
                } else //Registrar el movimiento utilizando el WebServices
                {
                    //Registrar Movimiento Rastreo
                    postParam = new HashMap<>();
                    postParam.put("lat", "" + lati);
                    postParam.put("lng", "" + lngi);
                    postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                    postParam.put("id_ruta", id_ruta);

                    RegistrarMvtoRuta irt = new RegistrarMvtoRuta();
                    irt.execute(URL_REGISTRO_RASTREO);
                }
            } else {
                long id = new DatabaseHelper( getActividadActual().getApplicationContext() )
                        .setMvtoRastreo( new MvtoRastreo(
                                ubicacion.getLatitude(),
                                ubicacion.getLongitude(),
                                sdf.format(Calendar.getInstance().getTime()),
                                id_ruta
                        ));

                if( id <= 0 )
                {
                    new Utilidades().mostrarSimpleMensaje(this, getString(R.string.txt_title_error_database),
                            getString( R.string.txt_msg_error_insert_database_mvto_ruta ),
                            true  );
                }
            }
        }
    }

    /**
     * Función encargada de validar Rugrama y visualizar alerta si es necesario
     * @param ubicacion Location actual
     * */
    public void validarRutaGrama(Location ubicacion) {
        ArrayList<RutaGrama> rutagramas = new DatabaseHelper( activity.getApplicationContext() )
                                                            .getListRutasGramas();

        if( rutagramas.size() > 0 )
        {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) activity.getApplicationContext();
            //Rutagramas desde Parametros generales
            ArrayList<RutaGrama> rutagramasDescarte = parametros.getRutagramasDescartar();

            //Recorrer RutaGrama y validar cercanía
            for (int i = 0; i < rutagramas.size(); i++)
            {
                RutaGrama tmp = rutagramas.get(i);
                if (!rutagramasDescarte.contains(tmp)) {
                    if (tmp.getLocation() != null) {
                        //Verificar si la distancia es menor o igual a 200 metros
                        if (((int) ubicacion.distanceTo(tmp.getLocation())) <= 200) {
                            //Descartar alerta para las siguientes validaciones
                            parametros.descartarRutaGrama(tmp);
                            //Visualizar alerta
                            mostrarDialogoRutagrama(tmp);
                        }
                    }
                }
            }
        }
    }

    /**
     * Método encargado de mostrar el dialogo de información para el Rutagrama
     * @param grama Objeto RutaGrama
     * */
    public void mostrarDialogoRutagrama(final RutaGrama grama) {
        AlertDialog.Builder build = new AlertDialog.Builder(getActividadActual());
        build.setCancelable(false);
        build.setTitle(getActividadActual().getString(R.string.warning));
        build.setMessage(grama.getId_tipo() + " " + getActividadActual().getString(R.string.txt_msg_rutagrama) + " " + grama.getDireccion());
        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //build.show();
        //Validar si el dialogo esta visible y en tal caso cerrarlo antes de volver a abrir el nuevo
        if (dialogRutagrama != null && dialogRutagrama.isShowing()) {
            dialogRutagrama.dismiss();
        }
        dialogRutagrama = build.create();
        //Visualizar alerta si la actividad actual no ha finalizado
        if (!getActividadActual().isFinishing()) {
            dialogRutagrama.show();
        }
    }

    /**
     * Método encargado de validar si la posición actual se puede tomar como llegada al colegio
     * @param ubicacion Location actual
     * */
    public void validarLlegadaColegio(Location ubicacion) {
        try {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) activity.getApplicationContext();
            JSONObject colegio = parametros.getEmpresa();
            if (colegio != null) {
                Double lat = colegio.getDouble("lat");
                Double lng = colegio.getDouble("lng");
                if (lat != 0 && lng != 0) {
                    Location coord_colegio = new Location("");
                    coord_colegio.setLatitude(lat);
                    coord_colegio.setLongitude(lng);
                    if (((int) ubicacion.distanceTo(coord_colegio)) <= 20) {
                        //Registrar Movimiento Rastreo
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        postParam = new HashMap<>();
                        postParam.put("lat", "" + location.getLatitude());
                        postParam.put("lng", "" + location.getLongitude());
                        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        postParam.put("id_ruta", id_ruta);

                        RegistrarMvtoRuta irt =new RegistrarMvtoRuta();
                        irt.execute(URL_REGISTRO_LLEGADA_COLEGIO);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSONException", "GPSServices.validarLlegadaColegio.JSONException:" + e.toString());
        } catch (Exception e) {
            Log.e("Exception", "GPSServices.validarLlegadaColegio.Exception:" + e.toString());
        }
    }

    /**
     * Clase encargada de realizar el proceso de registrar el movimiento de la ruta en la BD,
     * Se utiliza para registrar tanto el movimiento de la ruta, como la llegada al colegio, ya que
     * que los parametros son los mismo
     * */
    private class RegistrarMvtoRuta extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK")) {
                    String estado = result.getString("estado"); //Estado WebServices
                    if (!estado.equals("OK")) {
                        //Visualizar alerta si la actividad actual no ha finalizado
                        if (getActividadActual() != null) {
                            if (!getActividadActual().isFinishing()) {
                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), getString(R.string.txt_connection), activity.getString(R.string.txt_msg_error_consulta), true);
                            }
                        }
                        Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        //Visualizar alerta si la actividad actual no ha finalizado
                        if (getActividadActual() != null) {
                            if (!getActividadActual().isFinishing()) {
                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), "Error", activity.getString(R.string.txt_msg_error_tiempo_conexion), true);
                            }
                        }
                    }
                    //Comentado despues de modificar el App para revisión de permisos 11/12/2021
                    else {
                        //Visualizar alerta si la actividad actual no ha finalizado
//                        if (getActividadActual() != null) {
//                            if (!getActividadActual().isFinishing()) {
//                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), "Error", getActividadActual().getString(R.string.txt_msg_error_consulta), true);
//                            }
//                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("GPSS-JSONException", e.toString());
            }
        }
    }//RegistrarMvtoRuta

    /**
     * Clase encargada de gestionar la conexión del Sokect con el servidor GPS Ruta
     * */
    private class ConexionSocketGPSRuta implements Runnable {
        @Override
        public void run() {
            try {
                s = new Socket(getActividadActual().getString(R.string.url_server), 9002);
                output = new PrintWriter(s.getOutputStream());
                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));


                //Enviar el ID del usuario
                JSONObject id_user = new JSONObject();
                id_user.put("id_usuario", id_usuario);
                id_user.put("id_ruta", id_ruta);
                id_user.put("perfil", "3");
                output.println(id_user.toString());
                output.flush();

                //Habilitar flujo de lectura de datos, mientras se tenga conexión
                String msg;
                while (socket_leer && s.isConnected()) {
                    msg = input.readLine();
                    if (msg != null) {
                        try {
                            JSONObject jsonMsg = new JSONObject(msg);
                            Log.i("Res", jsonMsg.toString());
                            switch (jsonMsg.getString("tipo")) {
                                case "RES_COORD": //Mensaje enviado desde el servidor como respuesta a mensaje enviado
                                    JSONObject res_bbdd = null;
                                    try {
                                        res_bbdd = jsonMsg.getJSONObject("registro_msg_bbdd");
                                    } catch (JSONException e) {
                                        Log.w("JSONException", "GSPServices.ConexionSocketGPSRuta.Case.RES_COORD.JSONException:" + e.toString());
                                    }
                                    final JSONObject res_final = res_bbdd;
                                    //Enviar a la actividad principal, en su hilo principal, la ejecución de la respuesta
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.procesarRespuestaServidorSockectGPS(res_final);
                                        }
                                    });
                                    break;
                                case "RES_MSG": //Respuesta al enviar un mensaje
                                    break;
                                case "RES": //Respuesta del servidor
                                    if (jsonMsg.has("estado")) {
                                        if (jsonMsg.getString("estado").equals("OK")) {
                                            socket_leer = true;
                                        }
                                    }
                                    CONECTANDO = false;
                                    break;
                                case "LOCAL_ERROR"://Error local
                                    Log.e("errorSocket", String.format("GPSServices.ConexionSocketGPSRuta.run.Error[%s-%s-]", jsonMsg.getString("msg"), jsonMsg.getString("error")));
                                    break;
                            }
                        } catch (JSONException e) {
                            Log.e("JSONException", "GPSServices.ConexionSocketGPSRuta.run.JSONException:" + e.getMessage());
                        }
                    }
                }
                input.close();
                CONECTANDO = false;
            } catch (UnknownHostException e) {
                Log.e("UnknownHostException", "GPSServices.ConexionSocketGPSRuta.run.UnknownHostException:" + e.getMessage());
                //Finalizar conexión socket
                desconectarSocket();
                CONECTANDO = false;
            } catch (IOException e) {
                Log.e("IOException", "GPSServices.ConexionSocketGPSRuta.run.IOException:" + e.getMessage());
                CONECTANDO = false;
            } catch (JSONException e) {
                Log.e("JSONException", "GPSServices.ConexionSocketGPSRuta.run.JSONException:" + e.getMessage());
            } catch (Exception e) {
                Log.e("Exception", "GPSServices.ConexionSocketGPSRuta.run.Exception:" + e.getMessage());
            }
        }
    }



}