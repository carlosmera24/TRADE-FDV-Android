package co.com.puli.trade.fdv.actividades;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.ImageBitMap;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity
{
    private LinearLayout content_layout;
    private ImageView ivLogo;
    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        content_layout = (LinearLayout) findViewById( R.id.contentLayout );
        ivLogo = (ImageView) findViewById( R.id.imageViewLogo );

        //Definir el tiempo que se visualizará la actividad para pasar al login
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                //Iniciar la actividad Login
                Intent mainIntent = new Intent().setClass(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        //Asignar imagenes
        ImageBitMap image = new ImageBitMap();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Bitmap img = image.decodificarImagen(getResources(), R.drawable.background_route_gray, displaymetrics.widthPixels, 0);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
        {
            content_layout.setBackground( new BitmapDrawable( getResources(), img ) );
        }else {
            content_layout.setBackgroundDrawable(new BitmapDrawable(getResources(), img));
        }

        img = image.decodificarImagen(getResources(), R.drawable.logo_route_white, displaymetrics.widthPixels, 0);
        ivLogo.setImageBitmap(img);
    }

    @Override
    protected void onStop() {
        super.onStop();
        limpiarMemoria();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarMemoria();
    }

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     * */
    public void limpiarMemoria()
    {
        content_layout.setBackgroundResource(0);
        ivLogo.setImageBitmap(null);
        System.gc();
    }
}
