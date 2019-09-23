package co.com.puli.trade.fdv.clases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import java.net.URL;

/**
 * Clase encargada de gestionar la descarga de imagen desde URL y retornarla como Bitmap
 * Created by carlos on 30/11/15.
 */
public class DescargarImagenTask extends AsyncTask<String,Void,Bitmap>
{
    private ProgressDialog progreso;
    private Activity actividad;
    private String msg; //Texto a visualizar al inicio de su ejecución

    /**
     *Constructor de la clase
     * @param actividad Actividad que ejecuta la clase (Activity.this | this)
     * @param msg Texto a visualizar durante la ejecución de la clase
     * */
    public DescargarImagenTask(Activity actividad, String msg)
    {
        this.actividad = actividad;
        this.msg = msg;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        actividad.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progreso = ProgressDialog.show( actividad, "", msg, true);
                progreso.setCancelable( false );
            }
        });
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        try
        {
            URL web = new URL( url[0] );

            //***** Optimizar el tamaño de la imagen para reducir el uso de la memoria RAM
            //Tamaño de la pantalla
            DisplayMetrics metrics = new DisplayMetrics();
            actividad.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int screenHeight = 0; //metrics.heightPixels;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( web.openConnection().getInputStream(), null, options  );

            //Calcular inSampleSize
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > screenHeight || width > screenWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > screenWidth
                        && (halfWidth / inSampleSize) > screenHeight) {
                    inSampleSize *= 2;
                }
            }

            //Asignar a las opciones el inSampleSize
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            //Usar solo el cambio del color
            //options.inPreferredConfig = Bitmap.Config.RGB_565;

            return BitmapFactory.decodeStream( web.openConnection().getInputStream(), null, options );
        }catch(OutOfMemoryError e ){
            Log.e("OutOfMemoryError", "DescargarImagenTask.doInBackground.OutOfMemoryError:"+e.toString() );
            return null;
        }catch (Exception e ) {
            Log.e("Exception", "DescargarImagenTask.doInBackground:"+e.toString() );
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        progreso.cancel();
    }


}
