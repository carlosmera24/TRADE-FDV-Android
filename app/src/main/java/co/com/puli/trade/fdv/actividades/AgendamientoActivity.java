package co.com.puli.trade.fdv.actividades;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.adaptadores.AgendamientoAdapter;
import co.com.puli.trade.fdv.adaptadores.PDVRutaAdapter;
import co.com.puli.trade.fdv.clases.Ciudad;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.TipoInspeccion;
import co.com.puli.trade.fdv.clases.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AgendamientoActivity extends AppCompatActivity {
    private String id_fdv, date_select;
    private HashMap<String, String> postParam;
    private String URL_ZONAS, URL_CONSULTAR_PDV, URL_REGISTRO_AGENDA;
    private Button btnGuardar, btnFiltrar;
    private Spinner spZona;
    private ListView listaPDV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamiento);

        URL_ZONAS = getString(R.string.url_server_backend) + "consultar_zonas.jsp";
        URL_CONSULTAR_PDV = getString(R.string.url_server_backend) + "consultar_pdv.jsp";
        URL_REGISTRO_AGENDA = getString(R.string.url_server_backend) + "registrar_agendamiento.jsp";

        CustomFonts fuentes = new CustomFonts( getAssets() );

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById( R.id.toolbar );
        //Eliminar imagen y asignar color
        bar.setBackgroundColor( ContextCompat.getColor( this, R.color.colorPrimary) );
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_fdv = bundle.getString("id_fdv");

        listaPDV = findViewById( R.id.lvListaPDV );

        spZona = findViewById (R.id.spZona );

        //Fecha - DatePicker
        final EditText etFecha = (EditText) findViewById( R.id.etFecha );
        etFecha.setTypeface( fuentes.getRobotoThinFont() );
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Instancia para la fecha actual
                Calendar cal = Calendar.getInstance();
                int year = cal.get( Calendar.YEAR );
                int month = cal.get( Calendar.MONTH );
                int day = cal.get( Calendar.DAY_OF_MONTH );
                //Instancia del EditText para asignar valor seleccionado
                final EditText et = (EditText) v;
                //Datapicker
                DatePickerDialog dpicker = new DatePickerDialog(AgendamientoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Calendar fecha = Calendar.getInstance();
                            fecha.setTime( sdf.parse(year + "-" + (month + 1) + "-" + dayOfMonth ) );
                            date_select = sdf.format( fecha.getTime() ); //Asignar fecha en formato para BBDD
                            sdf = new SimpleDateFormat("dd MMM yyyy");
                            et.setText( sdf.format( fecha.getTime() ) );
                            et.setError( null );
                        }catch(ParseException e){
                            Log.e("AgendamientoActivity","AgendamientoActivity.OnCreate.etFecha.setOnClickListener.DatePickerDialog.onDateSet.ParseException:"+e.toString());
                        }
                    }
                }, year, month, day);
                dpicker.setTitle( getString( R.string.txt_datepicker_titulo ) );
                dpicker.show();
            }
        });

        //Botón calendario
        ImageButton ibCalendar = findViewById( R.id.ibCalendar );
        ibCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Activar click sobre el campo fecha
                etFecha.callOnClick();
            }
        });

        //Titulos
        TextView tvTituloPDV = findViewById( R.id.tvTituloPDV);
        tvTituloPDV.setTypeface( fuentes.getRobotoThinFont() );
        TextView tvTituloVisitar = findViewById( R.id.tvTituloVisitar);
        tvTituloVisitar.setTypeface( fuentes.getRobotoThinFont() );

        //Botón filtrar
        btnFiltrar = findViewById( R.id.btFiltrar );
        btnFiltrar.setTypeface( fuentes.getBoldFont() );
        btnFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Consultar PDV de acuerdo a la zona
                Ciudad zona = (Ciudad) spZona.getSelectedItem();
                TextView tvZona = (TextView) spZona.getSelectedView();
                tvZona.setTextColor( ContextCompat.getColor( AgendamientoActivity.this, R.color.black ) );
                if( zona.getId() == -1 )
                {
                    tvZona.setTextColor( Color.RED );
                    tvZona.setError( getString(R.string.txt_campo_requerido) );
                }else {
                    postParam = new HashMap<>();
                    postParam.put("zona", "" + zona.getId());
                    ConsultaPDVTask cpdvt = new ConsultaPDVTask();
                    cpdvt.execute(URL_CONSULTAR_PDV);
                }
            }
        });


        //Boton solicitar cita
        btnGuardar = findViewById( R.id.btGuardar );
        btnGuardar.setTypeface( fuentes.getBoldFont() );
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgendamientoAdapter adapterPDV = (AgendamientoAdapter) listaPDV.getAdapter() ;

                if(TextUtils.isEmpty( date_select ) )
                {
                    etFecha.setError( getString(R.string.txt_campo_requerido) );
                }else
                {
                    try {
                        //Verificar fecha/hora
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        //Obtener la fech actual
                        Calendar fch_actual = Calendar.getInstance();
                        fch_actual.setTime( new Date() );
                        //Eliminar el tiempo para la fecha actual, establecer 00:00:00:00 (HH:MM:SS:MM)
                        fch_actual.set( Calendar.HOUR_OF_DAY, 0 );
                        fch_actual.set( Calendar.MINUTE, 0 );
                        fch_actual.set( Calendar.SECOND, 0 );
                        fch_actual.set( Calendar.MILLISECOND, 0 );
                        //Obtener la fecha seleccionada
                        Calendar fch_sel = Calendar.getInstance();
                        fch_sel.setTime(sdf.parse(date_select));

                        if( fch_sel.getTime().compareTo( fch_actual.getTime() ) < 0 )//Fecha no validos, es inferior a la actual
                        {
                            etFecha.setError( getString(R.string.txt_dato_invalido) );
                        }else if( adapterPDV != null )//Verificar que ya este definido el listado de PDV
                        {
                            if( adapterPDV.agendamientoToJSONArray().length() <= 0 )
                            {
                                new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this,"Agendamiento", getString( R.string.txt_msg_agendamiento_vacio), true);
                            }else{
                                //Prepatar datos para el registro
                                postParam = new HashMap<>();
                                postParam.put("id_fdv", id_fdv);
                                postParam.put("fecha", date_select);
                                postParam.put("agenda",  adapterPDV.agendamientoToJSONArray().toString() );

                                //Enviar registro de agendamiento
                                RegistrarAgendamientoTask rsc = new RegistrarAgendamientoTask();
                                rsc.execute(URL_REGISTRO_AGENDA);
                            }

                        }
                    }catch(ParseException e){
                        Log.e("AgendamientoActivity","AgendamientoActivity.OnCreate.setOnClickListener.setOnClickListener.ParseException:"+e.toString());
                    }
                }
            }
        });
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

            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                //Consultar listado de zonas
                ConsultaZonasTask cpt = new ConsultaZonasTask();
                cpt.execute( URL_ZONAS );
            }else{
                new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error red", getString( R.string.txt_msg_error_red ), true  );
            }

        }catch (Exception e){}
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

    /**
     * Método encargado de liberar memoria para optimizar el uso de momoria RAM
     * */
    public void limpiarMemoria()
    {
        try {
            getSupportActionBar().setHomeAsUpIndicator(null);
            System.gc();
        }catch (Exception e){}
    }

    /**
     * Clase encargada de realizar el proceso de consulta de las zonas
     */
    public class ConsultaZonasTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AgendamientoActivity.this, null, getString(R.string.txt_consulta_lista_zonas), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttp(url[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK")) {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch (estado) {
                        case "OK":

                            //Agregar listado de zonas
                            JSONArray zonas = result.getJSONArray("zonas");
                            List<Ciudad> list_zonas = new ArrayList<>();
                            list_zonas.add( new Ciudad(-1, "--Zona geográfica--") );
                            list_zonas.add( new Ciudad(0, "Todas") );
                            for (int i = 0; i < zonas.length(); i++) {
                                JSONObject tmp = zonas.getJSONObject(i);
                                Ciudad ta = new Ciudad(tmp.getInt("id"), tmp.getString("nombre") );
                                list_zonas.add(ta);
                            }
                            ArrayAdapter<Ciudad> adaptador = new ArrayAdapter<>(AgendamientoActivity.this, android.R.layout.simple_spinner_dropdown_item, list_zonas);
                            spZona.setAdapter(adaptador);

                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Lista Zonas", getString(R.string.txt_msg_lista_zonas_vacios), true);
                            //Deshabilitar el botón
                            btnGuardar.setEnabled( false );
                            btnGuardar.setBackgroundColor( ContextCompat.getColor( AgendamientoActivity.this, R.color.gris ));
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultaZonasTask

    /**
     * Clase encargada de realizar el proceso de consulta de los PDV
     */
    public class ConsultaPDVTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AgendamientoActivity.this, null, getString(R.string.txt_consulta_lista_control_pdv), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK")) {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch (estado) {
                        case "OK":

                            //Agregar listado de pdv a ListView
                            JSONArray pdvs = result.getJSONArray("pdv");
                            ArrayList<TipoInspeccion> list_pdvs = new ArrayList<>();
                            for( int i=0; i < pdvs.length(); i++ )
                            {
                                JSONObject tmp = pdvs.getJSONObject(i);
                                TipoInspeccion ta = new TipoInspeccion( tmp.getInt("id"), tmp.getString("nombre"), 0 );
                                list_pdvs.add( ta );
                            }

                            AgendamientoAdapter adapter = new AgendamientoAdapter( AgendamientoActivity.this, list_pdvs);
                            listaPDV.setAdapter( adapter );

                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Lista PDV", getString(R.string.txt_msg_lista_pdv_vacio), true);
                            //Deshabilitar el botón
                            btnGuardar.setEnabled( false );
                            btnGuardar.setBackgroundColor( ContextCompat.getColor( AgendamientoActivity.this, R.color.gris ));
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultaPDVTask

    /**
     * Clase encargada de realizar el registro de la agenda en la BD
     */
    public class RegistrarAgendamientoTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progreso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(AgendamientoActivity.this, null, getString(R.string.txt_registro_agendamiento), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam);
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if (estado_ce.equals("OK")) {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch (estado) {
                        case "OK":
                            new Utilidades().mostrarMensajeBotonSoloOK(AgendamientoActivity.this, "Agendamiento", getString(R.string.txt_registro_agendamiento_ok), getString(R.string.txt_aceptar),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                        }
                                    });
                            break;
                        default:
                            String fch = sdf.format(Calendar.getInstance().getTime());
                            new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Conexión", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                } else if (estado_ce.equals("ERROR")) {
                    if (result.getInt("code") == 102) //Tiempo de consulta superado
                    {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_tiempo_conexion), true);
                    } else {
                        String fch = sdf.format(Calendar.getInstance().getTime());
                        new Utilidades().mostrarSimpleMensaje(AgendamientoActivity.this, "Error", fch + "\n" + getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            } catch (JSONException e) {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarAgendamientoTask
}
