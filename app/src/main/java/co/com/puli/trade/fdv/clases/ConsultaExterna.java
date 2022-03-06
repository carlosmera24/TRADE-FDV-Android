package co.com.puli.trade.fdv.clases;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Clase encargada de gestionar las conexiones a WebServices (Request)
 * Utilizando HttpURLConnection
 * Created by Carlos Eduardo Mera Ruiz on 11/11/15.
 */
@SuppressWarnings("unchecked")
public class ConsultaExterna
{
    private  final int HTTP_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    private HttpURLConnection http_conn;

    /**
     * Método encargado de procesar peticón POST
     * @param  strUrl URL del servicio (WebServices)
     * @param param Parámetros o variables del servicio almacenados como Clave/Valor
     * @return JSON con información de la consulta {consulta:OK | ERROR, [ Si ERROR, code:entero], Valores JSON retorno del WebServices}
     * */
    public JSONObject ejecutarHttpPost( String strUrl, HashMap<String,String> param)
    {
        JSONObject resultado = new JSONObject();
        http_conn = null;

        try
        {
            URL url = new URL(strUrl);

            http_conn = (HttpURLConnection) url.openConnection();
            http_conn.setReadTimeout(HTTP_TIMEOUT);
            http_conn.setConnectTimeout(HTTP_TIMEOUT);
            http_conn.setRequestMethod("POST");
            http_conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            http_conn.setRequestProperty("charset","utf-8");
            http_conn.setDoInput(true);
            http_conn.setDoOutput(true);

            OutputStream os = http_conn.getOutputStream();
            BufferedWriter write = new BufferedWriter( new OutputStreamWriter( os, "UTF-8") );
            write.write( getParametrosMap( param ) );
            write.flush();
            write.close();
            os.close();

            int responseCode = http_conn.getResponseCode();

            if( responseCode == HttpURLConnection.HTTP_OK )
            {
                resultado = inputStreamToJSONObject( http_conn.getInputStream() );
                if( resultado != null ) {
                    Log.i("ResString", resultado.toString());
                    resultado.put("consulta", "OK");//Se realizó la consulta al sevidor
                }else{
                    resultado = new JSONObject();
                    resultado.put("consulta", "ERROR");//Se realizó la consulta al sevidor
                    resultado.put("code",104);
                    resultado.put("msg","inputStreamToJSONObject( http_conn.getInputStream() ) retorno null");
                }
            }else
            {
                resultado.put("consulta", "ERROR" );//Error en la consulta
                resultado.put("code", responseCode );
            }

        }catch( MalformedURLException e )
        {
            Log.e("CE-MURLException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",101);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( IOException e )
        {
            Log.e("CE-IOException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",102); //Error en el proceso, TimeOut
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( JSONException e ) {
            Log.e("CE-JSONException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",103);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }finally {
            if( http_conn != null )
            {
                http_conn.disconnect();
            }
        }

        return  resultado;
    }//ejecutarHttpPost

    /**
     * Método encargado de procesar peticón
     * @param strUrl URL del servicio (WebServices)
     * @return JSON con información de la consulta {consulta:OK | ERROR, [ Si ERROR, code:entero], Valores JSON retorno del WebServices}
     * */
    public JSONObject ejecutarHttp( String strUrl )
    {
        JSONObject resultado = new JSONObject();
        http_conn = null;

        try
        {
            URL url = new URL(strUrl);

            http_conn = (HttpURLConnection) url.openConnection();
            http_conn.setReadTimeout(HTTP_TIMEOUT);
            http_conn.setConnectTimeout(HTTP_TIMEOUT);

            int responseCode = http_conn.getResponseCode();

            if( responseCode == HttpURLConnection.HTTP_OK )
            {
                resultado = inputStreamToJSONObject( http_conn.getInputStream() );
                if( resultado != null ) {
                    Log.i("ResString", resultado.toString());
                    resultado.put("consulta", "OK");//Se realizó la consulta al sevidor
                }else{
                    resultado = new JSONObject();
                    resultado.put("consulta", "ERROR");//Se realizó la consulta al sevidor
                    resultado.put("code",104);
                    resultado.put("msg","inputStreamToJSONObject( http_conn.getInputStream() ) retorno null");
                }
            }else
            {
                resultado.put("consulta", "ERROR" );//Error en la consulta
                resultado.put("code", responseCode );
            }

        }catch( MalformedURLException e )
        {
            Log.e("CE-MURLException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",101);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( IOException e )
        {
            Log.e("CE-IOException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",102); //Error en el proceso, TimeOut
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( JSONException e ) {
            Log.e("CE-JSONException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",103);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }finally {
            if( http_conn != null )
            {
                http_conn.disconnect();
            }
        }

        return  resultado;
    }//ejecutarHttp

    /**
     * Método encargado de procesar la subida de un fichero multimedia
     * @param strUrl URL del Servicio (WebServices)
     * @param filepath ruta String del archivo a subir, debe existir para no tener errores o excepciones
     * @param filefield Nombre de la variable requerida por el WebServices para identificar el archivo
     * @param param Parámetros o variables del servicio almacenados como Clave/Valor, use null o instancia vacía para no usar parámetros
     * @return JSON con información de la consulta {consulta:OK | ERROR, [ Si ERROR, code:entero], Valores JSON retorno del WebServices}
     * */
    public JSONObject ejecutarUploadFile( String strUrl, String filepath, String filefield, HashMap<String,String> param )
    {
        JSONObject resultado = new JSONObject();
        http_conn = null;
        DataOutputStream outputStream = null;

        String twoHyphens = "--";
        String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
        String lineEnd = "\r\n";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        try
        {
            //Verificar que el archivo existe (file)
            File sourceFile = new File(filepath);
            if (!sourceFile.isFile()) {
                resultado.put("consulta", "ERROR");
                resultado.put("msg", "El archivo indicado no existe");
                resultado.put("code", 0 );
            }else
            {
                File file = new File(filepath);
                FileInputStream fileInputStream = new FileInputStream(file);

                URL url = new URL(strUrl);
                http_conn = (HttpURLConnection) url.openConnection();

                http_conn.setDoInput(true);
                http_conn.setDoOutput(true);
                http_conn.setUseCaches(false);

                http_conn.setRequestMethod("POST");
                http_conn.setRequestProperty("Connection", "Keep-Alive");
                http_conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                http_conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

                //Post File
                outputStream = new DataOutputStream(http_conn.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] +"\"" + lineEnd);
//                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while(bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                //Post Data
                if( param != null )
                {
                    Iterator iterator = param.entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + URLEncoder.encode(pair.getKey(), "UTF-8") + "\"" + lineEnd);
                        outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                        outputStream.writeBytes(lineEnd);
                        outputStream.writeBytes(URLEncoder.encode(pair.getValue(), "UTF-8"));
                        outputStream.writeBytes(lineEnd);
                    }
                }

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                int responseCode = http_conn.getResponseCode();

                if( responseCode == HttpURLConnection.HTTP_OK )
                {
                    resultado = inputStreamToJSONObject( http_conn.getInputStream() );
                    if( resultado != null ) {
                        Log.i("ResString", resultado.toString());
                        resultado.put("consulta", "OK");//Se realizó la consulta al sevidor
                    }else{
                        resultado = new JSONObject();
                        resultado.put("consulta", "ERROR");//Se realizó la consulta al sevidor
                        resultado.put("code",104);
                        resultado.put("msg","inputStreamToJSONObject( http_conn.getInputStream() ) retorno null");
                    }
                }else
                {
                    resultado.put("consulta", "ERROR" );//Error en la consulta
                    resultado.put("code", responseCode );
                }

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
            }

        }catch( MalformedURLException e )
        {
            Log.e("CE-MURLException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",101);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( IOException e )
        {
            Log.e("CE-IOException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",102); //Error en el proceso, TimeOut
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }catch( JSONException e ) {
            Log.e("CE-JSONException", e.toString());
            try
            {
                resultado.put("consulta", "ERROR");//No se realizó la consulta
                resultado.put("code",103);
            }catch( JSONException e1 ) {
                e1.printStackTrace();
            }
        }finally {
            if( http_conn != null )
            {
                http_conn.disconnect();
            }
        }
        return resultado;
    }//ejecutarUploadFile

    public JSONObject inputStreamToJSONObject( InputStream inputStream )
    {
        JSONObject json = null;
        try
        {
            BufferedReader br = new BufferedReader( new InputStreamReader( inputStream ) );
            StringBuilder builder = new StringBuilder();
            String line;
            while( (line = br.readLine() ) != null )
            {
                builder.append( line );
            }
            br.close();

            json = new JSONObject( builder.toString() );

        }catch (JSONException e)
        {
            Log.e("CE-ISTJException", e.toString() );
        }catch (IOException e)
        {
            Log.e("CE-ISTJIOEx", e.toString() );
        }
        return json;
    }

    public String getParametrosMap( HashMap<String,String> param )
    {
        StringBuilder builder = new StringBuilder();
        try {

            Iterator iterator = param.entrySet().iterator();
            boolean first = true;
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                if (first) {
                    first = false;
                } else {
                    builder.append("&");
                }
                builder.append( URLEncoder.encode(pair.getKey(), "UTF-8") );
                builder.append("=");
                builder.append( URLEncoder.encode( pair.getValue(), "UTF-8" ) );
            }
        }catch( UnsupportedEncodingException e)
        {
            Log.e("CE-GPMEx", e.toString() );
        }

        return builder.toString();
    }
}
