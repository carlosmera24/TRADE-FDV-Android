package co.com.puli.trade.fdv.actividades;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;

public class PausaActivaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pausa_activa);

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary ) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //WebView
        WebView webVPausa = (WebView) findViewById( R.id.wvPausa );
        webVPausa.getSettings().setJavaScriptEnabled(true);
        webVPausa.loadUrl("http://www.viajesnuevacolombia.com/app-pausa-activa/");
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable( getResources(),  imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0) );
            getSupportActionBar().setHomeAsUpIndicator(iconBack);
        }catch (Exception e)
        {
            Log.e("Exception", "PausaActivaActivity.onResume.Exception:"+ e.toString() );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
