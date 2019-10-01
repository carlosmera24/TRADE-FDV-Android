package co.com.puli.trade.fdv.clases;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.actividades.PrincipalActivity;

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

import static android.os.Build.VERSION_CODES.M;

/**
 * Clase utilizada para manejar la instancia de los servicios del GPS, de tal manera que se puede manejar independiente de la actividad
 * Esto permitirá usarlos servicios, por ejemplo, en varias actividades o en un hilo por separado.
 * Created by carlos on 18/04/16.
 */
public class GPSServices extends Service implements LocationListener
{
    private final Binder binderServices = new LocalGPSBinder();//Binder para las actividades clientes
    private boolean init_gps_services = false;
    private PrincipalActivity activity;
    private boolean GPSEnabled = false; //Bandera estado GPS
    private boolean NetWorkEnabled = false; //Bandera estado Red
    private Location location;
    private LocationManager lm;
    private final int MIN_TIME_UPDATE = (int) TimeUnit.SECONDS.toMillis(5); //Tiempo minimo de actualización, 5s
    private final int MIN_DISTANCE_UPDATE = 20; //Distancia minima de actualización (20 Metros)
    private int ESTADO_RUTA = 0; //Determina si la ruta ha iniciado (1), finalizado (2) o No ha iniciado (0)
    private int RUTA_INICIADA = 1;
    private String URL_REGISTRO_RASTREO, URL_REGISTRO_LLEGADA_COLEGIO, id_ruta, id_usuario;
    private HashMap<String,String> postParam;
    private boolean SERVICES_ACTIVE = false;//Bandera servicio activo;
    private AlertDialog dialogRutagrama = null;
    private AlertDialog dialogErrorRed = null;
    private Socket s = null;
    private PrintWriter output = null;
    private boolean socket_leer = true;//Abrir lectura de datos para el socket
    private ConexionSocketGPSRuta csgpsr = null;
    private Thread hilo_socket = null;
    private boolean CONECTANDO = false;//Bandera de control para nueva conexión

//---Métodos y componentes Service
    /**
     * Clase para la instancia Binder, que permite acceder a los métodos publicos que usarán las otras actividades
     * */
    public class LocalGPSBinder extends Binder
    {
        /**
         * Método encargado de retornar la instancia y asi acceder a los métodos*/
        public GPSServices getService(){
            return GPSServices.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Retornar el binder para los clientes
        return binderServices;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Método llamado cada vez que se inicia el servicio o se invoca desde otra actividad
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Debe envualuarse en ésta sesiòn al ser Service en lugar de IntentService
        if( intent != null ) {
            Bundle extras = intent.getExtras();
            if( extras != null ) {
                //Enventos enviados desde NetworkBroadcastReceiver
                if (extras.containsKey("network_connect")) {
                    if (extras.getBoolean("network_connect")) {
                        //Reconectar Socket
                        reconectarSocket();
                    }
                }
            }
        }
        //conectar socket si es necesario
        if( !conectadoSocket() )
        {
            conectarSocket();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        desconectarSocket();
    }

    /**
     * Método encargado de detener el servicio
     * */
    public void detenerServicio()
    {
        stopSelf();
    }

    //---End Métodos Service

//  Métodos LocationListener
    @Override
    public void onLocationChanged(Location location_changed)
    {
        if( isCanGetLocation() )
        {
            if (location_changed != location)
            {
                location = location_changed;
                if( location != null)
                {
                    activity.cargarMapa(location);

                    //Notificar cambio de posición
                    notificarUbicacionRastreo( location );

                    //Validar si la ruta ha iniciado
                    if ( getESTADO_RUTA() == RUTA_INICIADA )
                    {
                        //Validar Rutagrama
                        validarRutaGrama( location );
                        //Validar llegada colegio
                        validarLlegadaColegio( location_changed );
                    }
                }
            }
        }else{
            stopUseGPS();
        }
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
//  End Métodos LocationListener

    @SuppressWarnings("ResourceType")

    /**
     * Método encargado de retornar la actividad actual en ejecución
     * */
    private Activity getActividadActual()
    {
        if( activity != null ) {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) activity.getApplicationContext();
            return parametros.getActividadActual();
        }
        return null;
    }

    public void initGPSServices(final PrincipalActivity activity, String id_ruta, String id_usuario) {
        this.activity = activity;
        this.id_ruta = id_ruta;
        this.id_usuario = id_usuario;
        URL_REGISTRO_RASTREO = activity.getString( R.string.url_server_backend ) + "registro_rastreo.jsp";
        URL_REGISTRO_LLEGADA_COLEGIO = activity.getString( R.string.url_server_backend ) + "registrar_control_fin_ruta.jsp";
        init_gps_services = true;
    }

    /**
     * Método encargado de inicializar la localización del usuario a partir de NetWork o GPS
     * **/
    @SuppressLint("MissingPermission")
    public void iniciarLocation() {
        try {
            if( isCanGetLocation() )
            {
                //Obtener ubicación desde NetWork
                if ( NetWorkEnabled ) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
                    if (lm != null) {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                //Obtener ubicacion desde GPS
                if ( GPSEnabled ) {
                    if (location == null) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
                        if (lm != null) {
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
                SERVICES_ACTIVE = true;
                //Cargar mapa
                activity.cargarMapa( location );
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
        if( !CONECTANDO ) {
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
    public void reconectarSocket()
    {
        if( !CONECTANDO )
        {
            CONECTANDO = true;
            csgpsr = new ConexionSocketGPSRuta();
            hilo_socket = new Thread(csgpsr);
            hilo_socket.start();
        }
    }

    public void desconectarSocket()
    {
        try
        {
            if( s!= null )
            {
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
        }catch(Exception e )
        {
            Log.e("Exception","GPSServices.desconectarSocket.Exception:"+ e.getMessage());
        }
    }

    /**
     * Método encargado de enviar el mensaje al socket
     * @param msg JSONObjecto con el mensaje a enviar
     * @return true | false
     * */
    public boolean enviarMensajeSocket( final JSONObject msg )
    {
       if( puedeSocketEnviarMensajes() )
       {
           new Thread(new Runnable() {
               @Override
               public void run() {
                   try {
                       output.println(msg.toString());
                       output.flush();
                   }catch( final Exception e )
                   {
                       //Ocurrió una excepción al tratar de enviar el mensaje al Socket
                       Log.e("Exception","GPSServices.enviarMensajeSocket.Exception:"+e.toString() );
                       //Enviar a la actividad principal, en su hilo principal, la ejecución de la respuesta
                       activity.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               JSONObject res = new JSONObject();
                               try {
                                   res.put("estado", "ERROR");
                                   res.put("msg", "Error al enviar el mensaje, Exception:"+ e.toString());
                               }catch( JSONException e ){
                                   Log.e("Exception","GPSServices.enviarMensajeSocket.Exception.JSONException:"+e.toString() );
                               }
                               activity.procesarRespuestaServidorSockectGPS(res);
                           }
                       });
                   }
               }
           }).start();
           return true;
       }else{
           return false;
       }
    }

    /**
     * Verifica y retorna el estado de conexión
     * */
    public boolean conectadoSocket()
    {
        return  ( s != null && s.isConnected() && !s.isClosed() && !CONECTANDO );
    }

    /**
     * Verifica si es posible enviar mensajes utilizando el socket*/
    public boolean puedeSocketEnviarMensajes()
    {
        return ( s != null && !s.isClosed() && output != null && !CONECTANDO );
    }

    @SuppressWarnings("ResourceType")
    /**
     * Método encargado de detener el uso del GPS
     * */
    public void stopUseGPS() {
        if (lm != null) {
            lm.removeUpdates( GPSServices.this );
        }
    }

    /**
     * Método encargado de mostrar el dialogo de información para gestión del GPS
     * */
    public void mostrarDialogoGPS()
    {
        AlertDialog.Builder build = new AlertDialog.Builder( getActividadActual() );
        build.setCancelable(false);
        build.setTitle("Configurar GPS");
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
        if( getActividadActual() != null ) {
            if (!getActividadActual().isFinishing()) {
                build.show();
            }
        }
    }

    /**
     * Método encargado de evaluar si es posible obtener datos de la ubicación
     * */
    public boolean isCanGetLocation()
    {
        //Validar si es android M o superior
        if( Build.VERSION.SDK_INT >= M )
        {
            //Validar permisos de ubicacipon
           if(ContextCompat.checkSelfPermission( activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
           {
               //Solicitar permiso de ubicación
               ActivityCompat.requestPermissions( activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
           }
        }

        lm = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        GPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        NetWorkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if ( !GPSEnabled || !NetWorkEnabled ) {
            mostrarDialogoGPS();
            return false;
        }else {
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

    public boolean isSERVICES_ACTIVE()
    {
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
    public void notificarUbicacionRastreo( Location ubicacion )
    {
        //Verificar disponibilidad de Red
        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //verificar si hay conexión con el socket y se puede enviar mensajes por el mismo
            if( conectadoSocket() && puedeSocketEnviarMensajes() )//Enviar mensaje utilizando el socket
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
            }else //Registrar el movimiento utilizando el WebServices
            {
                //Registrar Movimiento Rastreo
                postParam = new HashMap<>();
                postParam.put("lat", "" + location.getLatitude());
                postParam.put("lng", "" + location.getLongitude());
                postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                postParam.put("id_ruta", id_ruta);

                RegistrarMvtoRuta irt = new RegistrarMvtoRuta();
                irt.execute( URL_REGISTRO_RASTREO );
            }
        }else{

            mostrarDialogoErrorRed();
        }
    }

    /**
     * Método encargado de visualizar mensaje de error de red, se encarga de visualizar solo un alerta y evitar pilas de mensajes
     * */
    public void mostrarDialogoErrorRed()
    {
        if( getActividadActual() != null ) {
            AlertDialog.Builder build;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                build = new AlertDialog.Builder(getActividadActual());
            }else{
                build = new AlertDialog.Builder(getActividadActual(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            }

            build.setCancelable(true);
            build.setTitle("Error red");
            build.setMessage( getActividadActual().getString(R.string.txt_msg_error_red_gps) );
            //Validar si el dialogo esta visible y en tal caso cerrarlo antes de volver a abrir el nuevo
            if (dialogErrorRed != null && dialogErrorRed.isShowing()) {
                dialogErrorRed.dismiss();
            }
            dialogErrorRed = build.create();
            //Visualizar alerta si la actividad actual no ha finalizado
            if (!getActividadActual().isFinishing()) {
                dialogErrorRed.show();
            }
        }
    }

    /**
     * Función encargada de validar Rugrama y visualizar alerta si es necesario
     * @param ubicacion Location actual
     * */
    public void validarRutaGrama( Location ubicacion )
    {
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) activity.getApplicationContext();

        if( parametros.existenRutaGramas() )
        {
            //Rutagramas desde Parametros generales
            ArrayList<RutaGrama> rutagramas = parametros.getRutaGramas();
            ArrayList<RutaGrama> rutagramasDescarte = parametros.getRutagramasDescartar();

            //Recorrer RutaGrama y validar cercanía
            for( int i=0; i < rutagramas.size(); i++ )
            {
                RutaGrama tmp = rutagramas.get(i);
                if( !rutagramasDescarte.contains( tmp ) )
                {
                    if( tmp.getLocation() != null )
                    {
                        //Verificar si la distancia es menor o igual a 200 metros
                        if( ( (int) ubicacion.distanceTo( tmp.getLocation() ) ) <= 200 )
                        {
                            //Descartar alerta para las siguientes validaciones
                            parametros.descartarRutaGrama( tmp );
                            //Visualizar alerta
                            mostrarDialogoRutagrama( tmp );
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
    public void mostrarDialogoRutagrama(final RutaGrama grama)
    {
        AlertDialog.Builder build = new AlertDialog.Builder( getActividadActual() );
        build.setCancelable(false);
        build.setTitle("Atención");
        build.setMessage( grama.getId_tipo() +" "+ getActividadActual().getString( R.string.txt_msg_rutagrama ) +" "+ grama.getDireccion() );
        build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //build.show();
        //Validar si el dialogo esta visible y en tal caso cerrarlo antes de volver a abrir el nuevo
        if( dialogRutagrama != null && dialogRutagrama.isShowing() )
        {
            dialogRutagrama.dismiss();
        }
        dialogRutagrama = build.create();
        //Visualizar alerta si la actividad actual no ha finalizado
        if( !getActividadActual().isFinishing() ) {
            dialogRutagrama.show();
        }
    }

    /**
     * Método encargado de validar si la posición actual se puede tomar como llegada al colegio
     * @param ubicacion Location actual
     * */
    public void validarLlegadaColegio( Location ubicacion )
    {
        try {
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) activity.getApplicationContext();
            JSONObject empresa = parametros.getEmpresa();
            if (empresa != null) {
                Double lat = empresa.getDouble("lat");
                Double lng = empresa.getDouble("lng");
                if( lat != 0 && lng != 0 )
                {
                    Location coord_colegio = new Location("");
                    coord_colegio.setLatitude( lat );
                    coord_colegio.setLongitude( lng );
                    if( ( (int) ubicacion.distanceTo( coord_colegio ) ) <= 20 )
                    {
                        //Registrar Movimiento Rastreo
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        postParam = new HashMap<>();
                        postParam.put("lat", "" + location.getLatitude());
                        postParam.put("lng", "" + location.getLongitude());
                        postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        postParam.put("id_ruta", id_ruta);

                        RegistrarMvtoRuta irt = new RegistrarMvtoRuta();
                        irt.execute( URL_REGISTRO_LLEGADA_COLEGIO );
                    }
                }
            }
        }catch( JSONException e )
        {
            Log.e("JSONException","GPSServices.validarLlegadaColegio.JSONException:"+ e.toString() );
        }catch(Exception e )
        {
            Log.e("Exception","GPSServices.validarLlegadaColegio.Exception:"+ e.toString() );
        }
    }

    /**
     * Clase encargada de realizar el proceso de registrar el movimiento de la ruta en la BD,
     * Se utiliza para registrar tanto el movimiento de la ruta, como la llegada al colegio, ya que
     * que los parametros son los mismo
     * */
    private class RegistrarMvtoRuta extends AsyncTask<String, Void, JSONObject>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                    if( !estado.equals("OK") ){
                        //Visualizar alerta si la actividad actual no ha finalizado
                        if( getActividadActual() != null ) {
                            if (!getActividadActual().isFinishing()) {
                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), "Conexión", activity.getString(R.string.txt_msg_error_consulta), true);
                            }
                        }
                        Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        //Visualizar alerta si la actividad actual no ha finalizado
                        if( getActividadActual() != null ) {
                            if (!getActividadActual().isFinishing()) {
                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), "Error", activity.getString(R.string.txt_msg_error_tiempo_conexion), true);
                            }
                        }
                    }else{
                        //Visualizar alerta si la actividad actual no ha finalizado
                        if( getActividadActual() != null ) {
                            if (!getActividadActual().isFinishing()) {
                                new Utilidades().mostrarSimpleMensaje(getActividadActual(), "Error", getActividadActual().getString(R.string.txt_msg_error_consulta), true);
                            }
                        }
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("GPSS-JSONException", e.toString());
            }
        }
    }//RegistrarMvtoRuta

    /**
     * Clase encargada de gestionar la conexión del Sokect con el servidor GPS Ruta
     * */
    private class ConexionSocketGPSRuta implements Runnable
    {
        @Override
        public void run() {
            try
            {
                s = new Socket( getActividadActual().getString( R.string.url_server ), 9002);
                output = new PrintWriter( s.getOutputStream() );
                BufferedReader input = new BufferedReader( new InputStreamReader( s.getInputStream() ) );

                //Enviar el ID del usuario
                JSONObject id_user = new JSONObject();
                id_user.put("id_usuario",id_usuario);
                id_user.put("id_ruta", id_ruta);
                id_user.put("perfil","3");
                output.println( id_user.toString() );
                output.flush();

                //Habilitar flujo de lectura de datos, mientras se tenga conexión
                String msg;
                while( socket_leer &&  s.isConnected() )
                {
                    msg = input.readLine();
                    if( msg != null )
                    {
                        try {
                            JSONObject jsonMsg = new JSONObject(msg);
                            Log.i("Res", jsonMsg.toString() );
                            switch (jsonMsg.getString("tipo"))
                            {
                                case "RES_COORD": //Mensaje enviado desde el servidor como respuesta a mensaje enviado
                                    JSONObject res_bbdd = null;
                                    try {
                                        res_bbdd = jsonMsg.getJSONObject("registro_msg_bbdd");
                                    }catch(JSONException e){
                                        Log.w("JSONException","GSPServices.ConexionSocketGPSRuta.Case.RES_COORD.JSONException:"+e.toString());
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
                                    if( jsonMsg.has("estado") )
                                    {
                                        if( jsonMsg.getString("estado").equals("OK") )
                                        {
                                            socket_leer = true;
                                        }
                                    }
                                    CONECTANDO = false;
                                    break;
                                case "LOCAL_ERROR"://Error local
                                    Log.e("errorSocket", String.format("GPSServices.ConexionSocketGPSRuta.run.Error[%s-%s-]", jsonMsg.getString("msg"), jsonMsg.getString("error") ) );
                                    break;
                            }
                        } catch (JSONException e)
                        {
                            Log.e("JSONException","GPSServices.ConexionSocketGPSRuta.run.JSONException:"+ e.getMessage());
                        }
                    }
                }
                input.close();
                CONECTANDO = false;
            }catch( UnknownHostException e)
            {
                Log.e("UnknownHostException","GPSServices.ConexionSocketGPSRuta.run.UnknownHostException:"+ e.getMessage());
                //Finalizar conexión socket
                desconectarSocket();
                CONECTANDO = false;
            }catch( IOException e )
            {
                Log.e("IOException","GPSServices.ConexionSocketGPSRuta.run.IOException:"+ e.getMessage());
                CONECTANDO = false;
            }catch ( JSONException e )
            {
                Log.e("JSONException","GPSServices.ConexionSocketGPSRuta.run.JSONException:"+ e.getMessage());
            }catch (Exception e )
            {
                Log.e("Exception","GPSServices.ConexionSocketGPSRuta.run.Exception:"+ e.getMessage());
            }
        }
    }
}
