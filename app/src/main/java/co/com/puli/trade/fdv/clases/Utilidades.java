package co.com.puli.trade.fdv.clases;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.database.DatabaseHelper;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by carlos on 9/11/15.
 */
public class Utilidades
{
    /**
     * Método encargado de visualizar mensaje simple empleando AlertDialog
     * @param context Context de la actividad en ejecución, use Activity.this o getApplicationContext
     * @param title Titulo del mensaje
     * @param msg Contenido del mensaje a visualizar
     * @param bt_posive Valor boolean que permite indicar si se visualizará el botón Aceptar, true o false por defecto
     * */
    public void mostrarSimpleMensaje(Context context, String title, String msg, boolean bt_posive)
    {
        AlertDialog.Builder build;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            build = new AlertDialog.Builder(context);
        }else{
            build = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        }
        build.setTitle(title);
        build.setMessage(msg);
        if( bt_posive )
        {
            build.setPositiveButton(R.string.txt_aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        build.show();
    }

    /**
     * Método encargado de visualizar mensaje simple empleando AlertDialog
     * @param context Context de la actividad en ejecución, use Activity.this o getApplicationContext
     * @param title Titulo del mensaje
     * @param msg Contenido del mensaje a visualizar
     * @param txt_btn_ok texto del botón OK
     * @param okClickListener DialogInterface.OnClicklistener con la acción para el botón OK
     * */
    public void mostrarMensajeBotonOK(Context context, String title, String msg, String txt_btn_ok, DialogInterface.OnClickListener okClickListener)
    {
        AlertDialog.Builder build;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            build = new AlertDialog.Builder(context);
        }else{
            build = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        }
        build.setCancelable(false);
        build.setTitle( title );
        build.setMessage( msg );
        build.setPositiveButton(txt_btn_ok, okClickListener);
        build.setNegativeButton(R.string.txt_cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        build.show();
    }

    /**
     * Método encargado de visualizar mensaje simple empleando AlertDialog
     * @param context Context de la actividad en ejecución, use Activity.this o getApplicationContext
     * @param title Titulo del mensaje
     * @param msg Contenido del mensaje a visualizar
     * @param txt_btn_ok texto del botón OK
     * @param okClickListener DialogInterface.OnClicklistener con la acción para el botón OK
     * */
    public void mostrarMensajeBotonSoloOK(Context context, String title, String msg, String txt_btn_ok, DialogInterface.OnClickListener okClickListener)
    {
        AlertDialog.Builder build;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            build = new AlertDialog.Builder(context);
        }else{
            build = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        }
        build.setCancelable(false);
        build.setTitle( title );
        build.setMessage( msg );
        build.setPositiveButton(txt_btn_ok, okClickListener);
        build.show();
    }

    /**
     * Método encargado de validar la conexión a internet
     * @param manager use (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)
     * @return false | true*/
    public boolean redDisponible( ConnectivityManager manager )
    {
        NetworkInfo net_info = manager.getActiveNetworkInfo();
        if( net_info == null || !net_info.isConnected() || !net_info.isAvailable() )
        {
            return false;
        }else{
            return true;
        }
    }

    /**
     * Método encargado de validar la existencia de google services
     * @param context Actividad en ejecución, use suActividad.this o getApplicationContext
     * @param titleError titulo a visualizar en el mensaje de error si no está disponible Google Play Services
     * @param msgError Mensaje a visualizar como error al no estar dispnible Google Play Services
     * @return false | true Al ser false se visualiza el mensaje informativo, empleando strError como mensaje
     * */
    public boolean googlePlayServicesDisponible(Context context, String titleError, String msgError)
    {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(context);
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        if(result != ConnectionResult.SUCCESS)
        {
            if( googleApi.isUserResolvableError(result) )
            {
                googleApi.getErrorDialog( ((Activity) context), result, PLAY_SERVICES_RESOLUTION_REQUEST ).show();
            }else
            {
                mostrarSimpleMensaje(context, titleError, msgError, true);
            }
            return false;
        }else
        {
            return true;
        }
    }

