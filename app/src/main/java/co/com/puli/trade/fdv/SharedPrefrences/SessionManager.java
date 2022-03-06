package co.com.puli.trade.fdv.SharedPrefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SessionManager
{

    private Context context;

    public static void SetSharedPrefrence(String KeyName, String KeyValue,Context context)
    {
            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString(KeyName,KeyValue);
            editor.commit();

    }

    public static String GetSharedPreference(String KeyName, Context context)
    {
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getString(KeyName,"");
    }

   /* public static void SaveArrayList(String KeyName,List<DashBoard_Model> KeyValue, Context context)
    {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(KeyValue);
        editor.putString(KeyName,json);
        editor.commit();

    }

    public List<DashBoard_Model> getArrayList(String KeyName,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(KeyName, null);
        Type type = new TypeToken<List<DashBoard_Model>>() {}.getType();
        return gson.fromJson(json, type);
    }*/


}
