package co.com.puli.trade.fdv.clases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent myService = new Intent(context, GPSServices.class);
        context.startService(myService);
    }
}
