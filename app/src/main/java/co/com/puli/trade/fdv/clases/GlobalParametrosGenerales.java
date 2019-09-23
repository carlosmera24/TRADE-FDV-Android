package co.com.puli.trade.fdv.clases;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase utilizada de forma glogal para almacenar los parámetros generales descargados desde la BBDD
 * los cuales podrán ser accedidos por cualquier actividad
 * Created by carlos on 29/11/16.
 */

public class GlobalParametrosGenerales extends Application
{
    private HashMap<String,ParametroGeneral> parametros = null;
    private ArrayList<RutaGrama> rutaGramas = null;
    private ArrayList<RutaGrama> rutagramasDescartar = new ArrayList<>();
    private Activity actividadActual = null;
    private JSONObject empresa;
    private String fecha_inspeccion = null;
    private ArrayList<PDV> pdvCheckIn = new ArrayList<>();

    /**
     * Método encargado de inicializar los parametros generales a partir del JSONArray de la BD
     * */
    public void setParametros( JSONArray param )
    {
        try
        {
            parametros = new HashMap<>();
            for (int i = 0; i < param.length(); i++) {
                JSONObject tmp = param.getJSONObject(i);
                ParametroGeneral parametro = new ParametroGeneral( tmp.getString("id"), tmp.getString("key"), tmp.getString("var1_varchar"), tmp.getString("var2_text"),
                                                                    tmp.getString("var3_int"), tmp.getString("var4_decimal"), tmp.getString("var5_datetime") );
                parametros.put ( tmp.getString("key"), parametro );
            }
        }catch(JSONException e )
        {
            Log.e("JSONException","GlobalParametrosGenerales.setParametros.JSONException:"+ e.toString() );
        }
    }

    /**
     * Método encargadao de retonar el valor a partir de la Key del paramétro y la variable requerida
     * @param key Del paŕametro requerido (Identificador único)
     * @param var 0:ID | 1:Var1 Varchar | 2: Var2 Text | 3:Var3 Int | 4:Var4 Decimal | 5:Var5 DateTime | 6:Key
     * */
    public String getValue( String key, int var )
    {
        if( parametros.containsKey( key ) )
        {
            ParametroGeneral param = parametros.get( key );
            switch( var )
            {
                case 0: //ID
                        return param.getId();
                case 1: //Var1 Varchar
                    return param.getVar1();
                case 2: //Var2 Text
                    return param.getVar2();
                case 3: //Var3 Int
                    return param.getVar3();
                case 4: //Var4 Decimal
                    return param.getVar4();
                case 5: //Var5 DateTime
                    return param.getVar5();
                case 6: //Key
                    return param.getKey();
                default:
                    return null;
            }
        }else{
            return null;
        }
    }

    /**
     * Método encargado de validar si existen parametros definidos
     * @return boolean
     * */
    public boolean existenParametros()
    {
        return parametros != null ? true : false;
    }

    /**
     * Método encargado de inicializar las RutaGramas a partir del JSONArray de la BD
     * */
    public void setRutaGramas( JSONArray json_rutagramas)
    {
        try
        {
            rutaGramas = new ArrayList<>();
            for (int i = 0; i < json_rutagramas.length(); i++)
            {
                JSONObject tmp = json_rutagramas.getJSONObject(i);
                RutaGrama rg = new RutaGrama( tmp.getString("id"), tmp.getString("id_tipo"), tmp.getString("direccion"), tmp.getString("lat"), tmp.getString("lng") );
                rutaGramas.add( rg );
            }
        }catch(JSONException e )
        {
            Log.e("JSONException","GlobalParametrosGenerales.setRutaGramas.JSONException:"+ e.toString() );
        }
    }

    public boolean existenRutaGramas()
    {
        return rutaGramas != null ? true : false;
    }

    public ArrayList<RutaGrama> getRutaGramas() {
        return rutaGramas;
    }

    /**
     * Método encargado de agregar RutaGrama para ser descartado de notificaciones
     * @param grama Objecto RutaGrama
     * */
    public void descartarRutaGrama( RutaGrama grama )
    {
        if( !rutagramasDescartar.contains( grama ) ) {
            rutagramasDescartar.add(grama);
        }
    }

    public void limpiarRutaGramaDescarte()
    {
        rutagramasDescartar = new ArrayList<>();
    }

    public ArrayList<RutaGrama> getRutagramasDescartar() {
        return rutagramasDescartar;
    }

    /**
     * Método encargado de registrar la actividad actual en ejecución
     * Deberá registrarse desde cada actividad visible para maneter un óptimo funcionamiento
     * */
    public void setActividadActual(Activity actividadActual) {
        this.actividadActual = actividadActual;
    }

    /**
     * Retorna la actividad actual en ejecución
     * */
    public Activity getActividadActual() {
        return actividadActual;
    }

    /**
     * Retorna los datos de la empresa
     * {id,nombre,lat,lng}
     * */
    public JSONObject getEmpresa() {
        return empresa;
    }

    /**
     * Asginar los datos de la empresa descargados desde la BBDD
     * */
    public void setEmpresa(JSONObject empresa) {
        this.empresa = empresa;
    }

    /**
     * Método encargado de retornar la última fecha registrada para el vehículo asociado
     * @return String con fecha en formato yyyy-mm-dd
     * */
    public String getFecha_inspeccion() {
        return fecha_inspeccion;
    }

    public void setFecha_inspeccion(String fecha_inspeccion) {
        this.fecha_inspeccion = fecha_inspeccion;
    }

    /**
     * Método encargado de obtener el listado de PDV con CheckIn
     * */
    public ArrayList<PDV> getPdvCheckIn() {
        return pdvCheckIn;
    }

    /**
     * Método encargado de agregar PDV al listado de CheckIn
     * */
    public void agregarPDVCheckIn( PDV pdv )
    {
        if( !pdvCheckIn.contains( pdv ) )
        {
            pdvCheckIn.add( pdv );
        }
    }

    /**
     * Método encargado de limpiar el listado de PDV con CheckIn
     * */
    public void limpiarPDVCheckIn(){
        pdvCheckIn = new ArrayList<>();
    }
}
