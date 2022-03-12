package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.Categoria;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.PDV;
import co.com.puli.trade.fdv.clases.Producto;
import co.com.puli.trade.fdv.clases.TipoAlerta;
import co.com.puli.trade.fdv.clases.Utilidades;
import co.com.puli.trade.fdv.actividades.PrincipalActivity;
import co.com.puli.trade.fdv.database.DatabaseHelper;
import co.com.puli.trade.fdv.database.models.MvtoCheck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Adaptador para los items de la lista de Alumnos por ruta
 * Created by carlos on 10/11/15.
 */
public class PDVRutaAdapter extends BaseAdapter
{
    private ViewHolder holder;
    private LayoutInflater inflater;
    private Activity actividad;
    private ArrayList<PDV> pdvs;
    private PrincipalActivity instancia; //Instancia en ejecución de la Actividad principal
    private String URL_CHECK, URL_TIPOS_NOVEDADES,URL_REGISTRO_NOVEDAD, URL_REGISTRO_NUEVA_NOVEDAD, URL_LISTA_PRODUCTOS, URL_REGISTRO_CONTROL_PDV, id_fdv;
    private HashMap<String,String> postParam;
    private final int CHECK_ALL_DISABLE = 1;
    private final int CHECK_AUSENTE = 2;
    private final int CHECK_IN = 3;
    private final int CHECK_OUT = 4;
    private CustomFonts fuentes;

    public PDVRutaAdapter(Activity actividad, ArrayList<PDV> pdvs, PrincipalActivity instancia, String id_fdv)
    {
        this.actividad = actividad;
        this.pdvs = pdvs;
        this.instancia = instancia;
        this.id_fdv = id_fdv;
        inflater = actividad.getLayoutInflater();
        fuentes = new CustomFonts( actividad.getAssets() );
        URL_CHECK = actividad.getString(R.string.url_server_backend) + "registro_checkin.jsp";
        URL_TIPOS_NOVEDADES = actividad.getString(R.string.url_server_backend) + "consultar_tipo_novedades_empresa.jsp";
        URL_REGISTRO_NOVEDAD = actividad.getString(R.string.url_server_backend) + "registrar_novedad_pdv.jsp";
        URL_REGISTRO_NUEVA_NOVEDAD = actividad.getString(R.string.url_server_backend) + "registrar_nueva_novedad.jsp";
        URL_LISTA_PRODUCTOS = actividad.getString(R.string.url_server_backend) + "consultar_productos_empresa.jsp";
        URL_REGISTRO_CONTROL_PDV = actividad.getString(R.string.url_server_backend) + "registrar_control_pdv.jsp";
    }

    @Override
    public int getCount() {
        return pdvs.size();
    }

