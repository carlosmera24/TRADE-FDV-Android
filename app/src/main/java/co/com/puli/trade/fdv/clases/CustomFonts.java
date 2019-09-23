package co.com.puli.trade.fdv.clases;

import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Created by Carlos Eduardo Mera Ruiz on 30/10/15.
 * Clase utilizada para gestionar las fuentes personalizadas a utilizar en el APP, de tal manera que sea mas recursivo su uso
 */
public class CustomFonts
{
    private AssetManager assManager;
    private Typeface fBold, fRegular;

    /**
     * Método constructor
     * @param assManager Instancia del gestor de recursos assets, utilice getAssets para el parámetro
     * */
    public CustomFonts(AssetManager assManager)
    {
        this.assManager = assManager;
        fBold = Typeface.createFromAsset(assManager,"fonts/theboldfont.ttf");
        fRegular = Typeface.createFromAsset(assManager,"fonts/RobotoCondensed_Regular.ttf");
    }

    /**
     * Método encargado de retornar la fuente "The Bold Font"
     */
    public Typeface getBoldFont()
    {
        return fBold;
    }

    /**
     * Método encargado de retornar la fuente "Roboto Thin"
     * 2016-12-16 Se reemplaza la fuente por Roboto Condensed Rugular
     */
    public Typeface getRobotoThinFont()
    {
        return getRegularFont();
    }

    /**
     * Método encargado de retornar la fuente regular para textos/parrafos
     * */
    public Typeface getRegularFont()
    {
        return fRegular;
    }
}
