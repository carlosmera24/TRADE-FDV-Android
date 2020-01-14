package co.com.puli.trade.fdv.actividades;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;

public class ControlPDVActivity extends AppCompatActivity {
    private CustomFonts fuentes;
    private String id_fdv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_pdv);

        fuentes = new CustomFonts(getAssets());

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_fdv = bundle.getString("id_fdv");


        //Labels
        TextView tvAgregar_pdv = findViewById( R.id.tvLabelAgregarPDV );
        tvAgregar_pdv.setTypeface( fuentes.getRobotoThinFont() );
        TextView tvPunto_oportunidad = findViewById( R.id.tvLabelPuntoOportunidad );
        tvPunto_oportunidad.setTypeface( fuentes.getRobotoThinFont() );
        TextView tvNuevo_pedido = findViewById( R.id.tvLabelNuevoPedido );
        tvNuevo_pedido.setTypeface( fuentes.getRobotoThinFont() );
        TextView tvMis_pedidos = findViewById( R.id.tvLabelMisPedidos );
        tvMis_pedidos.setTypeface( fuentes.getRobotoThinFont() );
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

        }catch (Exception e) {
            Log.e("Exception","ControlPDVActivity.onResume.Exception:"+e.toString() );
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

    /**
     * * Método encargado de controlar Click en los items del Menu Drawer
     * */
    public void onClickItemDrawerMenu(View v)
    {
        Intent intent;
        switch ( v.getId() )
        {
            case R.id.lItemAgregarPDV:
                intent = new Intent( ControlPDVActivity.this, NuevoPDVActivity.class );
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
            case R.id.lItemPuntoOportunidad:
                intent = new Intent( ControlPDVActivity.this, PuntoOportunidadActivity.class);
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
            case R.id.lItemNuevoPedido:
                intent = new Intent( ControlPDVActivity.this, NuevoPedidoActivity.class);
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
            case R.id.lItemMisPedidos:
                intent = new Intent( ControlPDVActivity.this, MisPedidosActivity.class);
                intent.putExtra( "id_fdv", id_fdv );
                startActivity( intent );
                break;
        }
    }
}