    @Override
    public Object getItem(int position)
    {
        return pdvs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position)
    {
        int retorno = 0;
        //Validar si el fin de la ruta ya se registro y deshabilitar los botones
        SharedPreferences sharedPref = actividad.getSharedPreferences(actividad.getString(R.string.key_shared_preferences), Context.MODE_PRIVATE);
        String str_fin_ruta = sharedPref.getString("fin_ruta", null);
        if( str_fin_ruta != null && str_fin_ruta.equals("SI") )
        {
            retorno = CHECK_ALL_DISABLE;
        }else{
            PDV ra = pdvs.get( position );
            if( ra.isCheckAusente() )
            {
                retorno = CHECK_AUSENTE;
            }else if( ra.isCheckIn() && ra.isCheckOut() )
            {
                retorno = CHECK_ALL_DISABLE;
            }else if( ra.isCheckIn() )
            {
                retorno = CHECK_IN;
            }else if( ra.isCheckOut() )
            {
                retorno = CHECK_OUT;
            }
        }
        return retorno;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent)
    {
        View vista = convertView;
        if( vista == null )
        {
            vista = inflater.inflate( R.layout.item_lista_alumno_ruta, null, true);
            holder = new ViewHolder();
            holder.tvNombre = vista.findViewById( R.id.tvNombreAlumno );
            holder.btCheckIn = vista.findViewById( R.id.btCheckIn );
            holder.btCheckOut = vista.findViewById( R.id.btCheckOut );
            holder.btNoRuta = vista.findViewById( R.id.btNoRuta );
            holder.btnNovedad = vista.findViewById( R.id.btNovedad );
            vista.setTag(holder);
        }else{
            holder = (ViewHolder) vista.getTag();
        }

        //Restaurar botones para evitar un mal comportamiento en el Reciclaje(Recycler)
        holder.btCheckIn.setEnabled( true );
        holder.btCheckOut.setEnabled( false );
        holder.btNoRuta.setEnabled( true );
        holder.btCheckIn.setBackgroundResource(R.color.btn_in);
        holder.btCheckOut.setBackgroundResource(R.color.gris);
        holder.btNoRuta.setBackgroundResource(R.color.btn_aus);
        //Validar y deshabilitar los botones IN, OUT, AUS
        int type = getItemViewType(position);
        if(  type == CHECK_ALL_DISABLE) //Deshabilitar IN, OUT, AUSº
        {
            holder.btCheckIn.setEnabled( false );
            holder.btCheckIn.setBackgroundResource(R.color.gris);
            holder.btCheckOut.setEnabled(false);
            holder.btCheckOut.setBackgroundResource(R.color.gris);
            holder.btNoRuta.setEnabled(false);
            holder.btNoRuta.setBackgroundResource(R.color.gris);
        }else{
            if( type == CHECK_AUSENTE ) //Deshabilitar IN, OUT
            {
                holder.btCheckIn.setEnabled(false);
                holder.btCheckIn.setBackgroundResource(R.color.gris);
                holder.btCheckOut.setEnabled(false);
                holder.btCheckOut.setBackgroundResource(R.color.gris);
                holder.btNoRuta.setEnabled(false);
                holder.btNoRuta.setBackgroundResource(R.color.gris);
            }else{
                if( type == CHECK_IN ) //Deshabilitar IN, AUS y habilitar OUT
                {
                    holder.btCheckIn.setEnabled(false);
                    holder.btCheckIn.setBackgroundResource(R.color.gris);
                    holder.btNoRuta.setEnabled(false);
                    holder.btNoRuta.setBackgroundResource(R.color.gris);
                    holder.btCheckOut.setEnabled(true);
                    holder.btCheckOut.setBackgroundResource(R.color.btn_out);
                }
                if( type == CHECK_OUT ) //Deshabilitar OUT, AUS
                {
                    holder.btCheckOut.setEnabled(false);
                    holder.btCheckOut.setBackgroundResource(R.color.gris);
                    holder.btNoRuta.setEnabled(false);
                    holder.btNoRuta.setBackgroundResource(R.color.gris);
                }
            }
        }
        final View copyVista = vista;
        final PDV pdv = (PDV) getItem( position );
        final int copyPosition = position;
        holder.tvNombre.setText( pdv.getNombre() );
        holder.btCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ejecutarCheck( "0", pdv, copyVista, copyPosition);
            }
        });

        holder.btCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ejecutarCheck( "1", pdv, copyVista, copyPosition);
            }
        });

        holder.btNoRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ejecutarCheck( "2", pdv, copyVista, copyPosition);
            }
        });

        holder.btnNovedad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Consultar tipos de novedades
                String empresa = getNombreEmpresa();
                if( empresa != null ) {
                    ConsultarTipoNovedadesTask ctat = new ConsultarTipoNovedadesTask(pdv);
                    postParam = new HashMap<>();
                    postParam.put("empresa", empresa);
                    ctat.execute(URL_TIPOS_NOVEDADES);
                }else{
                    new Utilidades().mostrarSimpleMensaje( actividad, "Novedad", actividad.getString( R.string.txt_msg_error_nombre_colegio), false);
                }
            }
        });

        return vista;
    }

    /**
     * Métdo encargado de ejecutar el proceso del check
     * @param tipo_check 0|1|2 para IN|OUT|AUS
     * */
    private void ejecutarCheck( String tipo_check, PDV pdv, View view, int position)
    {
        Location location = instancia.getLocationGPS();
        if( location != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) actividad.getSystemService(Context.CONNECTIVITY_SERVICE)) ) {
                postParam = new HashMap<>();
                postParam.put("id_pdv", pdv.getId() );
                postParam.put("id_fdv", id_fdv );
                postParam.put("tipo_checkin", tipo_check);
                postParam.put("lat", "" + location.getLatitude());
                postParam.put("lng", "" + location.getLongitude());

                RegistrarCheckTask rct = new RegistrarCheckTask(view, position);
                rct.execute(URL_CHECK);
            }else
            {
                long id = new DatabaseHelper(actividad.getApplicationContext())
                        .setMvtoCheckIn( new MvtoCheck(
                                location.getLatitude(),
                                location.getLongitude(),
                                sdf.format(Calendar.getInstance().getTime()),
                                id_fdv,
                                pdv.getId(),
                                Integer.parseInt( tipo_check )
                        ));

                if( id > 0 )
                {
                    procesarRegistroCheck(position, tipo_check, view);
                }else{
                    new Utilidades().mostrarSimpleMensaje(actividad, actividad.getString(R.string.txt_title_error_database),
                            actividad.getString( R.string.txt_msg_error_insert_database_check ),
                            true  );
                }
            }
        }
    }

    public void procesarRegistroCheck( int position, String tipo_check, View vista)
    {
        String msg = "";
        View btnIn = vista.findViewById(R.id.btCheckIn);
        View btnOut = vista.findViewById(R.id.btCheckOut);
        View btnAus = vista.findViewById(R.id.btNoRuta);
        switch (  tipo_check )
        {
            case "0":
                msg = actividad.getString(R.string.txt_msg_checkin) ;
                btnIn.setEnabled(false);
                btnIn.setBackgroundResource(R.color.gris);
                btnAus.setEnabled(false);
                btnAus.setBackgroundResource(R.color.gris);
                btnOut.setEnabled(true);
                btnAus.setBackgroundResource(R.color.btn_out);
                actualizarDataAdapter( position, Integer.parseInt(tipo_check) );
                //Guardar el ID del PDV para habilitar su uso en pedidos
                agregarPDVCheckIn( (PDV) getItem( position)  );
                //Cargar listado de productos para control
                procesarCapturaControlPDV( (PDV) getItem( position) );
                break;
            case "1":
                msg = actividad.getString(R.string.txt_msg_checkout) ;
                btnOut.setEnabled(false);
                btnOut.setBackgroundResource(R.color.gris);
                btnAus.setEnabled(false);
                btnAus.setBackgroundResource(R.color.gris);
                actualizarDataAdapter( position, Integer.parseInt(tipo_check) );
                new Utilidades().mostrarSimpleMensaje(instancia, actividad.getString( R.string.txt_titulo_dialog_check ), msg, true);
                break;
            case "2":
                msg = actividad.getString(R.string.txt_msg_checkausente) ;
                btnIn.setEnabled(false);
                btnIn.setBackgroundResource(R.color.gris);
                btnOut.setEnabled(false);
                btnOut.setBackgroundResource(R.color.gris);
                btnAus.setEnabled(false);
                btnAus.setBackgroundResource(R.color.gris);
                actualizarDataAdapter( position, Integer.parseInt(tipo_check) );
                new Utilidades().mostrarSimpleMensaje(instancia, actividad.getString( R.string.txt_titulo_dialog_check ), msg, true);
                break;
        }
    }

    /**
     * Método encargado de modificar el estado del alumno IN,OUT,AUS en DATA Adapter
     * Además actualiza el listado, esto permite realizar una modificación en los datos del adaptador
     * evitando así realizar una consulta al WebServices para traer los alumnos de nuevo
     * @param position Posición en la lista DATA del adapter que se modifica
     * @param tipo_check 0|1|2 para IN|OUT|AUS
     * */
    public void actualizarDataAdapter( int position, int tipo_check )
    {
        PDV ra = (PDV) getItem( position );
        switch( tipo_check )
        {
            case 0: //IN
                ra.setCheckIn( 1 );
                break;
            case 1: //OUT
                ra.setCheckOut( 1 );
                break;
            case 2: //AUS
                ra.setCheckAusente( 1 );
                break;
        }
        pdvs.set( position, ra);
        notifyDataSetChanged();
        instancia.validarOUTsFinalRuta(pdvs);
    }

    /**
     * Método encargado de procesar y gestionar la creación de la nueva novedad
     * @param tipo_novedades List<String>
     * @param pdv PDV seleccionado
     * */
    public void mostrarNuevaNovedad(final List<TipoAlerta> tipo_novedades, PDV pdv)
    {
        final PDV punto_venta = pdv;
        AlertDialog.Builder builder = new AlertDialog.Builder( instancia );

        //Layout nueva novedad
        LayoutInflater inflater = actividad.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_nueva_novedad, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = view.findViewById( R.id.tvTituloAlerta );
        tvTitulo.setTypeface(fuentes.getBoldFont());

        final Spinner spNonvedad = view.findViewById( R.id.spAlert );
        ArrayAdapter<TipoAlerta> adaptador = new ArrayAdapter<>(actividad, android.R.layout.simple_spinner_dropdown_item, tipo_novedades);
        spNonvedad.setAdapter(adaptador);

        ImageButton btAgregar = view.findViewById( R.id.btnNuevaNovedad );
        btAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cerar dialogo actual
                dialog.dismiss();
                //Procesar dialogo para agregar nueva novedad
                mostrarAgregarNuevaNovedad(tipo_novedades, punto_venta);
            }
        });

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btGuardar = view.findViewById( R.id.btGuardar );
        btGuardar.setText( actividad.getString( R.string.txt_guardar ));
        btGuardar.setTypeface( fuentes.getRobotoThinFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificar disponibilidad de Red
                if( new Utilidades().redDisponible((ConnectivityManager) actividad.getSystemService(Context.CONNECTIVITY_SERVICE)) )
                {
                    Location location = instancia.getLocationGPS();
                    if( location != null ) {
                        TipoAlerta tp = (TipoAlerta) spNonvedad.getSelectedItem();
                        //Registrar Novedad
                        postParam = new HashMap<>();
                        postParam.put("id_tipo_novedad", "" + tp.getId());
                        postParam.put("id_fdv", id_fdv );
                        postParam.put("id_pdv", "" + punto_venta.getId() );
                        postParam.put("desc_novedad", tp.getDescripcion());
                        postParam.put("lat", ""+location.getLatitude() );
                        postParam.put("lng", ""+location.getLongitude() );

                        RegistrarNovedadTask rnt = new RegistrarNovedadTask(dialog);
                        rnt.execute(URL_REGISTRO_NOVEDAD);
                    }
                }else{
                    new Utilidades().mostrarSimpleMensaje(instancia, "Error red", actividad.getString( R.string.txt_msg_error_red ), true  );
                }
            }
        });

        dialog.show();
    }

    /**
     * Método encargado de gestionar la creación de una nueva novedad en la BBDD
     * @param tipo_novedades List<String>
     * @param pdv PDV seleccionado
     * */
    public void mostrarAgregarNuevaNovedad(final List<TipoAlerta> tipo_novedades, final PDV pdv)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( instancia );

        //Layout agregar nueva novedad
        LayoutInflater inflater = actividad.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_agregar_nueva_novedad, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = view.findViewById( R.id.tvTituloAlerta );
        tvTitulo.setTypeface(fuentes.getBoldFont());

        final EditText etNovedad = view.findViewById( R.id.tvDescNovedad);
        etNovedad.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    //ocultar teclado
                    new Utilidades().ocultarTeclado(instancia, etNovedad);
                    //Procesar registro
                    procesarAgregarNuevaNovedad(etNovedad, dialog);
                    return true;
                }
                return false;
            }
        });

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cerrar dialogo actual
                dialog.dismiss();
                //Retonar la vista del dialogo nueva novedad
                mostrarNuevaNovedad(tipo_novedades, pdv);
            }
        });

        Button btGuardar = view.findViewById( R.id.btGuardar );
        btGuardar.setText( actividad.getString( R.string.txt_guardar ));
        btGuardar.setTypeface( fuentes.getRobotoThinFont() );
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //procesar registro
                procesarAgregarNuevaNovedad(etNovedad, dialog);
            }
        });

        dialog.show();
    }

    /**
     * Método ecargado de validar y realizar la solucitud para el registro de la nueva novedad
     * */
    public void procesarAgregarNuevaNovedad( EditText etNovedad, AlertDialog dialog)
    {
        if(TextUtils.isEmpty( etNovedad.getText() ) )
        {
            etNovedad.setError( instancia.getString( R.string.txt_campo_requerido) );
        }else{
            //Verificar disponibilidad de Red
            if( new Utilidades().redDisponible((ConnectivityManager) actividad.getSystemService(Context.CONNECTIVITY_SERVICE)) )
            {
                Location location = instancia.getLocationGPS();
                if( location != null ) {
                    //Registrar nueva novedad en la BBDD
                    postParam = new HashMap<>();
                    postParam.put("desc", etNovedad.getText().toString() );
                    postParam.put("empresa", getNombreEmpresa() );

                    RegistrarNuevaNovedadTask rnt = new RegistrarNuevaNovedadTask(dialog);
                    rnt.execute(URL_REGISTRO_NUEVA_NOVEDAD);
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(instancia, "Error red", actividad.getString( R.string.txt_msg_error_red ), true  );
            }
        }
    }

    /**
     * Método encargado de procesar la consulta de los datos necesarios para la visualización del control por productos
     * */
    public void procesarCapturaControlPDV( PDV pdv )
    {
        //Verificar disponibilidad de Red
        if( new Utilidades().redDisponible((ConnectivityManager) actividad.getSystemService(Context.CONNECTIVITY_SERVICE)) )
        {
            //Preparar los parámetros para la consulta de los productos
            postParam = new HashMap<>();
            postParam.put("empresa", getNombreEmpresa() );
            ConsultarProductosTask cpt = new ConsultarProductosTask( pdv );
            cpt.execute( URL_LISTA_PRODUCTOS );
        }else{
            new Utilidades().mostrarSimpleMensaje(instancia, "Error red", actividad.getString( R.string.txt_msg_error_red ), true  );
        }

    }

    /**
     * Método encargado de visualizar el dialogo con la opción para capturar los productos
     * */
    public void mostrarCapturaControlPDV(ArrayList<Producto> list_productos, List<Categoria> list_categorias, final PDV pdv )
    {
       AlertDialog.Builder builder = new AlertDialog.Builder( instancia );

        LayoutInflater inflater = actividad.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_control_pdv, null, true);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        builder.setView(view);
        final  AlertDialog dialog = builder.create();
        dialog.setCancelable( false );

        TextView tvTitulo = view.findViewById( R.id.tvTitulo );
        tvTitulo.setTypeface( fuentes.getBoldFont() );

        //Construir spinner categorías
        final Spinner spCategorias = view.findViewById( R.id.spCategorias );
        ArrayAdapter<Categoria> catAdapter = new ArrayAdapter<>(instancia, android.R.layout.simple_spinner_dropdown_item, list_categorias );
        spCategorias.setAdapter( catAdapter );

        final ControlPDVAdapter adaptador = new ControlPDVAdapter( instancia, list_productos, id_fdv, pdv.getId() );
        ListView lista =  view.findViewById( R.id.lvListaProductos );
        lista.setAdapter(adaptador);

        Button btFiltrar = view.findViewById( R.id.btFiltrar );
        btFiltrar.setTypeface( fuentes.getRobotoThinFont() );
        btFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Categoria cat = (Categoria) spCategorias.getSelectedItem();
                adaptador.filtrarProductos( cat.getId() );
            }
        });

        Button btCancelar = view.findViewById( R.id.btCancelar );
        btCancelar.setTypeface( fuentes.getRobotoThinFont() );
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Utilidades().mostrarMensajeBotonOK(instancia, "Control PDV", instancia.getString( R.string.txt_msg_cancelar_control_pdv ),
                        instancia.getString(R.string.txt_aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dia, int which) {
                                dialog.dismiss();
                            }
                        });
            }
        });

        Button btAceptar = view.findViewById( R.id.btGuardar );
        btAceptar.setTypeface( fuentes.getRobotoThinFont() );
        btAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray productos = adaptador.productosToJSONArray();
                if( productos.length() > 0 )//Enviar registro de productos
                {
                    //Verificar disponibilidad de Red
                    if( new Utilidades().redDisponible((ConnectivityManager) actividad.getSystemService(Context.CONNECTIVITY_SERVICE)) )
                    {
                        //Preparar los parámetros para la consulta de los productos
                        postParam = new HashMap<>();
                        postParam.put("id_pdv", pdv.getId() );
                        postParam.put("id_fdv", id_fdv );
                        postParam.put("control", productos.toString() );
                        RegistrarControlPDVTask cpt = new RegistrarControlPDVTask( dialog );
                        cpt.execute( URL_REGISTRO_CONTROL_PDV );
                    }else{
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error red", actividad.getString( R.string.txt_msg_error_red ), true  );
                    }
                }else//No se asignó cantidad a ningún producto
                {
                    new Utilidades().mostrarSimpleMensaje(instancia, "Control PDV", instancia.getString( R.string.txt_msg_productos_control_pdv_no_cantidad ), false);
                }
            }
        });

        dialog.show();
    }

    /**
     * Método encargado de retornar el nombre de la empresa a partir de los parámetros globales
     * @return String nombre de la empresa, null si hay error
     * */
    public String getNombreEmpresa()
    {
        return new DatabaseHelper( instancia ).getUsuario().getEmpresa();
    }

    /**
     * Método encargado de agregar el PDV al listado de CheckIn en los parámetros generales
     * */
    public void agregarPDVCheckIn( PDV pdv )
    {
        GlobalParametrosGenerales parametros = (GlobalParametrosGenerales) actividad.getApplicationContext();
        parametros.agregarPDVCheckIn( pdv );
    }

    /**
     * Clase que contiene cada uno de los objectos del layout item_lista_alumno_ruta.xml
     * */
    static class ViewHolder
    {
        TextView tvNombre;
        Button btCheckIn, btCheckOut, btNoRuta, btnNovedad;
    }

    /**
     * Clase encargada de realizar el proceso de registrar el inicio de la ruta en la BD
     * */
    public class RegistrarCheckTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        int position;
        View vista;


        RegistrarCheckTask( View vista, int position )
        {
            this.vista = vista;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(instancia, null, actividad.getString(R.string.txt_registro_check), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam );
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    if( estado.equals("OK") )
                    {
                        procesarRegistroCheck(position, result.getString("tipo_check"), vista);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(instancia, "Conexión", actividad.getString(R.string.txt_msg_error_consulta), true);
                        Log.e("IRTask-Error", "Estado:"+estado+",msg:"+result.getString("msg")+",cod:"+result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("LA-JSONException", e.toString());
            }
        }
    }//ConsultarListaAlumnosTask

    /**
     * Clase encargada de realizar el proceso de consulta de los tipos de novedades
     * */
    public class ConsultarTipoNovedadesTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        PDV pdv;

        ConsultarTipoNovedadesTask( PDV pdv )
        {
            this.pdv = pdv;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( instancia, null, actividad.getString( R.string.txt_consulta_novedades), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam );
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            JSONArray tipo_novedades = result.getJSONArray("novedades");
                            List<TipoAlerta> list_novedades = new ArrayList<>();
                            for( int i=0; i < tipo_novedades.length(); i++ )
                            {
                                JSONObject tmp = tipo_novedades.getJSONObject(i);
                                TipoAlerta ta = new TipoAlerta( tmp.getInt("id"), tmp.getString("descripcion"), 0 );
                                list_novedades.add( ta );
                            }
                            mostrarNuevaNovedad(list_novedades, pdv);
                            break;
                        case "EMPTY":
                            new Utilidades().mostrarSimpleMensaje(instancia, "Novedades", actividad.getString(R.string.txt_msg_novedades_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(instancia, "Conexión", fch+"\n"+actividad.getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarTipoNovedadesTask

    /**
     * Clase encargada de realizar el registro de la novedad en la BDD
     * */
    public class RegistrarNovedadTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        AlertDialog dialog;

        public RegistrarNovedadTask( AlertDialog dialog )
        {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(instancia, null, actividad.getString( R.string.txt_registro_novedad), true);
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
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            dialog.dismiss();
                            new Utilidades().mostrarSimpleMensaje(instancia, actividad.getString(R.string.txt_nueva_novedad), actividad.getString(R.string.txt_registro_novedad_ok), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(instancia, "Conexión",fch+"\n"+  actividad.getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarNovedadTask

    /**
     * Clase encargada de realizar el registro de la nueva novedad en la BDD
     * */
    public class RegistrarNuevaNovedadTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        AlertDialog dialog;

        public RegistrarNuevaNovedadTask( AlertDialog dialog )
        {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(instancia, null, actividad.getString( R.string.txt_registro_novedad), true);
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
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            dialog.dismiss();
                            new Utilidades().mostrarSimpleMensaje(instancia, actividad.getString(R.string.txt_nueva_novedad), actividad.getString(R.string.txt_registro_novedad_ok), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(instancia, "Conexión",fch+"\n"+  actividad.getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarNuevaNovedadTask

    /**
     * Clase encargada de realizar el proceso de consulta de los productos por empresa
     * */
    public class ConsultarProductosTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        PDV pdv;

        ConsultarProductosTask( PDV pdv )
        {
            this.pdv = pdv;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show( instancia, null, actividad.getString( R.string.txt_consulta_productos), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarHttpPost(url[0], postParam );
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            JSONObject content_productos = result.getJSONObject("productos");
                            if( content_productos.getString("estado").equals("OK") )
                            {
                                //Construir productos
                                JSONArray productos = content_productos.getJSONArray("productos");
                                ArrayList<Producto> list_productos = new ArrayList<>();
                                for( int i=0; i < productos.length(); i++ )
                                {
                                    JSONObject tmp = productos.getJSONObject(i);
                                    Producto producto = new Producto(
                                            tmp.getString("id"),
                                            tmp.getString("nombre"),
                                            tmp.getString("imagen"),
                                            tmp.getString("id_empresa"),
                                            tmp.getString("empresa"),
                                            tmp.getString("id_categoria"),
                                            tmp.getString("categoria"),
                                            0
                                    );
                                    list_productos.add( producto );
                                }
                                //Constuir categorias
                                JSONObject content_categorias = result.getJSONObject("categorias");
                                List<Categoria> list_categorias = new ArrayList<>();
                                if( content_categorias.getString("estado").equals("OK") )
                                {
                                    list_categorias.add( new Categoria("0", "Todos") );
                                    JSONArray categorias = content_categorias.getJSONArray("categorias");
                                    for( int i=0; i < categorias.length(); i++ )
                                    {
                                        JSONObject tmp = categorias.getJSONObject(i);
                                        Categoria categoria = new Categoria( tmp.getString("id"), tmp.getString("nombre") );
                                        list_categorias.add( categoria );
                                    }
                                }else //EMPTY
                                {
                                    list_categorias.add( new Categoria("0", "--Sin categorías--") );
                                }
                                mostrarCapturaControlPDV(list_productos, list_categorias, pdv);
                            }else //EMPTY
                            {
                                new Utilidades().mostrarSimpleMensaje(instancia, "Productos control", actividad.getString(R.string.txt_msg_productos_vacios), true);
                            }
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(instancia, "Conexión", fch+"\n"+actividad.getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//ConsultarProductosTask

    /**
     * Clase encargada de realizar el registro de la nueva novedad en la BDD
     * */
    public class RegistrarControlPDVTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        AlertDialog dialog;

        public RegistrarControlPDVTask( AlertDialog dialog )
        {
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(instancia, null, actividad.getString( R.string.txt_registro_control_pdv), true);
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
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String estado_ce = result.getString("consulta"); //Estado ConsultaExterna
                if( estado_ce.equals("OK") )
                {
                    String estado = result.getString("estado"); //Estado WebServices
                    switch( estado)
                    {
                        case "OK":
                            dialog.dismiss();
                            new Utilidades().mostrarSimpleMensaje(instancia, "Control PDV", actividad.getString(R.string.txt_registro_control_pdv_ok), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(instancia, "Conexión",fch+"\n"+  actividad.getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(instancia, "Error", fch+"\n"+ actividad.getString(R.string.txt_msg_error_consulta), true);
                    }
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                progreso.cancel();
                Log.e("AA-JSONException", e.toString());
            }
        }
    }//RegistrarControlPDVTask
}
