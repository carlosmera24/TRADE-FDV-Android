package co.com.puli.trade.fdv.SharedPrefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeviceTokenNav {
    private static DeviceTokenNav token_nav;

    public static String KEY_TOKENNAV= "device_token";

    public static SharedPreferences getDefaultPref(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void setDeviceToken(Context ctx, String device_token_id)
    {
     	 SharedPreferences.Editor edit = getDefaultPref(ctx).edit();
     	 edit.putString(KEY_TOKENNAV, device_token_id);
     	 edit.commit();
      }
    public static String getStringData(Context ctx, String key)
    {
    	return getDefaultPref(ctx).getString(key,"");
    }

}