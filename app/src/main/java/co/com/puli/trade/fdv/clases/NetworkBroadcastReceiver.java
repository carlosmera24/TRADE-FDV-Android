package co.com.puli.trade.fdv.clases;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.database.DatabaseHelper;
import co.com.puli.trade.fdv.database.models.Inspeccion;
import co.com.puli.trade.fdv.database.models.MvtoCheck;
import co.com.puli.trade.fdv.database.models.MvtoFinRuta;
import co.com.puli.trade.fdv.database.models.MvtoInicioRuta;
import co.com.puli.trade.fdv.database.models.MvtoRastreo;

/**
 * Clase encargada de controlar los cambios en la conexión de Internet
 * Especialmente el reinicio de la conexión
 * Created by carlos on 23/06/17.
 */

public class NetworkBroadcastReceiver extends BroadcastReceiver
{
    private boolean is_update_server = false;
    private boolean is_update_local_bbdd = false;
    private int NOTIFICATION_ID = 501;
    private NotificationCompat.Builder builderNotification = null;
    NotificationManagerCompat manager = null;
    private int number_current_updated = 0;
    private int number_max_update = 0;
    private int number_max_steps_update_local = 2;//Numero de procesos a ejecutar para actualizar bbdd
    private int number_current_updated_local = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Verificar si la conexión está disponible
        boolean conectado = new Utilidades().redDisponible( ( (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE ) ) );
        //Enviar estado al GPSServices
        Intent intentGPS = new Intent(context, GPSServices.class);
        intentGPS.putExtra("network_connect", conectado );
        context.startService( intentGPS );

