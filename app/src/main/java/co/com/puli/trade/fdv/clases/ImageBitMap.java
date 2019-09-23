package co.com.puli.trade.fdv.clases;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Carlos Eduardo Mera Ruiz on 27/10/15.
 */
public class ImageBitMap
{
    public static int calcularInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight)
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
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Método encargado de decodificar la imagen y retornarla como Bitmap
     * @param res Instancia Resources, use getResources()
     * @param resId int ID del recurso drawable
     * @param reqWidth int tamaño requerido para el ancho de la imagen
     * @param reqHeight int tamaño requerido para la altura de la imagen
     * @return Bitmap = imagen decodificada
     * */
    public Bitmap decodificarImagen( Resources res, int resId, int reqWidth, int reqHeight )
    {
        final BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opciones);

        //Calcular inSampleSize
        opciones.inSampleSize = calcularInSampleSize( opciones, reqWidth, reqHeight );

        //Decodificar imagen con inSampleSize
        opciones.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource( res, resId, opciones);
    }
}