    /**
     * Método encargado de ocultar el teclado, óptimo para utilizar con un TextView o cualquier otro objeto de entrada
     * @param  context Context de la actividad en ejecución, use Activity.this o getApplicationContext
     * @param view Objecto view que ha realizado el llamado del teclado, por Ejemplo TextView*/
    public void ocultarTeclado(Context context, View view)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow( view.getWindowToken(), 0);
    }

    /**
     * Método encargado de visualizar DatePickerDialog a partir de un objeto EditText
     * Asigna la fecha seleccionada al EditText en formado YYYY-MM-DD
     * @param context de la actividad en ejecución
     * @param et EditText que solicita la visualización del DatePicker*/
    public static void mostrarDatePickerEditText(Context context, final EditText et, String titulo)
    {
        Calendar cal_actual = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fch_str = year + "-" + month + "-" + dayOfMonth;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
                    Calendar fecha = Calendar.getInstance();
                    fecha.setTime( sdf.parse( fch_str ) );
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    fch_str = sdf.format(fecha.getTime());
                }catch(ParseException e){}

                et.setText( fch_str );
            }
        }, cal_actual.get( Calendar.YEAR), cal_actual.get( Calendar.MONTH ), cal_actual.get( Calendar.DAY_OF_MONTH) );
        datePicker.setTitle( titulo );
        datePicker.show();
    }

    /**
     * Método encargado de convertir imagen String 64 a Bitmap
     * @param imagen64 String que contiene la imagen codificada a 64 Bits
     * @param ancho Requerido final para la imagen
     * @param alto Requerido final para la imagen
     * */
    public Bitmap imagenString64ToBitmap(String imagen64, int ancho, int alto)
    {
        Bitmap imagen = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            byte arrayImagen[] = Base64.decode(imagen64.getBytes(), Base64.DEFAULT);
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(arrayImagen, 0, arrayImagen.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, ancho, alto);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            imagen = BitmapFactory.decodeByteArray(arrayImagen, 0, arrayImagen.length, options);
            System.gc();
        }catch(OutOfMemoryError e ){
            Log.e("OutOfMemoryError", "imagenString64ToBitmap.OutOfMemoryError:"+e.toString() );
        }catch(Exception e)
        {
            Log.e("Exception", "imagenString64ToBitmap,Exception:"+e.toString());
        }
        return imagen;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Método encargado de retonar el nombre de la versión de Android
     * */
    public String getNombreVersionAndroid()
    {
        Field fields[] = Build.VERSION_CODES.class.getFields();
        for( Field field : fields )
        {
            String fieldName = field.getName();
            int fieldValue = -1;
            try {
                fieldValue = field.getInt(new Object());
            }catch(IllegalArgumentException e )
            {
                Log.i("IrgumentAException", "Utilidades.getNombreVersionAndroid.IllegalArgumentException: "+e.toString() );
            }catch(IllegalAccessException e )
            {
                Log.i("IAccessException", "Utilidades.getNombreVersionAndroid.IllegalArgumentException: "+e.toString() );
            }

            if( fieldValue == Build.VERSION.SDK_INT )
            {
                return fieldName;
            }
        }
        return null;
    }

    /**
     * Método encargado de validar si la ruta ha iniciado
     * @return boolean
     * */
    public boolean isRutaIniciada(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        String fch_ruta = sharedPref.getString("fch_ruta", null);
        if( fch_ruta != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fch_actual = sdf.format(Calendar.getInstance().getTime());
            if( fch_ruta.equals( fch_actual ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Método encargado de validar si la ruta ha finalizado
     * @return boolean
     * */
    public boolean isRutaFinalizada( Context context )
    {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        String fch_ruta = sharedPref.getString("fch_ruta", null);
        if( fch_ruta != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fch_actual = sdf.format(Calendar.getInstance().getTime());
            if( fch_ruta.equals( fch_actual ) )
            {
                //Validar FIN ruta
                String str_fin_ruta = sharedPref.getString("fin_ruta", null);
                if( str_fin_ruta != null &&  str_fin_ruta.equals("SI"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Método encargado de validar si se requiere actualizar la base de datos local para
     * el modo offline
     * @param context
     * @return
     */
    public boolean requireActualizacionOffline( Context context )
    {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        String fch_updated = sharedPref.getString("fch_updated_local_bbdd", null);

        if( fch_updated != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fch_actual = sdf.format(Calendar.getInstance().getTime());
            DatabaseHelper databaseHelper = new DatabaseHelper( context );

            if( databaseHelper.getListAlumnosRuta().size() == 0
                    || databaseHelper.getTiposInspeccionAdapter().size() ==0 )
            {
                return true;
            }
            if( fch_updated.equals( fch_actual) )
            {
                return false;
            }
            else{
                return true;
            }
        }

        return true;
    }
}
