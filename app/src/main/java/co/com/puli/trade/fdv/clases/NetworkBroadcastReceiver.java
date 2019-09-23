package co.com.puli.trade.fdv.clases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * Clase encargada de controlar los cambios en la conexión de Internet
 * Especialmente el reinicio de la conexión
 * Created by carlos on 23/06/17.
 */

public class NetworkBroadcastReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Verificar si la conexión está disponible
        boolean conectado = new Utilidades().redDisponible( ( (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE ) ) );
        //Enviar estado al GPSServices
        Intent intentGPS = new Intent(context, GPSServices.class);
        intentGPS.putExtra("network_connect", conectado );
        context.startService( intentGPS );
    }
}
