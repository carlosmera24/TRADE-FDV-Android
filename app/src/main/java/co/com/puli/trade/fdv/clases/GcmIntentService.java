package co.com.puli.trade.fdv.clases;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.actividades.ChatActivity;
import co.com.puli.trade.fdv.actividades.LoginActivity;

/**
 * Clase encargada de procesar las notificaciones
 * Nombre definido en AndroidManifest.xml en el tag service
 * Created by carlos on 14/12/15.
 */
public class GcmIntentService extends IntentService
{
    private final int NOTIFICATION_ID = 1;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.     *
     */
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String mensajeType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if( !extras.isEmpty() )
        {
            if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(mensajeType))
            {
                mostrarNotificacion(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Método encargado de visualizar la notificación
     * @param extras Bundle con todos los parametros enviados en la notificación
     * */
    private void mostrarNotificacion(Bundle extras)
    {
        String titulo = extras.getString("titulo"); //Titulo para la notificación
        String msg = extras.getString("mensaje"); //Mensaje para la notificación
        String opc_actividad = extras.getString("actividad"); //Actividad a ejecutar
        Class lauch_activity = LoginActivity.class;
        Bundle param_extras = new Bundle();
        int flag_pendint_intent = PendingIntent.FLAG_CANCEL_CURRENT; //Tipo de Flag para la actividad de apertura

        //Preparar los parametros a enviarse en la actividad de apertura
        if (opc_actividad != null)
        {
            switch (opc_actividad) {
                case "chat":
                    //Consultar id_usuario del App
                    String id_user = getIdUserApp();
                    if( !id_user.equals("") ) //Definir usuario destino/origen para el App segun usuario
                    {
                        if( id_user.equals(extras.getString("id_origen")) )
                        {
                            param_extras.putString("id_usuario", extras.getString("id_origen"));
                            param_extras.putString("id_destino", extras.getString("id_destino"));
                        }else{
                            param_extras.putString("id_usuario", extras.getString("id_destino"));
                            param_extras.putString("id_destino", extras.getString("id_origen"));
                        }
                    }else //Definir usuarios por defecto
                    {
                        param_extras.putString("id_usuario", extras.getString("id_origen"));
                        param_extras.putString("id_destino", extras.getString("id_destino"));
                    }
                    param_extras.putString("usuario_chat", extras.getString("nombre_usuario_destino"));
                    lauch_activity = ChatActivity.class;
                    flag_pendint_intent = PendingIntent.FLAG_UPDATE_CURRENT;
                    break;
            }
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_notification_default);
        mBuilder.setContentTitle(titulo);
        mBuilder.setContentText(msg);
        mBuilder.setAutoCancel(true);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)); //Texto Grande, multiples lineas
        mBuilder.setLights(Color.RED, 3000, 3000); //Led
        mBuilder.setVibrate(new long[]{100, 200, 100, 200, 100, 200}); //Vibrar, long[] representa pausa,vibrar
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); //Sonido, Notificación por defecto

        Intent intent = new Intent(this, lauch_activity);
        intent.putExtras(param_extras);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, flag_pendint_intent);

        mBuilder.setContentIntent(pendIntent);

        manager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Método encargado de retornar el Id del usuario del App a partir de SharedPreferences
     * @return id_usuario, vacío si no se encontro datos
     * */
    private String getIdUserApp()
    {
        String KEY_ID_USER = "app_id_user";
        SharedPreferences sharedPref = getSharedPreferences( getResources().getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_ID_USER, "");
    }
}