        if( conectado  )
        {
            //validar si se require actualizar la bbdd local
            if( new Utilidades().requireActualizacionOffline(context) )
            {
                //Iniciar actualización
                iniciarActualizacionLocal( context );
            }else
            {
                //Validar si require actuialización del servidor
                requiereUpload( context );
            }
        }else{
            is_update_server = false;
        }
    }

    //--------------Procesar actualización de bbdd local-------------------
    private void iniciarActualizacionLocal(Context context)
    {
        //Notification progress
        if( !is_update_local_bbdd )
        {
            builderNotification = startNotificationUpdateLocalBBDD(context);
            is_update_local_bbdd = true;
            //Consultar datos inspeccion
            ConsultarDatosInspeccionTask ctnot = new ConsultarDatosInspeccionTask(context);
            String URL_TIPO_INSPECCION = context.getString(R.string.url_server_backend) + "consultar_tipos_inspeccion_empresa.jsp";
            ctnot.execute(URL_TIPO_INSPECCION);
        }
    }

    private NotificationCompat.Builder startNotificationUpdateLocalBBDD(Context context)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString( R.string.channel_id_default ) )
                .setSmallIcon( R.mipmap.ic_launcher )
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle( context.getString( R.string.txt_title_update_local_bbdd ) )
                .setContentText( context.getString( R.string.text_content_small_update_local_bbdd ) )
                .setStyle( new NotificationCompat.BigTextStyle().bigText( context.getString( R.string.text_content_update_local_bbdd ) ) )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .setProgress(number_max_steps_update_local, 0, false);

        manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());

        return builder;
    }

    private void updateStepsProgressLocalBBDD(Context context)
    {
        number_current_updated_local++;
        builderNotification.setProgress(number_max_steps_update_local, number_current_updated, false);
        manager.notify(NOTIFICATION_ID, builderNotification.build());
        if( number_current_updated_local == number_current_updated_local)
        {
            finishProgressLocalBBDD(context);
        }
    }

    public void finishProgressLocalBBDD(Context context)
    {
        PendingIntent close = PendingIntent.getActivity(context, 0, new Intent(), 0);
        builderNotification.setContentText( context.getString( R.string.content_small_finshed_update_local_bbdd ))
                .setStyle( new NotificationCompat.BigTextStyle().bigText( context.getString( R.string.content_finshed_update_local_bbdd ) ) )
                .setProgress(0, 0, false)
                .setAutoCancel(true)
                .setContentIntent( close );

        manager.notify(NOTIFICATION_ID, builderNotification.build());
        number_max_steps_update_local = 0;
        number_current_updated_local = 0;
        is_update_local_bbdd = false;
        //Establecer fecha de actualización en shared preferences
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fch_actual = sdf.format(Calendar.getInstance().getTime());
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fch_updated_local_bbdd", fch_actual );
        editor.apply();
        //ejecutar validación para upload actualización al servidor
        requiereUpload( context);
    }

    /**
     * Clase encargada de procesar la consulta de los tipos de inspección
     * */
    private class ConsultarDatosInspeccionTask extends AsyncTask<String, Void, JSONObject>
    {
        Context context;
        HashMap<String,String> postParam = new HashMap<>();

        public ConsultarDatosInspeccionTask(Context context)
        {
            this.context = context;
            postParam.put("empresa", new DatabaseHelper(context).getUsuario().getEmpresa());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... url)
        {ConsultaExterna ce=null;
            try
            {
                ce = new ConsultaExterna();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

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
                    if( estado.equals("OK") )
                    {
                        JSONArray tipo_inspeccion = result.getJSONArray("inspeccion");
                        DatabaseHelper db = new DatabaseHelper( context );
                        db.setTiposInspeccion( tipo_inspeccion );
                        updateStepsProgressLocalBBDD(context);
                        //Consultar alumnos de la ruta
                        ConsultarListaAlumnosTask clat = new ConsultarListaAlumnosTask( context );
                        String URL_LISTA_PASAJEROS = context.getString(R.string.url_server_backend) + "lista_pdv_fdv.jsp";
                        clat.execute(URL_LISTA_PASAJEROS);
                    }else{
                        //Error en la consulta
                        Log.e("NetworkBR", "ConsultarDatosInspeccionTask Estado:"+ estado);
                    }
                }else
                {
                    Log.e("NetworkBR", "ConsultarDatosInspeccionTask ERROR Consulta externa");
                }
            }catch ( JSONException e )
            {
                Log.e("NetworkBR-JSONException", "ConsultarDatosInspeccionTask"+ e);
            }
        }
    }//ConsultarDatosInspeccionTask

    /**
     * Clase encargada de realizar el proceso de consultar la lista de alumnos
     * */
    private class ConsultarListaAlumnosTask extends AsyncTask<String, Void, JSONObject>
    {
        Context context;
        HashMap<String,String> postParam = new HashMap<>();

        ConsultarListaAlumnosTask( Context context )
        {
            this.context = context;
            postParam.put("id_fdv", new DatabaseHelper( context).getUsuario().getId_fdv() );
        }

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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if ( estado.equals("OK") )
                    {
                        JSONArray pdvs = result.getJSONArray("pdv");
                        ArrayList<PDV> arrayPDVs = new ArrayList<>();
                        for( int i=0; i < pdvs.length(); i++ )
                        {
                            JSONObject pdv = pdvs.getJSONObject(i);
                            arrayPDVs.add( new PDV(
                                                    pdv.getString("id"),
                                                    pdv.getString("nombre"),
                                                    pdv.getString("nombre_contacto"),
                                                    pdv.getString("apellido_contacto"),
                                                    pdv.getString("direccion"),
                                                    pdv.getString("telefono"),
                                                    pdv.getString("celular"),
                                                    pdv.getString("email"),
                                                    pdv.getString("lng"),
                                                    pdv.getString("lat"),
                                                    pdv.getString("zona"),
                                                    pdv.getInt("estado_in"),
                                                    pdv.getInt("estado_out"),
                                                    pdv.getInt("estado_ausente")
                                        ));
                        }
                        //Actualizar listado de alumnos local
                        new DatabaseHelper( context ).updateAlumnosRuta( arrayPDVs );
                        updateStepsProgressLocalBBDD(context);
                    }else{
                        //Error en la consulta
                        Log.e("NetworkBR", "ConsultarListaAlumnosTask Estado:"+ estado);
                    }
                }else
                {
                    Log.e("NetworkBR", "ConsultarListaAlumnosTask ERROR Consulta externa");
                }
            }catch ( Exception e )
            {
                Log.e("NetworkBR-JSONException", "ConsultarListaAlumnosTask"+ e);
            }
        }
    }//ConsultarListaAlumnosTask

    //--------------Procesar actualización de bbdd local-------------------


    //--------------Procesar upload datos de bbdd local-------------------
    private void requiereUpload( Context context )
    {
        if( new DatabaseHelper(context).existsRegisterForUpdate() )
        {
            number_max_update = new DatabaseHelper( context ).getCountForUpdate();
            //Notification progress
            if( !is_update_server )
            {
                builderNotification = startNotificationUpload(context);
                is_update_server = true;
                updateRegister(context);
            }
        }
    }

    private NotificationCompat.Builder startNotificationUpload(Context context)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString( R.string.channel_id_default ) )
                .setSmallIcon( R.mipmap.ic_launcher )
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle( context.getString( R.string.txt_title_update_server ) )
                .setContentText( context.getString( R.string.text_content_small_update_server ) )
                .setStyle( new NotificationCompat.BigTextStyle().bigText( context.getString( R.string.text_content_update_server ) ) )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .setProgress(number_max_update, 0, false);

        manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());

        return builder;
    }

    private void updateProgress( int value )
    {
        number_current_updated += value;
        builderNotification.setProgress(number_max_update, number_current_updated, false);
        manager.notify(NOTIFICATION_ID, builderNotification.build());
    }

    public void finishProgress(Context context)
    {
        PendingIntent close = PendingIntent.getActivity(context, 0, new Intent(), 0);
        String msg = context.getString( R.string.content_finshed_update_server ).replace("{number}", ""+number_max_update );
        builderNotification.setContentText( context.getString( R.string.content_small_finshed_update_server ))
                .setStyle( new NotificationCompat.BigTextStyle().bigText( msg ) )
                .setProgress(0, 0, false)
                .setAutoCancel(true)
                .setContentIntent( close );

        manager.notify(NOTIFICATION_ID, builderNotification.build());
        number_max_update = 0;
        number_current_updated = 0;
        is_update_server = false;
    }

    /**
     * Procesa la solicitud para actualizar registro por registro.
     * Su llamada será recursiva, su funcionamiento depende de ir eliminando registros procesados
     * 1- Consulta los registros para actualizar de Inspeccion, MvtoKilometros, Inicio/Fin Ruta, ChecksIn/Out, MvtoRastreo
     * 2- Se procesan datos en bloque
     * @param context
     */
    private void updateRegister( Context context )
    {
        if( number_current_updated == number_max_update )
        {
            finishProgress(context);
        }else
        {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            //Iniciar actualización al servidor
            List<Inspeccion> inspeccions = databaseHelper.getInspeccionesForUpdate();
            if( !inspeccions.isEmpty() )
            {
                String URL_REGISTRAR_INSPECCION = context.getString(R.string.url_server_backend) + "registrar_inspeccion_offline.jsp";
                new RegistrarInspeccionTask(inspeccions, context)
                        .execute(URL_REGISTRAR_INSPECCION);
            }else {
                //Consultar inicios de ruta
                List<MvtoInicioRuta> mvtoInicioRutas = databaseHelper.getMvtoIniciosRutaForUpdate();
                if (!mvtoInicioRutas.isEmpty()) {
                    //Obtener el primer registro a actualizar
                    MvtoInicioRuta mvtoInicioRuta = mvtoInicioRutas.get(0);
                    //Iniciar actualización al servidor
                    String URL_INICIO_RUTA = context.getString(R.string.url_server_backend) + "registro_inicio_ruta_offline.jsp";
                    new IniciarRutaTask(mvtoInicioRuta, context).execute(URL_INICIO_RUTA);
                } else {
                    //Consultar Mvtos Checks
                    List<MvtoCheck> mvtoChecks = databaseHelper.getMvtoChecksForUpdate();
                    if (!mvtoChecks.isEmpty()) {
                        //Obtener el primer registro a actualizar
                        MvtoCheck mvtoCheck = mvtoChecks.get(0);
                        //Iniciar actualización servidor
                        String URL_CHECK = context.getString(R.string.url_server_backend) + "registro_checkin_offline.jsp";
                        new RegistrarCheckTask(mvtoCheck, context).execute(URL_CHECK);
                    } else {
                        //Consultar Mvtos Rastreo
                        List<MvtoRastreo> mvtoRastreos = databaseHelper.getMvtoRastreoForUpdate();
                        if (!mvtoRastreos.isEmpty()) {
                            //Obtener el primer registro a actualizar
                            MvtoRastreo mvtoRastreo = mvtoRastreos.get(0);
                            //Iniciar actualización al servidor
                            String URL_REGISTRO_RASTREO = context.getString(R.string.url_server_backend) + "registro_rastreo_offline.jsp";
                            new RegistrarMvtoRuta(mvtoRastreo, context).execute(URL_REGISTRO_RASTREO);
                        } else {
                            //Consultar Mvtos Fin de rutas
                            List<MvtoFinRuta> mvtoFinRutas = databaseHelper.getMvtoFinalesRutaForUpdate();
                            if (!mvtoFinRutas.isEmpty()) {
                                //Obtener el primer registro a actualizar
                                MvtoFinRuta mvtoFinRuta = mvtoFinRutas.get(0);
                                //Iniciar actualización al servidor
                                String URL_FIN_RUTA = context.getString(R.string.url_server_backend) + "registro_fin_ruta_offline.jsp";
                                new FinalizarRutaTask(mvtoFinRuta, context).execute(URL_FIN_RUTA);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Clase encargada de realizar el proceso de registrar la inspección previa en la BDD
     * */
    private class RegistrarInspeccionTask extends AsyncTask<String, Void, JSONObject>
    {
        HashMap<String,String> postParam = new HashMap<>();
        List<Inspeccion> inspecciones = null;
        Context context = null;

        public RegistrarInspeccionTask( List<Inspeccion> inspecciones, Context context)
        {
            this.inspecciones = inspecciones;
            this.context = context;

            JSONArray array = new JSONArray();
            JSONObject tmp = new JSONObject();
            String fecha = null;
            String id_fdv = null;
            for( Inspeccion inspeccion : inspecciones )
            {
                try {
                    tmp.put("id", "" + inspeccion.getId_tipo_inspeccion());
                    tmp.put("inspeccion", "" + inspeccion.getResultado());
                    array.put(tmp);
                    fecha = inspeccion.getFecha();
                    id_fdv = inspeccion.getId_fdv();
                } catch (JSONException e) {
                    Log.e("NetworkBR", "InspeccionToJSONArray.JSONException:" + e.toString());
                }
            }
            postParam.put("fecha", fecha);
            postParam.put("id_fdv", id_fdv);
            postParam.put("inspeccion", array.toString());
        }

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
                    if( estado.equals("OK") )
                    {
                        //Borrar inspecciones
                        for( Inspeccion inspeccion : inspecciones )
                        {
                            new DatabaseHelper( context ).deleteInspecciones( inspeccion.getId() );
                        }
                        //Cambiar contador
                        int count = inspecciones.size() + 1; //Inspecciones + Mvto Kilometro
                        updateProgress( count );
                        //Continuar update
                        updateRegister( context );
                    }else{
                        //Error en la actualización
                        Log.e("NetworkBR", "RegistrarInspeccionTask ERROR");
                    }
                }else
                {
                    //Error en la actualización
                    Log.e("NetworkBR", "RegistrarInspeccionTask ERROR Consulta externa");
                }
            }catch ( JSONException e )
            {
                Log.e("NetworkBR-JSONException", "RegistrarInspeccionTask"+ e.toString());
            }
        }
    }//RegistrarInspeccionTask

    /**
     * Clase encargada de realizar el proceso de registrar el inicio de la ruta en la BD
     * */
    private class IniciarRutaTask extends AsyncTask<String, Void, JSONObject>
    {
        HashMap<String,String> postParam = new HashMap<>();
        MvtoInicioRuta mvtoInicioRuta = null;
        Context context = null;

        public IniciarRutaTask(MvtoInicioRuta mvtoInicioRuta, Context context)
        {
            this.mvtoInicioRuta = mvtoInicioRuta;
            this.context = context;
            postParam.put("lat", "" + mvtoInicioRuta.getLat());
            postParam.put("lng", "" + mvtoInicioRuta.getLng());
            postParam.put("fecha", mvtoInicioRuta.getFecha() );
            postParam.put("id_ruta", mvtoInicioRuta.getId_ruta() );
        }

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
                    if( estado.equals("OK") )
                    {
                        //Borrar Mvto Inicio Ruta
                        new DatabaseHelper( context ).deleteMvoInicioRuta( mvtoInicioRuta.getId() );
                        //Cambiar contador
                        updateProgress( 1 );
                        //Continuar update
                        updateRegister( context );
                    }else{
                        //Error en la actualización
                        Log.e("NetworkBR", "IniciarRutaTask ERROR");
                    }
                }else
                {
                    //Error en la actualización
                    Log.e("NetworkBR", "IniciarRutaTask ERROR Consulta externa");
                }
            }catch ( JSONException e )
            {
                Log.e("NetworkBR-JSONException", "IniciarRutaTask: "+ e.toString());
            }
        }
    }//IniciarRutaTask

    /**
     * Clase encargada de realizar el proceso de registrar el inicio de la ruta en la BD
     * */
    public class RegistrarCheckTask extends AsyncTask<String, Void, JSONObject>
    {
        HashMap<String,String> postParam = new HashMap<>();
        MvtoCheck mvtoCheck = null;
        Context context = null;

        public RegistrarCheckTask(MvtoCheck mvtoCheck, Context context) {
            this.mvtoCheck = mvtoCheck;
            this.context = context;
            postParam.put("lat", "" + mvtoCheck.getLat());
            postParam.put("lng", "" + mvtoCheck.getLng());
            postParam.put("id_fdv", mvtoCheck.getId_fdv() );
            postParam.put("id_pdv", mvtoCheck.getId_pdv() );
            postParam.put("tipo_checkin", ""+ mvtoCheck.getTipo_checkin());
            postParam.put("fecha", mvtoCheck.getFecha());
        }

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
                    if( estado.equals("OK") )
                    {
                        //Borrar Mvto Check
                        new DatabaseHelper( context ).deleteMvoCheck( mvtoCheck.getId() );
                        //Cambiar contador
                        updateProgress( 1 );
                        //Continuar update
                        updateRegister( context );
                    }else{
                        //Error en la actualización
                        Log.e("NetworkBR", "RegistrarCheckTask ERROR");
                    }
                }else
                {
                    //Error en la actualización
                    Log.e("NetworkBR", "RegistrarCheckTask ERROR Consulta externa");
                }
            }catch ( JSONException e )
            {
                Log.e("NetworkBR-JSONException", "RegistrarCheckTask: "+ e.toString());

            }
        }
    }//RegistrarCheckTask

    /**
     * Clase encargada de realizar el proceso de registrar el movimiento de la ruta en la BD,
     * Se utiliza para registrar tanto el movimiento de la ruta, como la llegada al colegio, ya que
     * que los parametros son los mismo
     * */
    private class RegistrarMvtoRuta extends AsyncTask<String, Void, JSONObject> {
        HashMap<String,String> postParam = new HashMap();
        MvtoRastreo mvtoRastreo = null;
        Context context = null;

        public RegistrarMvtoRuta(MvtoRastreo mvtoRastreo, Context context)
        {
            this.mvtoRastreo = mvtoRastreo;
            this.context = context;
            postParam.put("lat", "" + mvtoRastreo.getLat());
            postParam.put("lng", "" + mvtoRastreo.getLng());
            postParam.put("fecha", mvtoRastreo.getFecha());
            postParam.put("id_ruta", mvtoRastreo.getId_ruta());
        }

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
                if (estado_ce.equals("OK"))
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Borrar Mvto Check
                        new DatabaseHelper( context ).deleteMvoRastreo( mvtoRastreo.getId() );
                        //Cambiar contador
                        updateProgress( 1 );
                        //Continuar update
                        updateRegister( context );
                    }else{
                        //Error en la actualización
                        Log.e("NetworkBR", "RegistrarMvtoRuta ERROR");
                    }
                }else{
                    //Error en la actualización
                    Log.e("NetworkBR", "RegistrarMvtoRuta ERROR Consulta externa");
                }
            }catch (JSONException e) {
                Log.e("NetworkBR-JSONException", "RegistrarMvtoRuta: "+ e.toString());
            }
        }
    }//RegistrarMvtoRuta

    /**
     * Clase encargada de realizar el proceso de registrar el final de la ruta en la BD
     * */
    private class FinalizarRutaTask extends AsyncTask<String, Void, JSONObject>
    {
        HashMap<String,String> postParam = new HashMap<>();
        MvtoFinRuta mvtoFinRuta = null;
        Context context = null;

        public FinalizarRutaTask(MvtoFinRuta mvtoFinRuta, Context context)
        {
            this.mvtoFinRuta = mvtoFinRuta;
            this.context = context;
            postParam.put("lat", "" + mvtoFinRuta.getLat());
            postParam.put("lng", "" + mvtoFinRuta.getLng());
            postParam.put("fecha", mvtoFinRuta.getFecha());
            postParam.put("id_ruta", mvtoFinRuta.getId_ruta());
        }

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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Borrar Mvto Fin ruta
                        new DatabaseHelper( context ).deleteMvoFinRuta( mvtoFinRuta.getId() );
                        //Cambiar contador
                        updateProgress( 1 );
                        //Continuar update
                        updateRegister( context );
                    }else{
                        //Error en la actualización
                        Log.e("NetworkBR", "FinalizarRutaTask ERROR");
                    }
                }else
                {
                    //Error en la actualización
                    Log.e("NetworkBR", "FinalizarRutaTask ERROR Consulta externa");
                }
            }catch ( JSONException e )
            {
                Log.e("NetworkBR-JSONException", "FinalizarRutaTask: "+ e.toString());
            }
        }
    }//FinalizarRutaTask
    //--------------Procesar upload datos de bbdd local-------------------

}
