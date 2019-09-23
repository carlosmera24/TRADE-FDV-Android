package co.com.puli.trade.fdv.clases;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Clase encargada de recibir las notificaciones
 * Su nombre corresponde al definido en AndroidManifest.xml para el tag receiver
 * Created by carlos on 14/12/15.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        ComponentName cn = new ComponentName( context.getPackageName(), GcmIntentService.class.getName() );
        startWakefulService( context, intent.setComponent( cn ) );
        setResultCode(Activity.RESULT_OK);

    }
}
