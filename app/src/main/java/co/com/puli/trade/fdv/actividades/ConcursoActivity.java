package co.com.puli.trade.fdv.actividades;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.adaptadores.ChatAdapter;
import co.com.puli.trade.fdv.clases.Chat;
import co.com.puli.trade.fdv.clases.ConsultaExterna;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.GlobalParametrosGenerales;
import co.com.puli.trade.fdv.clases.ImageBitMap;
import co.com.puli.trade.fdv.clases.MediaFilePath;
import co.com.puli.trade.fdv.clases.Utilidades;

import static android.os.Build.VERSION_CODES.M;

public class ConcursoActivity extends AppCompatActivity {
    private String URL_CONSULTAR_CHAT, URL_REGISTRAR_CHAT, URL_UPLOAD_FILE, id_usuario, id_destino, usuario_chat;
    private HashMap<String, String> postParam;
    private RecyclerView lvChat;
    private EditText etMensaje;
    private ImageButton ibEnviar, ibSelMedia;
    private CustomFonts fuentes;
    private final String permisos[] = { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private int PERMISOS_REQUEST = 0;
    private final int MEDIA_SELECT_CAMERA = 1;
    private final int MEDIA_SELECT_GALLERY = 2;
    private AlertDialog dialog_multimedia_select;//Dialogo de selección de medio para multimedia
    private String file_path_camera = null;
    private File tmp_file = null;
    private Socket s = null;
    private PrintWriter output = null;
    private boolean load_msg_init = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concurso);

        URL_CONSULTAR_CHAT = getString(R.string.url_server_backend) + "consultar_chat.jsp";
        URL_REGISTRAR_CHAT = getString(R.string.url_server_backend) + "registro_chat.jsp";
        URL_UPLOAD_FILE = getString(R.string.url_server_backend_web) + "upload_media_chat.php";

        fuentes = new CustomFonts(getAssets());

        //Definir Toolbar como ActionBar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        //Eliminar imagen y asignar color
        bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Extras
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("id_usuario");
        id_destino = bundle.getString("id_destino");
        usuario_chat = bundle.getString("usuario_chat");

        TextView tvTitulo = (TextView) findViewById(R.id.tvTitulo);
        tvTitulo.setTypeface(fuentes.getBoldFont());
        tvTitulo.setText(usuario_chat);

        //RecyclerView para el listado del chat
        lvChat = (RecyclerView) findViewById(R.id.lvChat);
        lvChat.setHasFixedSize( true );
        LinearLayoutManager layout_manager = new LinearLayoutManager(this);
        lvChat.setLayoutManager( layout_manager );
        lvChat.setAdapter(new ChatAdapter(null, ConcursoActivity.this, id_usuario));

