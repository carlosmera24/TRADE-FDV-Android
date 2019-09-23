package co.com.puli.trade.fdv.actividades;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.Utilidades;

public class DetallePedidoActivity extends AppCompatActivity {
    String URL_CONSULTAR_DETALLE_PEDIDO, id_fdv, id_pdv, fecha;
    WebView wvInforme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pedido);

        URL_CONSULTAR_DETALLE_PEDIDO = getString(R.string.url_server_backend) + "consultar_detalle_pedido.jsp?id_fdv=";

        //Definir Toolbar como ActionBar
        Toolbar bar = findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_fdv = bundle.getString("id_fdv");
        id_pdv = bundle.getString("id_pdv");
        fecha = bundle.getString("fecha");

        //WebView
        wvInforme = findViewById( R.id.wvPedido );
        wvInforme.clearCache( true );
        wvInforme.clearHistory();
        wvInforme.getSettings().setJavaScriptEnabled( true );
        wvInforme.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            ImageBitMap imgbm = new ImageBitMap();
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            Drawable iconBack = new BitmapDrawable(getResources(), imgbm.decodificarImagen(getResources(), R.drawable.ic_btn_atras, displaymetrics.widthPixels, 0));
            getSupportActionBar().setHomeAsUpIndicator(iconBack);

            //Asginar como actividad principal
            GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) getApplicationContext();
            parametros.setActividadActual( this );

            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                wvInforme.loadUrl( URL_CONSULTAR_DETALLE_PEDIDO + id_fdv +"&id_pdv=" + id_pdv +"&fecha="+ fecha );
            }else{
                new Utilidades().mostrarSimpleMensaje(DetallePedidoActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            }

        }catch (Exception e) {
            Log.e("Exception","DetallePedidoActivity.onResume.Exception:"+e.toString() );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarMemoria();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     */
    public void limpiarMemoria() {
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            System.gc();
        } catch (Exception e) {
        }
    }
}
