package co.com.puli.trade.fdv.actividades;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class LoginActivity extends AppCompatActivity
{
    private LinearLayout content_layout;
    private ImageView ivLogo, ivFooter;
    private String URL_LOGIN, URL_REGISTRO_TOKEN, URL_PARAMETROS_GENERALES, URL_RUTAGRAMAS, URL_FECHA_INSPECCION, URL_DATOS_LOGIN;
    private final String KEY_REG_ID = "registration_id";
    private final String KEY_APP_VERSION = "app_version";
    private final String KEY_USER = "app_user";
    private final String KEY_PASS = "app_pass";
    private final String KEY_ID_USER = "app_id_user";
    private HashMap<String,String> postParam;
    private EditText etUser, etPass;
    private SharedPreferences sharedPref;
    private final String SENDER_ID = "997534468509 "; //Numero del proyecto en Google Console

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);


        URL_LOGIN = getString( R.string.url_server_backend ) + "validar_usuario.jsp";
        URL_REGISTRO_TOKEN = getString( R.string.url_server_backend ) + "registrar_token_dispositivo.jsp";
        URL_PARAMETROS_GENERALES = getString( R.string.url_server_backend ) + "consultar_parametros_generales.jsp";
        URL_RUTAGRAMAS = getString( R.string.url_server_backend ) + "consultar_rutagrama.jsp";
        URL_FECHA_INSPECCION= getString( R.string.url_server_backend ) + "consultar_fecha_inspeccion.jsp";
        URL_DATOS_LOGIN= getString( R.string.url_server_backend ) + "registrar_datos_login.jsp";

        etUser = findViewById( R.id.etUsuario );
        etPass = findViewById( R.id.etPassword );

        CustomFonts fuentes = new CustomFonts( getAssets() );

        content_layout = findViewById(R.id.content_layout_center);

        ivLogo = findViewById(R.id.imageViewLogo);

        ivFooter = findViewById(R.id.imageViewFooter);

        etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    //Ocultar teclado
                    new Utilidades().ocultarTeclado( LoginActivity.this, etPass );
                    //Iniciar login
                    iniciarLogin();
                    return true;
                }
                return false;
            }
        });


        Button btIngreso = findViewById(R.id.btIngreso);
        btIngreso.setTypeface(fuentes.getBoldFont());
        btIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarLogin();
            }
        });

        Button btRecordar = findViewById(R.id.btRecordarPas);
        btRecordar.setTypeface( fuentes.getRobotoThinFont() );

        Button btCrear = findViewById( R.id.btCrearUsuario );
        btCrear.setTypeface( fuentes.getRobotoThinFont() );
        btCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Abrir actividad para creación de usuario (Registro)
                Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Método encargado de validar si hay datos de login del usuario y lanzar el logueo automatico
     * */
    public void autoLogin()
    {
        String user = sharedPref.getString(KEY_USER, "");
        String pass = sharedPref.getString(KEY_PASS, "");

        if( !user.equals("") && !pass.equals(""))
        {
            etUser.setText( user );
            etPass.setText( pass );
            iniciarLogin();
        }
    }

    /**
     * Método encargado de validar y ejecutar el proceso de login
     * */
    public void iniciarLogin()
    {
        //Ocultar teclado
        new Utilidades().ocultarTeclado( LoginActivity.this, etPass);
        //Verificar disponiblidad de Internet
        if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            if( TextUtils.isEmpty( etUser.getText() ) )
            {
                etUser.setError(getString(R.string.txt_campo_requerido));
            }else if( TextUtils.isEmpty( etPass.getText() ) )
            {
                etPass.setError( getString( R.string.txt_campo_requerido ) );
            }else{
                postParam = new HashMap<String,String>();
                postParam.put("usuario", etUser.getText().toString());
                postParam.put( "password", etPass.getText().toString() );
                postParam.put( "perfil", "7" ); //FDV
                ValidarInicioSessionTask vis = new ValidarInicioSessionTask();
                vis.execute(URL_LOGIN);
            }
        }else{
            new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
        }

    }

    /**
     * Método encargado de validar e iniciar el proceso de actualización del token del dispositivo
     *  @param datos JSONObject con los datos requeridos del usuario
     * */
    public void registrarTokenDispotivo( JSONObject datos )
    {
        //Validar si el servicio Google Play Services está disponible
        if( new Utilidades().googlePlayServicesDisponible(this, getString(R.string.txt_titulo_error_google_play_services), getString(R.string.txt_msg_error_google_play_services)) )
        {
            try {
                String idReg = getRegistrationID();
                //Si el idReg es igual a null, el token en la BD del usuario es null, o el token y el idReg son diferentes
                if (idReg == null || datos.getString("token").equals("NULL") || !datos.getString("token").equals(idReg) ) //Actualizar o solicitar registro ID
                {
                    //Procesar la obtención del token del dispositivo generado desde el servidor FCM
                    procesarGetTokenFCM( datos );
                } else {
                    consultarParametrosGenerales(datos);
                }
            }catch (JSONException e)
            {
                Log.e("JSONException","LoginActivity.registrarTokenDispotivo:"+ e.toString() );
            }
        }else{
            consultarParametrosGenerales(datos);
        }
    }

    /**
     * Método encargado de procesar el registro del App en el servidor FCM y obtener el token del dispositivo,
     * Intenta obtener el TOKEN del dispositivo, si es correcto se envía el token para registrarse en el servidor BackEnd,
     * De lo contrario (Falla) se continua con el App informando el error.
     * @param datos JSONObject con los datos requeridos del usuario
     */
    public void procesarGetTokenFCM(final JSONObject datos )
    {
        //Obtener el token de FCM
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if( !task.isSuccessful() )
                {
                    new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Error", getString(R.string.txt_msg_error_token_fcm), true);
                    consultarParametrosGenerales( datos );
                    return;
                }

                //get Token
                String token = task.getResult().getToken();
                try {
                    //Actualizar registro del token en la BD del servidro WEB
                    RegistrarTokenBDTask rtbd = new RegistrarTokenBDTask( datos );
                    postParam.put("id_usuario", datos.getString("id_usuario") );
                    postParam.put("token", token );
                    rtbd.execute( URL_REGISTRO_TOKEN );
                }catch(JSONException e)
                {
                    Log.e("JSONException", "LoginActivity.procesarGetTokenFCM.onSuccess" + e.toString() );
                }
            }
        });
    }

    /**
     * Método encargado de retornar el Registratrion ID a partir del registro en SharedPreferences
     * @return registrationID Si no hay registro en SharedPreferences retorna NULL o si la versión del App ha cambiado
     * */
    public String getRegistrationID()
    {
        String registrationID = sharedPref.getString(KEY_REG_ID, null);

        if( registrationID == null ) //NO hay dados registrados
        {
            return null;
        }else
        {
            //Verificar version del app
            int version = sharedPref.getInt( KEY_APP_VERSION, Integer.MIN_VALUE );
            int currentVersion = getAppVersion();

            if( version != currentVersion )
            {
                return null;
            }else{
                return registrationID;
            }
        }
    }

    /**Método encargado de retornar la versión del App desde PackageManager
     * @return int version del app*/
    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo( getPackageName(), 0);
            return packageInfo.versionCode;
        }catch(Exception e)
        {
            Log.e("Exception", "LoginActivity.getAppVersion.Exception:"+e.toString());
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Método encargado de procesar la consulta de los parámetros generales
     * @param datos JSONObject con los datos requeridos del usuario
     * */
    public void consultarParametrosGenerales( JSONObject datos )
    {
        ConsultarParametrosGeneralesTask cpgt = new ConsultarParametrosGeneralesTask(datos);
        cpgt.execute( URL_PARAMETROS_GENERALES );
    }

    /**
     * Método encargado de procesar el iicio de la actividad principal
     * @param datos JSONObject con los datos requeridos del usuario
     * */
    @SuppressLint("WrongConstant")
    public void iniciarActividadPrincipal(JSONObject datos )
    {
        try
        {
            Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
            intent.putExtra("id_usuario", datos.getString("id_usuario") );
            intent.putExtra("id_perfil", datos.getString("id_perfil") );
            intent.putExtra("usuario", datos.getString("usuario") );
            intent.putExtra("id_fdv", datos.getString("id_fdv") );
            intent.putExtra("id_ruta", datos.getString("id_ruta") );
            intent.putExtra("id_vehiculo", datos.getString("id_vehiculo") );
            intent.putExtra("nombre_usuario", datos.getString("nombre_usuario") );
            intent.putExtra("imagen", datos.getString("imagen") );
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 11) {
                intent.addFlags(0x8000);
            }
            startActivity(intent);
        }catch (JSONException e)
        {
            Log.e("JSONException", "LoginActivity.iniciarActividadPrincipal:"+ e.toString() );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        ImageBitMap image = new ImageBitMap();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Bitmap img = image.decodificarImagen(getResources(), R.drawable.background_route_gray, displaymetrics.widthPixels, 0);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
        {
            content_layout.setBackground( new BitmapDrawable(getResources(),img) );
        }else {
            content_layout.setBackgroundDrawable(new BitmapDrawable(getResources(), img));
        }

        img = image.decodificarImagen(getResources(), R.drawable.logo_route_white, displaymetrics.widthPixels, 0);
        ivLogo.setImageBitmap(img);

//        img = image.decodificarImagen( getResources(), R.drawable.image_bottom, displaymetrics.widthPixels, 0 );
//        ivFooter.setImageBitmap(img);

        autoLogin();
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

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     * */
    public void limpiarMemoria()
    {
        content_layout.setBackgroundResource(0);
        ivLogo.setImageBitmap(null);
//        ivFooter.setImageBitmap(null);
        System.gc();
    }

    /**
     * Clase encargada de procesar el inicio de sesión*/
    class ValidarInicioSessionTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_validar_acceso), true);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {

                SharedPreferences.Editor editor = sharedPref.edit();
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado )
                    {
                        case "OK":
                            //Verificar estado del usuario
                            String user_status = result.getString("status_usuario");
                            if( user_status.equals("3") )//Usuario nuevo, requiere modificación de contraseña
                            {
                                progreso.cancel();
                                //Abrir actividad para cambio de contraseña
                                Intent intent = new Intent(LoginActivity.this, CambiarPasswordActivity.class);
                                intent.putExtra("id_usuario", result.getString("id_usuario"));
                                startActivity(intent);
                            }else
                            {
                                //Datos necesarios para enviar como parámetros a otros métodos
                                JSONObject datos = new JSONObject();
                                datos.put("id_usuario", result.getString("id_usuario"));
                                datos.put("id_perfil", result.getString("id_perfil"));
                                datos.put("usuario", result.getString("usuario"));
                                datos.put("id_fdv", result.getString("id_fdv"));
                                datos.put("id_vehiculo", result.getString("id_vehiculo"));
                                datos.put("id_ruta", result.getString("id_ruta"));
                                datos.put("nombre_usuario", result.getString("nombre") + " " + result.getString("apellido") );
                                datos.put("token", result.getString("token"));
                                datos.put("imagen", result.getString("imagen"));
                                datos.put("empresa", result.getString("empresa"));

                                //Guardar datos de login del usuario
                                editor.putString(KEY_USER, postParam.get("usuario"));
                                editor.putString(KEY_PASS, postParam.get("password"));
                                editor.putString(KEY_ID_USER, result.getString("id_usuario"));
                                editor.apply();

                                //Procesar registro del token
                                progreso.cancel();
                                registrarTokenDispotivo( datos );
                            }
                            break;
                        case "ERROR":
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            progreso.cancel();
                            break;
                        default: // EMPTY | NOT
                            new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Error inicio sesión", getString(R.string.txt_msg_error_login), true);
                            //Guardar datos de login del usuario en blanco
                            editor.putString( KEY_USER, "" );
                            editor.putString( KEY_PASS, "" );
                            editor.commit();
                            progreso.cancel();
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Error inicio sesión", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Error inicio sesión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException","LoginActivity.ValidarInicioSessionTask.onPostExecute:"+e.toString());
            }catch (Exception e )
            {
                Log.e("Exception", "LoginActivity.ValidarInicioSessionTask.onPostExecute: " + e.toString());
                String fch = sdf.format( Calendar.getInstance().getTime() );
                new Utilidades().mostrarSimpleMensaje(LoginActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                progreso.cancel();
            }
        }
    }//ValidarInicioSessionTask

    /**
     * Clase encargada de procesar el registro del token en la BD
     * */
    class RegistrarTokenBDTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;
        JSONObject datos;

        public RegistrarTokenBDTask( JSONObject datos)
        {
            this.datos = datos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_msg_registro_token_bd), true);
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
                        //Guardar token en SharedPreferences
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString( KEY_REG_ID, postParam.get("token") );
                        editor.putInt( KEY_APP_VERSION, getAppVersion() );
                        editor.apply();
                        //Iniciar la consulta de los parámtros generales
                        progreso.cancel();
                        consultarParametrosGenerales( datos );
                    }else
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Registro token",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_registro_token_bd),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            RegistrarTokenBDTask rtbd = new RegistrarTokenBDTask( datos );
                                                                            //Los parametros deben estar en memoria en este punto
                                                                            rtbd.execute( URL_REGISTRO_TOKEN );
                                                                        }
                                                                    }
                                                            );
                        progreso.cancel();
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    progreso.cancel();
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Registro token",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            RegistrarTokenBDTask rtbd = new RegistrarTokenBDTask( datos );
                                                                            //Los parametros deben estar en memoria en este punto
                                                                            rtbd.execute( URL_REGISTRO_TOKEN );
                                                                        }
                                                                    }
                                                            );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Registro token",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_consulta),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            RegistrarTokenBDTask rtbd = new RegistrarTokenBDTask( datos );
                                                                            //Los parametros deben estar en memoria en este punto
                                                                            rtbd.execute( URL_REGISTRO_TOKEN );
                                                                        }
                                                                    }
                                                            );
                        Log.e("ErrorConsulta", "LoginActivity.RegistrarTokenBDTask.onPostExecute:" + result.getString("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException", "LoginActivity.RegistrarTokenBDTask.onPostExecute" + e.toString());
            }
        }
    }//RegistrarTokenBDTask

    /**
     * Clase encargada de procesar la consulta de los parámetros generales en la BD
     * */
    class ConsultarParametrosGeneralesTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;
        JSONObject datos;

        public ConsultarParametrosGeneralesTask( JSONObject datos)
        {
            this.datos = datos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_msg_consulta_parametros), true);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                progreso.cancel();

                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Registrar los parametros generales descargados
                        JSONArray param = result.getJSONArray("parametros");
                        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
                        parametros.setParametros( param );
                        //Consultar RutaGramas
                        ConsultarRutaGramasTask crgt = new ConsultarRutaGramasTask(datos, parametros);
                        crgt.execute( URL_RUTAGRAMAS );
                    }else if( estado.equals("ERROR") )
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Parámetros generales",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_consulta_parametros),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            consultarParametrosGenerales( datos );
                                                                        }
                                                                    }
                                                            );
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Parámetros generales",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            consultarParametrosGenerales( datos );
                                                                        }
                                                                    }
                                                            );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Parámetros generales",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_consulta),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            consultarParametrosGenerales( datos );
                                                                        }
                                                                    }
                                                            );
                        Log.e("ErrorConsulta", "LoginActivity.ConsultarParametrosGeneralesTask.onPostExecute: " + result.getString("code"));
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("JSONException", "LoginActivity.ConsultarParametrosGeneralesTask.onPostExecute: " + e.toString());
            }catch( Exception e )
            {
                Log.e("Exception", "LoginActivity.ConsultarParametrosGeneralesTask.onPostExecute: " + e.toString());
                String fch = sdf.format( Calendar.getInstance().getTime() );
                new Utilidades().mostrarMensajeBotonOK(
                        LoginActivity.this,
                        "Parámetros generales",
                        fch + "\n" + getString(R.string.txt_msg_error_consulta_parametros),
                        getString(R.string.txt_reintentar),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                consultarParametrosGenerales( datos );
                            }
                        }
                );
            }
        }
    }//ConsultarParametrosGeneralesTask

    /**
     * Clase encargada de procesar la consulta de los RutaGramas en la BD
     * */
    class ConsultarRutaGramasTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;
        JSONObject datos;
        GlobalParametrosGenerales parametros;

        public ConsultarRutaGramasTask( JSONObject datos, GlobalParametrosGenerales parametros)
        {
            this.datos = datos;
            this.parametros = parametros;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_msg_consulta_rutagramas), true);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                progreso.cancel();

                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Registrar los parametros generales descargados
                        JSONArray rutagramas = result.getJSONArray("rutagramas");
                        parametros.setRutaGramas( rutagramas );

                        //Guardar los datos de la empresa
                        JSONObject empresa = new JSONObject();
                        empresa.put("nombre", datos.getString("empresa") );
                        empresa.put("lat", 0.0 );
                        empresa.put("lng", 0.0 );
                        parametros.setEmpresa( empresa );

                        //Consultar fecha de inspección vehículo
                        postParam = new HashMap<>();
                        postParam.put("id_fdv", datos.getString("id_fdv") );
                        ConsultarFechaInspeccionTask cfit = new ConsultarFechaInspeccionTask(datos, parametros);
                        cfit.execute( URL_FECHA_INSPECCION );
                    }else if( estado.equals("ERROR") )
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Rutagramas",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_consulta_parametros),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            ConsultarRutaGramasTask crgt = new ConsultarRutaGramasTask(datos, parametros);
                                                                            crgt.execute( URL_RUTAGRAMAS );
                                                                        }
                                                                    }
                                                            );
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                        LoginActivity.this,
                                                                        "Rutagramas",
                                                                        fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion),
                                                                        getString(R.string.txt_reintentar),
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                ConsultarRutaGramasTask crgt = new ConsultarRutaGramasTask(datos, parametros);
                                                                                crgt.execute( URL_RUTAGRAMAS );
                                                                            }
                                                                        }
                                                                );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                        LoginActivity.this,
                                                                        "Rutagramas",
                                                                        fch + "\n" + getString(R.string.txt_msg_error_consulta),
                                                                        getString(R.string.txt_reintentar),
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                ConsultarRutaGramasTask crgt = new ConsultarRutaGramasTask(datos, parametros);
                                                                                crgt.execute( URL_RUTAGRAMAS );
                                                                            }
                                                                        }
                                                                );
                        Log.e("ErrorConsulta", "LoginActivity.ConsultarRutaGramasTask.onPostExecute: " + result.getString("code"));
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("JSONException", "LoginActivity.ConsultarRutaGramasTask.onPostExecute: " + e.toString());
            }catch (Exception e )
            {
                Log.e("Exception", "LoginActivity.ConsultarRutaGramasTask.onPostExecute: " + e.toString());
                String fch = sdf.format( Calendar.getInstance().getTime() );
                new Utilidades().mostrarMensajeBotonOK(
                        LoginActivity.this,
                        "Rutagramas",
                        fch + "\n" + getString(R.string.txt_msg_error_consulta),
                        getString(R.string.txt_reintentar),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConsultarRutaGramasTask crgt = new ConsultarRutaGramasTask(datos, parametros);
                                crgt.execute( URL_RUTAGRAMAS );
                            }
                        }
                );
            }
        }
    }//ConsultarRutaGramasTask


    /**
     * Clase encargada de procesar la consultar la última fecha de la inspección realizada para el vehículo
     * */
    class ConsultarFechaInspeccionTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;
        JSONObject datos;
        GlobalParametrosGenerales parametros;

        public ConsultarFechaInspeccionTask( JSONObject datos, GlobalParametrosGenerales parametros)
        {
            this.datos = datos;
            this.parametros = parametros;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_msg_consulta_fecha_inspeccion), true);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                //Cerrar el dialogo spinner
                progreso.cancel();

                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    PackageInfo packageInfo = getPackageManager().getPackageInfo( getPackageName(), 0);
                    RegistrarDatosLoginTask rdlt = new RegistrarDatosLoginTask( datos, parametros);
                    String strSO = String.format("Android %s %s", new Utilidades().getNombreVersionAndroid(), Build.VERSION.RELEASE );
                    switch(estado)
                    {
                        case "OK":
                            //Registrar los parametros generales descargados
                            parametros.setFecha_inspeccion( result.getString("fecha_date") );
                            //Registrar datos del login
                            postParam = new HashMap<>();
                            postParam.put("id_usuario", datos.getString("usuario") );
                            postParam.put("so", strSO );
                            postParam.put("vapp", packageInfo.versionName );
                            rdlt.execute( URL_DATOS_LOGIN );
                            break;
                        case "EMPTY":
                            //Registrar datos del login
                            postParam = new HashMap<>();
                            postParam.put("id_usuario", datos.getString("usuario") );
                            postParam.put("so", strSO );
                            postParam.put("vapp", packageInfo.versionName );
                            rdlt.execute( URL_DATOS_LOGIN );
                            break;
                        case "ERROR":
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarMensajeBotonOK(
                                                                        LoginActivity.this,
                                                                        "Inspección previa",
                                                                        fch + "\n" + getString(R.string.txt_msg_error_consulta_fecha_inspeccion_previa),
                                                                        getString(R.string.txt_reintentar),
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                ConsultarFechaInspeccionTask cfit = new ConsultarFechaInspeccionTask(datos, parametros);
                                                                                cfit.execute( URL_FECHA_INSPECCION );
                                                                            }
                                                                        }
                                                                );
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Inspección previa",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            ConsultarFechaInspeccionTask cfit = new ConsultarFechaInspeccionTask(datos, parametros);
                                                                            cfit.execute( URL_FECHA_INSPECCION );
                                                                        }
                                                                    }
                                                            );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                                                    LoginActivity.this,
                                                                    "Inspección previa",
                                                                    fch + "\n" + getString(R.string.txt_msg_error_consulta),
                                                                    getString(R.string.txt_reintentar),
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            ConsultarFechaInspeccionTask cfit = new ConsultarFechaInspeccionTask(datos, parametros);
                                                                            cfit.execute( URL_FECHA_INSPECCION );
                                                                        }
                                                                    }
                                                            );
                        Log.e("ErrorConsulta", "LoginActivity.ConsultarFechaInspeccionTask.onPostExecute: " + result.getString("code"));
                    }
                }
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("JSONException", "LoginActivity.ConsultarFechaInspeccionTask.onPostExecute: " + e.toString());
            }catch (Exception e )
            {
                Log.e("Exception", "LoginActivity.ConsultarFechaInspeccionTask.onPostExecute: " + e.toString());
                String fch = sdf.format( Calendar.getInstance().getTime() );
                new Utilidades().mostrarMensajeBotonOK(
                        LoginActivity.this,
                        "Inspección previa",
                        fch + "\n" + getString(R.string.txt_msg_error_consulta_fecha_inspeccion_previa),
                        getString(R.string.txt_reintentar),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConsultarFechaInspeccionTask cfit = new ConsultarFechaInspeccionTask(datos, parametros);
                                cfit.execute( URL_FECHA_INSPECCION );
                            }
                        }
                );
            }
        }
    }//ConsultarFechaInspeccionTask

    /**
     * Clase encargada de registrar datos del usario
     * */
    class RegistrarDatosLoginTask extends AsyncTask<String,Void,JSONObject>
    {
        ProgressDialog progreso;
        JSONObject datos;
        GlobalParametrosGenerales parametros;

        public RegistrarDatosLoginTask( JSONObject datos, GlobalParametrosGenerales parametros)
        {
            this.datos = datos;
            this.parametros = parametros;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(LoginActivity.this, null, getString( R.string.txt_msg_registrando_datos_login), true);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                progreso.cancel();

                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        //Iniciar actividad principal
                        iniciarActividadPrincipal( datos );
                    }else if( estado.equals("ERROR") )
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                LoginActivity.this,
                                "Registro datos login",
                                fch + "\n" + getString(R.string.txt_msg_error_registro_datos_login),
                                getString(R.string.txt_reintentar),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RegistrarDatosLoginTask cct = new RegistrarDatosLoginTask(datos, parametros);
                                        cct.execute( URL_DATOS_LOGIN );
                                    }
                                }
                        );
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                LoginActivity.this,
                                "Colegio",
                                fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion),
                                getString(R.string.txt_reintentar),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RegistrarDatosLoginTask cct = new RegistrarDatosLoginTask(datos, parametros);
                                        cct.execute( URL_DATOS_LOGIN );
                                    }
                                }
                        );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarMensajeBotonOK(
                                LoginActivity.this,
                                "Colegio",
                                fch + "\n" + getString(R.string.txt_msg_error_consulta),
                                getString(R.string.txt_reintentar),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RegistrarDatosLoginTask cct = new RegistrarDatosLoginTask(datos, parametros);
                                        cct.execute( URL_DATOS_LOGIN );
                                    }
                                }
                        );
                        Log.e("ErrorConsulta", "LoginActivity.ConsultarColegioTask.onPostExecute: " + result.getString("code"));
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("JSONException", "LoginActivity.ConsultarColegioTask.onPostExecute: " + e.toString());
            }catch (Exception e )
            {
                Log.e("Exception", "LoginActivity.ConsultarColegioTask.onPostExecute: " + e.toString());
                String fch = sdf.format( Calendar.getInstance().getTime() );
                new Utilidades().mostrarMensajeBotonOK(
                        LoginActivity.this,
                        "Colegio",
                        fch + "\n" + getString(R.string.txt_msg_error_consulta),
                        getString(R.string.txt_reintentar),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RegistrarDatosLoginTask cct = new RegistrarDatosLoginTask(datos, parametros);
                                cct.execute( URL_DATOS_LOGIN );
                            }
                        }
                );
            }
        }
    }//RegistrarDatosLoginTask
}