        etMensaje = (EditText) findViewById(R.id.etMensaje);
        etMensaje.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    enviarMensaje();
                    return true;
                }
                return false;
            }
        });

        ibEnviar = (ImageButton) findViewById(R.id.ibEnviar);
        ibEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje();
            }
        });

        ibSelMedia = (ImageButton) findViewById(R.id.btnSeleccionarMedia);
        ibSelMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarNuevoMultimedia();
            }
        });
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

            //Consultar mensajes chat
            if( !load_msg_init ) {
                ejecutarConsultaMensajes();
            }
            //Iniciar conexión con el Servidor de Chat
            ConexionSockectChatTask csct = new ConexionSockectChatTask();
            csct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }catch (Exception e) {
            Log.e("Exception","ConcursoActivity.onResume.Exception:"+e.toString() );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        limpiarMemoria();

        //Detener el socket con el servidor del chat
        if( s != null && s.isConnected() )
        {
            try {
                s.close();
                output.close();
            }catch(Exception e){
                Log.e("Exception","ConcursoActivity.onStop.Exception:"+ e.getMessage());
            }
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
     * Método encargado de ejecutar la petición a la clase ConsultarMensajesTask para procesar la consulta de los mensajes del Chat
     */
    public void ejecutarConsultaMensajes() {
        postParam = new HashMap<>();
        postParam.put("id_origen", id_usuario);
        postParam.put("id_destino", id_destino);
        ConsultarMensajesTask cmt = new ConsultarMensajesTask();
        cmt.execute(URL_CONSULTAR_CHAT);
    }

    /**
     * Método encargado de recargar la vista del chat
     */
    public void recargarChat(JSONArray mensajes) {
        try {
            ChatAdapter adapter = (ChatAdapter) lvChat.getAdapter();

            //Definir si es necesario el auto scroll para visualizar los mensajes
            boolean mover = false;
            if (adapter == null) {
                mover = true;
            } else if (adapter.getItemCount() != mensajes.length()) {
                mover = true;
            }
            //Construir mensajes y actualizar el adaptador
            ArrayList<Chat> arrayMensajes = new ArrayList<>();
            for (int i = 0; i < mensajes.length(); i++) {
                JSONObject tmp = mensajes.getJSONObject(i);
                String id = tmp.getString("id");
                String id_origen = tmp.getString("id_origen");
                String id_destino = tmp.getString("id_destino");
                String mensaje = tmp.getString("mensaje");
                String fecha = tmp.getString("fecha");
                Chat chat = new Chat(id, id_origen, id_destino, mensaje, fecha);
                arrayMensajes.add(chat);
            }
            adapter.setMensajes(arrayMensajes);
            adapter.notifyDataSetChanged();

            //Auto scroll si hay nuevos mensajes
            if (mover) {
                lvChat.getLayoutManager().scrollToPosition( adapter.getItemCount() - 1 );
            }
        } catch (Exception e) {
            Log.e("Exception", "ConcursoActivity.recargarChat:" + e.toString());
        }
    }

    /**
     * Método encargado de cargar un nuevo mensaje
     * */
    public void cargarNuevoMensaje(JSONObject msg )
    {
        try {
            ChatAdapter adapter = (ChatAdapter) lvChat.getAdapter();

            //Construir mensajes y actualizar el adaptador
            ArrayList<Chat> arrayMensajes = adapter.getMensajes();
            String id_origen = msg.getString("id_origen");
            String id_destino = msg.getString("id_destino");
            String mensaje = msg.getString("mensaje");
            String fecha = msg.getString("fecha");
            Chat chat = new Chat("0", id_origen, id_destino, mensaje, fecha);
            arrayMensajes.add(chat);

            adapter.setMensajes(arrayMensajes);
            adapter.notifyDataSetChanged();

            //Auto scroll para el nuevo mensaje
            lvChat.getLayoutManager().scrollToPosition( adapter.getItemCount() - 1 );
        }catch (JSONException e) {
            Log.e("JSONException", "ConcursoActivity.cargarNuevoMensaje.JSONException:" + e.toString());
        }
    }

    /**
     * Método encargado de ejecutar el envío del mensaje al WebSevices (Directo a la BBDD)
     */
    public void enviarMensajeBBDD() {
        //Ocultar teclado
        new Utilidades().ocultarTeclado(ConcursoActivity.this, etMensaje);

        String msg = etMensaje.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            //Verificar conexión de red
            if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                postParam = new HashMap<>();
                postParam.put("id_origen", id_usuario);
                postParam.put("id_destino", id_destino);
                postParam.put("mensaje", msg);
                postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                etMensaje.setText("");//Limpiar campo de mensaje
                ConcursoActivity.RegistrarMensajesTask rmt = new ConcursoActivity.RegistrarMensajesTask();
                rmt.execute(URL_REGISTRAR_CHAT);
            } else {
                new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
            }
        }
    }

    /**
     * Método encargado de procesar el envío del mensaje a través del socket
     * */
    public void enviarMensaje()
    {
        String msg = etMensaje.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            //Verificar conexión de red
            if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))
            {
                //Verificar si hay conexión a través del socket
                if( s != null && !s.isClosed() && output != null )
                {
                    //Preparar JSON para enviar
                    try
                    {
                        final JSONObject msgJson = new JSONObject();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        msgJson.put("id_origen", id_usuario);
                        msgJson.put("id_destino", id_destino);
                        msgJson.put("mensaje", msg);
                        msgJson.put("fecha", sdf.format(Calendar.getInstance().getTime()));
                        msgJson.put("guardar_bbdd", "YES" );//Habilitar el registro en la BBDD
                        etMensaje.setText("");//Limpiar campo de mensaje

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                output.println(msgJson.toString());
                                output.flush();
                            }
                        }).start();

                    }catch( JSONException e )
                    {
                        Log.e("JSONException","ConcursoActivity.enviarMensaje.JSONException:"+ e.getMessage() );
                    }
                }else//Sockect cerrado enviar directamente a la BBDD
                {
                    //Enviar mensaje directo a la BBDD
                    enviarMensajeBBDD();
                }
            }else{
                new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
            }
        }
    }

    /**
     * Método encargado de enviar el mensaje multimedia depués de subir la imagen
     * Si hay conexción con el socket envia el mensaje, si no, actualiza los mensajes utilizando el webservices
     * @param msg cuerpo del mensaje del chat como respuesta del WebServices para Upload de la imagen
     * */
    public void enviarMensajeMultimedia( JSONObject msg )
    {
        //Verificar conexión de red
        if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))
        {
            //Verificar si hay conexión a través del socket
            if( s != null && !s.isClosed() && output != null )
            {
                //Preparar JSON para enviar
                try
                {
                    final JSONObject msgJson = msg;
                    msgJson.put("guardar_bbdd", "NO" );//dehabilitar el registro en la BBDD

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            output.println(msgJson.toString());
                            output.flush();
                        }
                    }).start();
                }catch( JSONException e )
                {
                    Log.e("JSONException","ConcursoActivity.enviarMensaje.JSONException:"+ e.getMessage() );
                    ejecutarConsultaMensajes();
                }
            }else//Sockect cerrado actualizar utilizando WebServices
            {
                ejecutarConsultaMensajes();
            }
        }else{
            new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
        }
    }

    /**
     * Método encargado de crear y visualizar nuevo dialogo con opciones para selección de multimedia
     * */
    public void mostrarNuevoMultimedia()
    {
        if( permisosHabilitados() ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ConcursoActivity.this);
            //Layout selecciona multimedia
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.layout_seleccion_media, null, true);

            builder.setView(view);
            dialog_multimedia_select = builder.create();
            final AlertDialog dialog = dialog_multimedia_select;//Copia

            TextView tvTitulo = (TextView) view.findViewById(R.id.tvTituloAlerta);
            tvTitulo.setTypeface(fuentes.getBoldFont());

            ImageButton ibGaleria = (ImageButton) view.findViewById( R.id.ibGaleria );
            ibGaleria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentGallery = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                    intentGallery.setType("image/*");
                    intentGallery.setAction( Intent.ACTION_GET_CONTENT );
                    startActivityForResult( Intent.createChooser(intentGallery, getString( R.string.txt_seleccionar_multimedia ) ),MEDIA_SELECT_GALLERY );
                }
            });

            ImageButton ibCamara = (ImageButton) view.findViewById( R.id.ibCamara );
            ibCamara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        tmp_file = null;
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_";
                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File image = File.createTempFile(
                                imageFileName,  /* nombre */
                                ".jpg",         /* extensión */
                                storageDir      /* directorio */
                        );

                        file_path_camera = image.getAbsolutePath();

                        if (image != null) {
                            Uri destinoURI = FileProvider.getUriForFile(getApplicationContext(),
                                    "co.com.puli.trade.fdv.fileprovider",
                                    image);

                            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, destinoURI);
                            startActivityForResult(intentCamera, MEDIA_SELECT_CAMERA);
                        }
                    }catch( Exception e )
                    {
                        Log.e("Exception","ConcursoActivity.mostrarNuevoMultimedia.ibCamara.Exception:"+ e.toString() );
                    }
                }
            });

            Button btCancelar = (Button) view.findViewById( R.id.btCancelar);
            btCancelar.setTypeface(fuentes.getRobotoThinFont());
            btCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog_multimedia_select.show();
        }else{
            ActivityCompat.requestPermissions( this, permisos, PERMISOS_REQUEST);
        }
    }

    /**
     * Fución encargada de validar los permisos necesarios
     * @return false | true
     * */
    public boolean permisosHabilitados()
    {
        if (Build.VERSION.SDK_INT >= M) {
            for (int i = 0; i < permisos.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permisos[i]) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mostrarNuevoMultimedia();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Controlar seleccion de imagen para envío al chat
        if( resultCode == RESULT_OK )
        {
            //Cerrar dialogo de selección
            if( dialog_multimedia_select != null )
            {
                dialog_multimedia_select.dismiss();
            }
            switch( requestCode )
            {
                case MEDIA_SELECT_CAMERA:
                    procesarImagenCamara( data );
                    break;
                case MEDIA_SELECT_GALLERY:
                    procesarMediaGaleria( data );
                    break;
            }
        }
    }

    public void procesarMediaGaleria( Intent data )
    {
        try {
            Uri imgUri = data.getData();
            Bitmap imgBitMap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            String pathMedia = MediaFilePath.getPath(getApplicationContext(), imgUri);

            Log.i("Path", pathMedia );
            mostrarPreviewMedia( imgBitMap, pathMedia );
        }catch( IOException e )
        {
            Log.e("IOException","ConcursoActivity.procesarMediaGaleria.IOException:"+ e.toString() );
        }catch( Exception e){ e.printStackTrace(); }
    }

    public void procesarImagenCamara( Intent data )
    {
        try {
            //Definir formato de la imagen y compresión
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap imgBitMap = BitmapFactory.decodeFile(file_path_camera,bmOptions);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            imgBitMap.compress(Bitmap.CompressFormat.JPEG, 80, output);
            //Almacener imagen localmente
            //Crear directorio si no existe
            String dir_path = Environment.getExternalStorageDirectory().toString() +"/Puli/Images";
            File dir = new File( dir_path );
            dir.mkdirs();
            //Crear fichero
            String archivo = System.currentTimeMillis() + ".jpg";
            File destino = new File(dir, archivo);
            destino.createNewFile();
            FileOutputStream fos = new FileOutputStream(destino);
            fos.write( output.toByteArray() );
            fos.close();

            //Escalar imagen para Thumbnail
            int targetW = 240;
            int targetH = 240;
            // Get the dimensions of the bitmap
            bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file_path_camera, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            Bitmap thumbnailBitmap = BitmapFactory.decodeFile(file_path_camera, bmOptions);

            //Asignar archivo temporal para ser borrado despues de su envío
            tmp_file = destino;

            mostrarPreviewMedia(thumbnailBitmap, destino.getAbsolutePath() );
        }catch(FileNotFoundException e)
        {
            Log.e("IOException","ConcursoActivity.procesarImagenCamara.FileNotFoundException:"+ e.toString() );
        }catch(IOException e)
        {
            Log.e("IOException","ConcursoActivity.procesarImagenCamara.IOException:"+ e.toString() );
        }
    }

    /**
     * Método encargado de procesar la imagen seleccionada y visualizar una vista previa para envio
     * */
    public void mostrarPreviewMedia( Bitmap imgBitmap, final String pathMedia )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConcursoActivity.this);
        //Layout selecciona multimedia
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_preview_multimedia, null, true);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvTitulo = (TextView) view.findViewById(R.id.tvTituloAlerta);
        tvTitulo.setTypeface(fuentes.getBoldFont());

        ImageView ivPreview = (ImageView) view.findViewById( R.id.ivPreview );
        ivPreview.setImageBitmap( imgBitmap );

        Button btEnviar = (Button) view.findViewById( R.id.btEnviar );
        btEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMedia( pathMedia, dialog );
            }
        });

        Button cancelar = (Button) view.findViewById( R.id.btCancelar );
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mostrarNuevoMultimedia();
            }
        });

        //Agregar control para el cierre del dialogo y borrar el archivo creado
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                borrarFileTmp();
            }
        });

        dialog.show();
    }

    /**
     * Método encargado de procesar el envío del archivo
     * */
    public void enviarMedia (final String pathMedia, final AlertDialog dialog )
    {
        //Verificar conexión de red
        if (new Utilidades().redDisponible((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            postParam = new HashMap<>();
            postParam.put("opcion", "1");
            postParam.put("id_origen", id_usuario);
            postParam.put("id_destino", id_destino);
            postParam.put("fecha", sdf.format(Calendar.getInstance().getTime()));
            postParam.put("notificar","NO"); //Deshabilitar la notificación al registrar el chat en el WebServices
            ConcursoActivity.UploadFileMensajesTask ufmt = new ConcursoActivity.UploadFileMensajesTask(pathMedia, "archivo", dialog);
            ufmt.execute(URL_UPLOAD_FILE);
        } else {
            new Utilidades().mostrarSimpleMensaje(this, "Error red", getString(R.string.txt_msg_error_red), true);
        }
    }

    /**
     * Método encargado de borrar el archivo temporal (File tmp_file)
     * */
    public boolean borrarFileTmp()
    {
        if( tmp_file != null )
        {
            return tmp_file.delete();
        }
        return false;
    }

    /**
     * Clase encargada de realizar el proceso de consulta de los mensajes del chat de la BD
     * */
    private class ConsultarMensajesTask extends AsyncTask<String, Void, JSONObject>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                    switch( estado )
                    {
                        case "OK":
                            JSONArray mensajes = result.getJSONArray("mensajes");
                            load_msg_init = true;
                            recargarChat( mensajes );
                            break;
                        case "EMPTY":
                            //new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Mensajes", getString(R.string.txt_msg_mensajes_vacios), true);
                            break;
                        default:
                            String fch = sdf.format( Calendar.getInstance().getTime() );
                            new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                            Log.e("IRTask-Error", "Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                            break;
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("JSONException", "ConcursoActivity.ConsultarMensajesTask.onPostExecute:"+e.toString());
            }
        }
    }//ConsultarMensajesTask

    /**
     * Clase encargada de realizar el proceso de enviar y registrar el mensaje en el Chat de la BD
     * */
    private class RegistrarMensajesTask extends AsyncTask<String, Void, JSONObject>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                    if( estado.equals("OK") )
                    {
                        ejecutarConsultaMensajes();
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error", "ConcursoActivity.RegistrarMensajesTask.onPostExecute, Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                }
            }catch ( JSONException e )
            {
                Log.e("JSONException", "ConcursoActivity.RegistrarMensajesTask.onPostExecute:"+e.toString());
            }
        }
    }//RegistrarMensajesTask

    /**
     * Clase encargada de realizar el proceso de subir el archivo y registrar el mensaje en la BBDD
     * */
    private class UploadFileMensajesTask extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog progreso;
        String filePath, fileField;
        AlertDialog dialog;
        UploadFileMensajesTask( String filePath, String fileField, AlertDialog dialog )
        {
            this.filePath = filePath;
            this.fileField = fileField;
            this.dialog = dialog;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progreso = ProgressDialog.show(ConcursoActivity.this, null, getString( R.string.txt_registrar_multimedia), true);
            progreso.setCancelable(false);
        }

        @Override
        protected JSONObject doInBackground(String... url) {
            ConsultaExterna ce = new ConsultaExterna();
            return ce.ejecutarUploadFile(url[0], filePath, fileField, postParam );
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
                    if( estado.equals("OK") )
                    {
                        dialog.dismiss(); //Cerrar dialogo preview imagen
                        enviarMensajeMultimedia( result.getJSONObject("mensaje") );
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Conexión", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                        Log.e("Error", "ConcursoActivity.UploadFileMensajesTask.onPostExecute, Estado:" + estado + ",msg:" + result.getString("msg") + ",cod:" + result.getString("code"));
                    }
                }else if( estado_ce.equals("ERROR") )
                {
                    if( result.getInt("code") == 102 ) //Tiempo de consulta superado
                    {
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_tiempo_conexion), true);
                    }else{
                        String fch = sdf.format( Calendar.getInstance().getTime() );
                        new Utilidades().mostrarSimpleMensaje(ConcursoActivity.this, "Error", fch+"\n"+getString(R.string.txt_msg_error_consulta), true);
                    }
                    Log.e("Error","["+ result.toString() +"]");
                }
                progreso.cancel();
            }catch ( JSONException e )
            {
                Log.e("JSONException", "ConcursoActivity.UploadFileMensajesTask.onPostExecute:"+e.toString());
            }
        }
    }//UploadFileMensajesTask

    /**
     * Clase encargada de gestionar la conexión del Sokect con el servidor Chat
     * */
    private class ConexionSockectChatTask extends AsyncTask<Void, JSONObject, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject res = new JSONObject();
            try
            {
                s = new Socket( getString( R.string.url_server ), 9001);
                output = new PrintWriter( s.getOutputStream() );
                BufferedReader input = new BufferedReader( new InputStreamReader( s.getInputStream() ) );

                //Enviar el ID del usuario
                output.println( id_usuario );
                output.flush();

                //Habilitar flujo de lectura de datos, mientras se tenga conexión
                String msg;
                while( s.isConnected() )
                {
                    msg = input.readLine();
                    if( msg != null )
                    {
                        JSONObject jsonMsg = new JSONObject();
                        try {
                            jsonMsg = new JSONObject(msg);
                            publishProgress( jsonMsg );
                        } catch (Exception e)
                        {
                            try {
                                jsonMsg.put("estado", "JSON_ERROR");
                                jsonMsg.put("msg", "Error al convertir el mensje entrante en JSON");
                                jsonMsg.put("error", e.getMessage() );
                                jsonMsg.put("tipo","LOCAL_ERROR");
                            }catch(JSONException je)
                            {
                                Log.e("JSONException","ConcursoActivity.ConexionSockectChatTask.doInBackground.JSONException:"+ je.getMessage());
                            }
                        }
                    }
                }
            }catch( UnknownHostException e)
            {
                try {
                    res = new JSONObject();
                    res.put("tipo","LOCAL_ERROR");
                    res.put("estado", "ERROR");
                    res.put("msg", "Error host deconocido");
                    res.put("error", e.getMessage() );
                }catch(JSONException je )
                {
                    Log.e("JSONException","ConcursoActivity.ConexionSockectChatTask.doInBackground.JSONException:"+ je.getMessage());
                }
            }catch( IOException e )
            {
                try {
                    res = new JSONObject();
                    res.put("tipo","LOCAL_ERROR");
                    res.put("estado", "ERROR");
                    res.put("msg", "Error IO (IOException)");
                    res.put("error", e.getMessage() );
                }catch(JSONException je )
                {
                    Log.e("JSONException","ConcursoActivity.ConexionSockectChatTask.JSONException:"+ je.getMessage());
                }
            }
            return res;
        }

        @Override
        protected void onProgressUpdate(JSONObject... values) {
            super.onProgressUpdate(values);
            try {
                JSONObject msg = values[0];
                switch (msg.getString("tipo"))
                {
                    case "MSG": //Mensaje enviado desde el servidor para el Chat (Entrante)
                        cargarNuevoMensaje( msg.getJSONObject("mensaje") );
                        break;
                    case "RES_MSG": //Respuesta al enviar un mensaje
                        cargarNuevoMensaje( msg.getJSONObject("mensaje") );
//                        etMensaje.setText("");
                        break;
                    case "RES": //Respuesta del servidor
                        break;
                    case "LOCAL_ERROR"://Error local
                        Log.e("errorSocket", String.format("ConcursoActivity.ConexionSocketChatTask.onProgressUpdate.Error[%s-%s-]", msg.getString("msg"), msg.getString("error") ) );
                        break;
                }
            }catch(JSONException je )
            {
                Log.e("JSONException","ConcursoActivity.onProgressUpdate.JSONException:"+ je.getMessage());
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            //Conexión finalizada con el servidor
            try
            {
                if( s!= null ) {
                    s.close();
                    output.close();
                }
                Log.i("Socket", jsonObject.toString() );
            }catch(Exception e )
            {
                Log.e("Exception","ConcursoActivity.onPostExecute.Exception:"+ e.getMessage());
            }
        }
    }//ConexionSocketChatTask
}
