package co.com.puli.trade.fdv.clases;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.actividades.ChatActivity;
import co.com.puli.trade.fdv.actividades.LoginActivity;

/**
 * Clase encargada de procesar las notificaciones
 * Nombre definido en AndroidManifest.xml en el tag service
 * Created by carlos on 14/12/15.
 */
public class FcmIntentService extends FirebaseMessagingService
{
    private final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        mostrarNotificacion( remoteMessage.getData(), remoteMessage.getNotification() );
    }

    /**
     * Método encargado de procesar la notificación
     * @param data JSONObject con los datos del mensaje en formato FCM
     */
    private void mostrarNotificacion(Map<String,String> data, RemoteMessage.Notification notif)
    {
        if( notif == null )
        {
            Log.e("FcmError", "FcmIntenService.momstrarNotificacion.Error: notificación null");
            return;
        }

        //Configurar y preparar el comportamiento al tocar la notificación
        Class lauch_activity = LoginActivity.class; //Actividad por defecto
        Bundle param_extras = new Bundle();
        int flag_pendig_intent = PendingIntent.FLAG_CANCEL_CURRENT; //Tipo de Flag para la actividad de apertura

        //Preparar la actividad a abrir y los parámetros necesarios a enviar
        if( !data.isEmpty() && data.get("opc_avtividad") != null )
        {
            switch( data.get("opc_avtividad") )
            {
                case "chat":
                    if( data.get("id_origen") != null && data.get("id_destino") !=  null && data.get("nombre_usuario_destino") != null)
                    {
                        //Consultar id_usuario del App
                        String id_user = getIdUserApp();
                        if (!id_user.equals("")) //Definir usuario destino/origen para el App segun usuario
                        {
                            if ( id_user.equals( data.get("id_origen") ) )
                            {
                                param_extras.putString("id_usuario", data.get("id_origen") );
                                param_extras.putString("id_destino", data.get("id_destino"));
                            } else {
                                param_extras.putString("id_usuario", data.get("id_destino"));
                                param_extras.putString("id_destino", data.get("id_origen"));
                            }
                        } else //Definir usuarios por defecto
                        {
                            param_extras.putString("id_usuario", data.get("id_origen"));
                            param_extras.putString("id_destino", data.get("id_destino"));
                        }
                        param_extras.putString("usuario_chat",data.get("nombre_usuario_destino"));
                        lauch_activity = ChatActivity.class;
                        flag_pendig_intent = PendingIntent.FLAG_UPDATE_CURRENT;
                    }
                    break;
            }
        }

        //Preparar el intent para la actvidad a abrir
        Intent intent = new Intent(this, lauch_activity);
        intent.putExtras(param_extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flag_pendig_intent);

        //Configurar la notificación con el canal por defecto a utilizar, el cual debe ser configurado para android O y superior en el inicio del App (SplashActiviy)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,getString( R.string.channel_id_default ) );
        mBuilder.setSmallIcon(R.drawable.ic_notification_default);
        mBuilder.setColor( getResources().getColor( R.color.color_notification ) );
        mBuilder.setContentTitle(notif.getTitle());
        mBuilder.setContentText(notif.getBody());
        mBuilder.setAutoCancel(true);
        mBuilder.setStyle( new NotificationCompat.BigTextStyle().bigText(notif.getBody()) ); //Texto Grande, multiples lineas
        mBuilder.setLights( getResources().getColor( R.color.color_notification ), 3000, 3000); //Led
        mBuilder.setVibrate(new long[]{100, 200, 100, 200, 100, 200}); //Vibrar, long[] representa pausa,vibrar
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); //Sonido, Notificación por defecto
        //Definir el inten que será abierto cuando el usuario haga tap sobre la notificación
        mBuilder.setContentIntent( pendingIntent );

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
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
