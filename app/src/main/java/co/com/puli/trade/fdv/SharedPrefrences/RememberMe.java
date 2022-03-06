package co.com.puli.trade.fdv.SharedPrefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RememberMe
{
    private static RememberMe dynaminNam;
    public static String KEY_IS_LOGIN = "is_login";
    public static String KEY_EMAIL_ID = "email_id";
    public static String KEY_PASSWORD = "password";


    public static SharedPreferences getDefaultPref(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
    public static void saveLoginCredientials(Context ctx, boolean isLogined, String emailId, String password)
    {
        SharedPreferences.Editor edit = getDefaultPref(ctx).edit();
        edit.putBoolean(KEY_IS_LOGIN, isLogined);
        edit.putString(KEY_EMAIL_ID, emailId);
        edit.putString(KEY_PASSWORD, password);
        edit.commit();
    }
    public static String getStringData(Context ctx, String key)
    {
    	return getDefaultPref(ctx).getString(key,"");
    }
    public static boolean getBooleanData(Context ctx, String key)
    {
    	return getDefaultPref(ctx).getBoolean(key,false);
    }

}
