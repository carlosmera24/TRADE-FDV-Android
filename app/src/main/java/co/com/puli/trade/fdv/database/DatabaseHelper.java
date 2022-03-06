package co.com.puli.trade.fdv.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import co.com.puli.trade.fdv.clases.RutaGrama;
import co.com.puli.trade.fdv.database.DatabaseContract.*;
import co.com.puli.trade.fdv.database.models.Colegio;
import co.com.puli.trade.fdv.database.models.Inspeccion;
import co.com.puli.trade.fdv.database.models.MvtoCheck;
import co.com.puli.trade.fdv.database.models.MvtoFinRuta;
import co.com.puli.trade.fdv.database.models.MvtoInicioRuta;
import co.com.puli.trade.fdv.database.models.MvtoKilometros;
import co.com.puli.trade.fdv.database.models.MvtoRastreo;
import co.com.puli.trade.fdv.database.models.ParametroGeneral;
import co.com.puli.trade.fdv.database.models.RutaAlumno;
import co.com.puli.trade.fdv.database.models.TerminoAndCondition;
import co.com.puli.trade.fdv.database.models.TipoInspeccion;
import co.com.puli.trade.fdv.database.models.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by carlos on 13/11/21
 * Estructura y manejo de la creación, actualización y demás funcionalidades de la Base de datos
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "PuliTransportadores.db";

    /*-----Registros para crear tablas------*/
    //Crear tabla usuario
    private static final String CREATE_TABLE_USER = "CREATE TABLE "+ UserEntry.TABLE_NAME +
            "("+ UserEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            UserEntry.COLUMN_ID_USER +" TEXT," +
            UserEntry.COLUMN_ID_PERFIL +" INTEGER," +
            UserEntry.COLUMN_USER +" TEXT," +
            UserEntry.COLUMN_ID_CONDUCTOR +" TEXT," +
            UserEntry.COLUMN_ID_FDV +" TEXT," +
            UserEntry.COLUMN_ID_RUTA +" TEXT," +
            UserEntry.COLUMN_NOMBRE_USUARIO +" TEXT," +
            UserEntry.COLUMN_TOKEN +" TEXT," +
            UserEntry.COLUMN_IMAGEN +" TEXT," +
            UserEntry.COLUMN_STATUS_RUTA +" INTEGER)";

    //Crear tabla parametro General
    private static final String CREATE_TABLE_PARAMETRO_GENERAL = "CREATE TABLE "+ ParametroGeneralEntry.TABLE_NAME +
            "("+ ParametroGeneralEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            ParametroGeneralEntry.COLUMN_NAME +" TEXT," +
            ParametroGeneralEntry.COLUMN_VAR1 +" TEXT," +
            ParametroGeneralEntry.COLUMN_VAR2 +" TEXT," +
            ParametroGeneralEntry.COLUMN_VAR3 +" TEXT," +
            ParametroGeneralEntry.COLUMN_VAR4 +" TEXT," +
            ParametroGeneralEntry.COLUMN_VAR5 +" TEXT)";

    //Crear tabla Ruta Grama
    private static final String CREATE_TABLE_RUTA_GRAMA = "CREATE TABLE "+ RutaGramaEntry.TABLE_NAME +
            "("+ RutaGramaEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            RutaGramaEntry.COLUMN_ID_TIPO +" INTEGER," +
            RutaGramaEntry.COLUMN_DIRECCION +" TEXT," +
            RutaGramaEntry.COLUMN_LAT +" NUMERIC," +
            RutaGramaEntry.COLUMN_LNG +" NUMERIC)";

    //Crear tabla Terminos y condiciones
    private static final String CREATE_TABLE_TERMINOS_CONDICIONES = "CREATE TABLE "+ TerminosCondicionesEntry.TABLE_NAME +
            "("+ TerminosCondicionesEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            TerminosCondicionesEntry.COLUMN_ESTADO +" TEXT," +
            TerminosCondicionesEntry.COLUMN_MENSAJE +" TEXT," +
            TerminosCondicionesEntry.COLUMN_CODIGO_USUARIO +" TEXT)";

    //Creat tabla colegio
    private static final String CREATE_TABLE_COLEGIO = "CREATE TABLE "+ ColegioEntry.TABLE_NAME +
            "("+ ColegioEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            ColegioEntry.COLUMN_ID +" INTEGER," +
            ColegioEntry.COLUMN_NOMBRE +" TEXT," +
            ColegioEntry.COLUMN_LAT +" NUMERIC," +
            ColegioEntry.COLUMN_LNG +" NUMERIC)";

    //Ceate table inspeccion
    private static final String CREATE_TABLE_INSPECCION = "CREATE TABLE "+ InspeccionEntry.TABLE_NAME +
            "("+ InspeccionEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            InspeccionEntry.COLUMN_ID_TIPO_INSPECCION +" INTEGER," +
            InspeccionEntry.COLUMN_ID_VEHICULO +" TEXT," +
            InspeccionEntry.COLUMN_ID_CONDUCTOR +" TEXT," +
            InspeccionEntry.COLUMN_FECHA +" TEXT," +
            InspeccionEntry.COLUMN_FECHA_DATE +" TEXT," +
            InspeccionEntry.COLUMN_RESULTADO +" TEXT)";

    //Create table tipo_inspeccion
    private static final String CREATE_TABLE_MVTO_KILOMETROS = "CREATE TABLE "+ MvtoKilometrosEntry.TABLE_NAME +
            "("+ MvtoKilometrosEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            MvtoKilometrosEntry.COLUMN_VALUE_KM +" TEXT," +
            MvtoKilometrosEntry.COLUMN_ID_VEHICULO +" TEXT," +
            MvtoKilometrosEntry.COLUMN_ID_CONDUCTOR +" TEXT," +
            MvtoKilometrosEntry.COLUMN_FECHA +" TEXT)";

    //Create table tipo_inspeccion
    private static final String CREATE_TABLE_TIPO_INSPECCION = "CREATE TABLE "+ TipoInspeccionEntry.TABLE_NAME +
            "("+ TipoInspeccionEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            TipoInspeccionEntry.COLUMN_DESCRIPTION +" TEXT)";

    //Create table mvto_inicio_ruta
    private static final String CREATE_TABLE_MVTO_INICIO_RUTA = "CREATE TABLE "+ MvtoInicioRutaEntry.TABLE_NAME +
            "("+ MvtoInicioRutaEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            MvtoInicioRutaEntry.COLUMN_ID_RUTA +" TEXT," +
            MvtoInicioRutaEntry.COLUMN_FECHA +" TEXT," +
            MvtoInicioRutaEntry.COLUMN_LAT +" NUMERIC," +
            MvtoInicioRutaEntry.COLUMN_LNG +" NUMERIC)";

    //Create table mvto_fin_ruta
    private static final String CREATE_TABLE_MVTO_FIN_RUTA = "CREATE TABLE "+ MvtoFinRutaEntry.TABLE_NAME +
            "("+ MvtoFinRutaEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            MvtoFinRutaEntry.COLUMN_ID_RUTA +" TEXT," +
            MvtoFinRutaEntry.COLUMN_FECHA +" TEXT," +
            MvtoFinRutaEntry.COLUMN_LAT +" NUMERIC," +
            MvtoFinRutaEntry.COLUMN_LNG +" NUMERIC)";

    //Create table rutas alumnos
    private static final String CREATE_TABLE_RUTAS_ALUMNOS = "CREATE TABLE "+ RutasAlumnosEntry.TABLE_NAME +
            "("+ RutasAlumnosEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            RutasAlumnosEntry.COLUMN_ID_ALUMNO +" TEXT," +
            RutasAlumnosEntry.COLUMN_ID_RUTA +" TEXT," +
            RutasAlumnosEntry.COLUMN_ID_RUTA_VEHICULO +" TEXT," +
            RutasAlumnosEntry.COLUMN_ID_VEHICULO +" TEXT," +
            RutasAlumnosEntry.COLUMN_ID_CONDUCTOR +" TEXT," +
            RutasAlumnosEntry.COLUMN_DESCRIPCION_RUTA +" TEXT," +
            RutasAlumnosEntry.COLUMN_ID_MONITOR +" TEXT," +
            RutasAlumnosEntry.COLUMN_NOMBRE +" TEXT," +
            RutasAlumnosEntry.COLUMN_APELLIDO +" TEXT," +
            RutasAlumnosEntry.COLUMN_ESTADO_IN +" INTEGER," +
            RutasAlumnosEntry.COLUMN_ESTADO_OUT +" INTEGER," +
            RutasAlumnosEntry.COLUMN_ESTADO_AUSENTE +" INTEGER," +
            RutasAlumnosEntry.COLUMN_ORDEN +" INTEGER)";

    //Create table MvtoCheck
    private static final String CREATE_TABLE_MVTO_CHECKIN = "CREATE TABLE "+ MvtoCheckEntry.TABLE_NAME +
            "("+ MvtoCheckEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            MvtoCheckEntry.COLUMN_LAT +" NUMERIC," +
            MvtoCheckEntry.COLUMN_LNG +" NUMERIC," +
            MvtoCheckEntry.COLUMN_FECHA +" TEXT," +
            MvtoCheckEntry.COLUMN_ID_VEHICULO +" TEXT," +
            MvtoCheckEntry.COLUMN_ID_VEHICULO_RUTA +" TEXT," +
            MvtoCheckEntry.COLUMN_ID_ALUMNO +" TEXT," +
            MvtoCheckEntry.COLUMN_TIPO_CHECKIN +" INTEGER)";

    //Create table MvtoRastro
    private static final String CREATE_TABLE_MVTO_RASTREO = "CREATE TABLE "+ MvtoRastreoEntry.TABLE_NAME +
            "("+ MvtoRastreoEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            MvtoRastreoEntry.COLUMN_LAT +" NUMERIC," +
            MvtoRastreoEntry.COLUMN_LNG +" NUMERIC," +
            MvtoRastreoEntry.COLUMN_FECHA +" TEXT," +
            MvtoRastreoEntry.COLUMN_ID_RUTA +" INTEGER)";

    //Create table RutasUsuario
    private static final String CREATE_TABLE_RUTAS_USUARIO = "CREATE TABLE "+ RutasUsuarioEntry.TABLE_NAME +
            "("+ RutasUsuarioEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
            RutasUsuarioEntry.COLUMN_RUTA_CODIGO +" TEXT," +
            RutasUsuarioEntry.COLUMN_DESCRIPCION +" TEXT)";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Crear tablas requeridas
        db.execSQL( CREATE_TABLE_USER );
        db.execSQL( CREATE_TABLE_PARAMETRO_GENERAL );
        db.execSQL( CREATE_TABLE_RUTA_GRAMA );
        db.execSQL( CREATE_TABLE_TERMINOS_CONDICIONES );
        db.execSQL( CREATE_TABLE_COLEGIO );
        db.execSQL( CREATE_TABLE_INSPECCION );
        db.execSQL( CREATE_TABLE_MVTO_KILOMETROS );
        db.execSQL( CREATE_TABLE_TIPO_INSPECCION );
        db.execSQL( CREATE_TABLE_MVTO_INICIO_RUTA );
        db.execSQL( CREATE_TABLE_MVTO_FIN_RUTA );
        db.execSQL( CREATE_TABLE_RUTAS_ALUMNOS );
        db.execSQL( CREATE_TABLE_MVTO_CHECKIN );
        db.execSQL( CREATE_TABLE_MVTO_RASTREO );
        db.execSQL( CREATE_TABLE_RUTAS_USUARIO );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Eliminar las tablas
        db.execSQL("DROP TABLE IF EXISTS "+ UserEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ ParametroGeneralEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ RutaGramaEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ TerminosCondicionesEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ ColegioEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ InspeccionEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ MvtoKilometrosEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ TipoInspeccionEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ MvtoInicioRutaEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ MvtoFinRutaEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ RutasAlumnosEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ MvtoCheckEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ MvtoRastreoEntry.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS "+ RutasUsuarioEntry.TABLE_NAME );

        //Crear nuevas tablas
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Busca el usuario principal, retorna null si no hay datos
     * @return Usuario
     */
    public Usuario getUsuario()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                UserEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                UserEntry._ID + " ASC");

        if( cursor.moveToFirst() )
        {
            Usuario usuario = new Usuario(
                    cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_ID_USER)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USER)),
                    cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_ID_PERFIL)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_ID_CONDUCTOR)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_ID_FDV)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_ID_RUTA)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_NOMBRE_USUARIO)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_TOKEN)),
                    cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_IMAGEN))
            );
            cursor.close();
            database.close();

            return usuario;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Registra los datos del usuario
     * @param usuario Usuario a registrar
     * @return long  ID de la fila recién creada o -1 si hubo un error al insertar los datos
     */
    public long setUsuario( Usuario usuario )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_ID_USER, usuario.getId() );
        values.put(UserEntry.COLUMN_ID_PERFIL, usuario.getId_perfil() );
        values.put(UserEntry.COLUMN_USER, usuario.getUsuario() );
        values.put(UserEntry.COLUMN_ID_CONDUCTOR, usuario.getId_conductor());
        values.put(UserEntry.COLUMN_ID_FDV, usuario.getId_fdv());
        values.put(UserEntry.COLUMN_ID_RUTA, usuario.getId_ruta());
        values.put(UserEntry.COLUMN_NOMBRE_USUARIO, usuario.getNombre_usuario());
        values.put(UserEntry.COLUMN_TOKEN, usuario.getToken());
        values.put(UserEntry.COLUMN_IMAGEN, usuario.getImagen());

        long id = database.insert( UserEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Registrar los datos para los parámetros a partir de respuesta JSON
     * @param param JSONArray
     * @return ids registrados {@link List<Long>}
     */
    public List<Long> setParametros(JSONArray param )
    {
        List<Long> ids = new ArrayList<>();
        try
        {
            SQLiteDatabase database = this.getWritableDatabase();
            for (int i = 0; i < param.length(); i++)
            {
                JSONObject tmp = param.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(ParametroGeneralEntry._ID, tmp.getInt("id") );
                values.put(ParametroGeneralEntry.COLUMN_NAME, tmp.getString("key"));
                values.put(ParametroGeneralEntry.COLUMN_VAR1, tmp.getString("var1_varchar"));
                values.put(ParametroGeneralEntry.COLUMN_VAR2,tmp.getString("var2_text"));
                values.put(ParametroGeneralEntry.COLUMN_VAR3,tmp.getString("var3_int"));
                values.put(ParametroGeneralEntry.COLUMN_VAR4,tmp.getString("var4_decimal"));
                values.put(ParametroGeneralEntry.COLUMN_VAR5,tmp.getString("var5_datetime") );
                long id = database.insert(ParametroGeneralEntry.TABLE_NAME,null, values);
                if( id > 0 )
                {
                    ids.add( id );
                }
            }
            database.close();
        }catch(JSONException e )
        {
            Log.e("JSONException","DatabaseHelper.setParametros.JSONException:"+ e.toString() );
        }
        return ids;
    }

    /**
     * Método encargadao de retonar el valor a partir de la Key del paramétro y la variable requerida
     * @param key Del paŕametro requerido (Identificador único)
     * @param var 0:ID | 1:Var1 Varchar | 2: Var2 Text | 3:Var3 Int | 4:Var4 Decimal | 5:Var5 DateTime | 6:Key
     * */
    public String getValue( String key, int var )
    {
        SQLiteDatabase database = this.getReadableDatabase();
        String selection = ParametroGeneralEntry.COLUMN_NAME +"= ?";
        String[] selectionArgs = { key };
        Cursor cursor = database.query(
                ParametroGeneralEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                UserEntry._ID + " ASC");

        if( cursor.moveToFirst() )
        {
            ParametroGeneral parametroGeneral = new ParametroGeneral(
                    cursor.getInt(cursor.getColumnIndex(ParametroGeneralEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_VAR1)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_VAR2)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_VAR3)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_VAR4)),
                    cursor.getString(cursor.getColumnIndex(ParametroGeneralEntry.COLUMN_VAR5))
                    );

            cursor.close();
            database.close();

            switch( var )
            {
                case 0: //ID
                    return parametroGeneral.getId() +"";
                case 1: //Var1 Varchar
                    return parametroGeneral.getVar1();
                case 2: //Var2 Text
                    return parametroGeneral.getVar2();
                case 3: //Var3 Int
                    return parametroGeneral.getVar3();
                case 4: //Var4 Decimal
                    return parametroGeneral.getVar4();
                case 5: //Var5 DateTime
                    return parametroGeneral.getVar5();
                case 6: //Key
                    return parametroGeneral.getKey();
                default:
                    return null;
            }
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Método encargado de inicializar las RutaGramas a partir del JSONArray de la BD
     * @param json_rutagramas {@link JSONArray}
     * @return ids {@link List<Long>} con los IDs registrados
     * */
    public List<Long> setRutaGramas( JSONArray json_rutagramas)
    {
        List<Long> ids = new ArrayList<>();
        try
        {
            SQLiteDatabase database = this.getWritableDatabase();
            for (int i = 0; i < json_rutagramas.length(); i++)
            {
                JSONObject tmp = json_rutagramas.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(RutaGramaEntry._ID,tmp.getString("id"));
                values.put(RutaGramaEntry.COLUMN_ID_TIPO, tmp.getString("id_tipo"));
                values.put(RutaGramaEntry.COLUMN_DIRECCION,tmp.getString("direccion"));
                values.put(RutaGramaEntry.COLUMN_LAT,tmp.getString("lat"));
                values.put(RutaGramaEntry.COLUMN_LNG,tmp.getString("lng") );
                long id = database.insert(RutaGramaEntry.TABLE_NAME,null, values);
                if( id > 0 )
                {
                    ids.add( id );
                }
            }
            database.close();
        }catch(JSONException e )
        {
            Log.e("JSONException","DatabaseHelper.setRutaGramas.JSONException:"+ e.toString() );
        }
        return ids;
    }

    /**
     * Retorna el listado de las rutas grama disponibles para el usuario
     * @return ArrayList<{@link RouteModelList}>
     */
    public ArrayList<RutaGrama> getListRutasGramas()
    {
        ArrayList<RutaGrama> rutas = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                RutaGramaEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        while( cursor.moveToNext() )
        {
            rutas.add( new RutaGrama(
                    cursor.getString( cursor.getColumnIndex( RutaGramaEntry._ID)),
                    cursor.getString( cursor.getColumnIndex( RutaGramaEntry.COLUMN_ID_TIPO)),
                    cursor.getString( cursor.getColumnIndex( RutaGramaEntry.COLUMN_DIRECCION)),
                    cursor.getString( cursor.getColumnIndex( RutaGramaEntry.COLUMN_LAT)),
                    cursor.getString( cursor.getColumnIndex( RutaGramaEntry.COLUMN_LNG))
            )  );
        }

        cursor.close();
        database.close();

        return rutas;
    }

    /**
     * Registra los datos de los terminos y condiciones
     * @param terminos {@link TerminoAndCondition}
     * @return long  ID de la fila recién creada o -1 si hubo un error al insertar los datos
     */
    public long setTerminosCondiciones(TerminoAndCondition terminos)
    {
        SQLiteDatabase database = this.getWritableDatabase();//agregar
        ContentValues values = new ContentValues();
        if( terminos.getId() > 0 )
        {
            values.put(TerminosCondicionesEntry._ID, terminos.getId());
        }

        values.put(TerminosCondicionesEntry.COLUMN_ESTADO, terminos.getEstado());
        values.put(TerminosCondicionesEntry.COLUMN_MENSAJE, terminos.getMensage());
        values.put(TerminosCondicionesEntry.COLUMN_CODIGO_USUARIO, terminos.getCodigoUsuario());

        long id = database.insert( TerminosCondicionesEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Retorna los datos del colegio
     * @return terminos {@link TerminoAndCondition}
     */
    public TerminoAndCondition getTerminosCondiciones()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                TerminosCondicionesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        if( cursor.moveToFirst() )
        {
            TerminoAndCondition terminos = new TerminoAndCondition(
                    cursor.getInt(cursor.getColumnIndex(TerminosCondicionesEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(TerminosCondicionesEntry.COLUMN_ESTADO)),
                    cursor.getString(cursor.getColumnIndex(TerminosCondicionesEntry.COLUMN_MENSAJE)),
                    cursor.getString(cursor.getColumnIndex(TerminosCondicionesEntry.COLUMN_CODIGO_USUARIO))
            );
            cursor.close();
            database.close();

            return terminos;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Actualizar los datos de los terminos y condiciones
     * @param terminos {@link TerminoAndCondition}
     * @return int filas actualizadas
     */
    public int updateTerminosCondiciones(TerminoAndCondition terminos)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TerminosCondicionesEntry.COLUMN_ESTADO, terminos.getEstado());
        values.put(TerminosCondicionesEntry.COLUMN_MENSAJE, terminos.getMensage());

        int rows = database.update(
                TerminosCondicionesEntry.TABLE_NAME,
                values,
                TerminosCondicionesEntry._ID +"=?",
                new String[]{ ""+terminos.getId() }
        );
        database.close();
        return rows;
    }

    /**
     * Registra los datos del usuario
     * @param colegio {@link Colegio}
     * @return long  ID de la fila recién creada o -1 si hubo un error al insertar los datos
     */
    public long setColegio(Colegio colegio)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColegioEntry.COLUMN_ID, colegio.getId());
        values.put(ColegioEntry.COLUMN_NOMBRE, colegio.getNombre());
        values.put(ColegioEntry.COLUMN_LAT, colegio.getLat());
        values.put(ColegioEntry.COLUMN_LNG, colegio.getLng());

        long id = database.insert( ColegioEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Retorna los datos del colegio
     * @return colegio {@link Colegio}
     */
    public Colegio getColegio()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                ColegioEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        if( cursor.moveToFirst() )
        {
            Colegio colegio = new Colegio(
                    cursor.getInt(cursor.getColumnIndex(ColegioEntry.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(ColegioEntry.COLUMN_NOMBRE)),
                    cursor.getDouble(cursor.getColumnIndex(ColegioEntry.COLUMN_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(ColegioEntry.COLUMN_LNG))
            );
            cursor.close();
            database.close();

            return colegio;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Registrar datos de la última inspección
     * @param inspeccion Inspeccion
     * @return log ID de la fila creada o -1 si hubo un error al insertar los datos
     */
    public long setInspeccion(Inspeccion inspeccion)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InspeccionEntry.COLUMN_ID_TIPO_INSPECCION, inspeccion.getId_tipo_inspeccion());
        values.put(InspeccionEntry.COLUMN_ID_VEHICULO, inspeccion.getId_vehiculo());
        values.put(InspeccionEntry.COLUMN_ID_CONDUCTOR, inspeccion.getId_conductor());
        values.put(InspeccionEntry.COLUMN_FECHA, inspeccion.getFecha() );
        values.put(InspeccionEntry.COLUMN_FECHA_DATE, inspeccion.getFecha_date());
        values.put(InspeccionEntry.COLUMN_RESULTADO, inspeccion.getResultado());

        long id = database.insert( InspeccionEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Retornar los datos de la ultima inspección realizada
     */
    public Inspeccion getInspeccion()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                InspeccionEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                InspeccionEntry.COLUMN_FECHA +" DESC");

        if( cursor.moveToFirst() )
        {
            Inspeccion inspeccion = new Inspeccion(
                    cursor.getInt(cursor.getColumnIndex(InspeccionEntry._ID)),
                    cursor.getInt(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_TIPO_INSPECCION)),
                    cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_VEHICULO)),
                    cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_CONDUCTOR)),
                    cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_FECHA)),
                    cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_FECHA_DATE)),
                    cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_RESULTADO))
            );
            cursor.close();
            database.close();

            return inspeccion;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Registra los datos del moviento de kilometros
     * @param mvtoKilometros {@link MvtoKilometros}
     */
    public long setMvtoKilometros(MvtoKilometros mvtoKilometros)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if( mvtoKilometros.getId() > 0 )
        {
            values.put(MvtoKilometrosEntry._ID, mvtoKilometros.getId() );
        }
        values.put(MvtoKilometrosEntry.COLUMN_VALUE_KM, mvtoKilometros.getKm() );
        values.put(MvtoKilometrosEntry.COLUMN_ID_VEHICULO, mvtoKilometros.getId_vehiculo() );
        values.put(MvtoKilometrosEntry.COLUMN_ID_CONDUCTOR, mvtoKilometros.getId_conductor());
        if( mvtoKilometros.getFecha() != null ){
            values.put(MvtoKilometrosEntry.COLUMN_FECHA, mvtoKilometros.getFecha());
        }

        long id = database.insert( MvtoKilometrosEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Retornar el valor del movimiento kilomentros, en la BBDD siempre habrá uno solo
     * @return mvtoKilometro
     */
    public MvtoKilometros getMvtoKilometro()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                MvtoKilometrosEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MvtoKilometrosEntry.COLUMN_FECHA +" DESC");

        if( cursor.moveToFirst() )
        {
            MvtoKilometros km = new MvtoKilometros(
                    cursor.getInt(cursor.getColumnIndex(MvtoKilometrosEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_VALUE_KM)),
                    cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_ID_VEHICULO)),
                    cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_ID_CONDUCTOR)),
                    cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_FECHA))
            );
            cursor.close();
            database.close();

            return km;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Registr los tipos de inspección
     * @param json_Inspeccion {@link JSONArray}
     * @return ids {@link ArrayList<Long>}
     */
    public List<Long> setTiposInspeccion( JSONArray json_Inspeccion )
    {
        List<Long> ids = new ArrayList<>();
        try
        {
            SQLiteDatabase database = this.getWritableDatabase();
            for (int i = 0; i < json_Inspeccion.length(); i++)
            {
                JSONObject tmp = json_Inspeccion.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(TipoInspeccionEntry._ID,tmp.getString("id"));
                values.put(TipoInspeccionEntry.COLUMN_DESCRIPTION, tmp.getString("desc"));
                long id = database.insert(TipoInspeccionEntry.TABLE_NAME,null, values);
                if( id > 0 )
                {
                    ids.add( id );
                }
            }
            database.close();
        }catch(JSONException e )
        {
            Log.e("JSONException","DatabaseHelper.setRutaGramas.JSONException:"+ e.toString() );
        }
        return ids;
    }

    /**
     * Retorna el listado de los tipos de inspección
     * @return tipos_inspeccion {@link ArrayList<TipoInspeccion>}
     */
    public ArrayList<co.com.puli.trade.fdv.clases.TipoInspeccion> getTiposInspeccionAdapter()
    {
        ArrayList<co.com.puli.trade.fdv.clases.TipoInspeccion> tipos = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                TipoInspeccionEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                TipoInspeccionEntry.COLUMN_DESCRIPTION +" ASC"
        );
        while( cursor.moveToNext() )
        {
            tipos.add( new co.com.puli.trade.fdv.clases.TipoInspeccion(
                    cursor.getInt(cursor.getColumnIndex(TipoInspeccionEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(TipoInspeccionEntry.COLUMN_DESCRIPTION)),
                    3)
            );
        }

        cursor.close();
        database.close();

        return tipos;
    }

    /**
     * Registrar inicio de ruta
     */
    public long setInicioRuta( MvtoInicioRuta mvtoInicioRuta)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if( mvtoInicioRuta.getId() > 0 )
        {
            values.put(MvtoInicioRutaEntry._ID, mvtoInicioRuta.getId() );
        }
        values.put(MvtoInicioRutaEntry.COLUMN_FECHA, mvtoInicioRuta.getFecha() );
        values.put(MvtoInicioRutaEntry.COLUMN_ID_RUTA, mvtoInicioRuta.getId_ruta() );
        values.put(MvtoInicioRutaEntry.COLUMN_LAT, mvtoInicioRuta.getLat());
        values.put(MvtoInicioRutaEntry.COLUMN_LNG, mvtoInicioRuta.getLng());

        long id = database.insert( MvtoInicioRutaEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Actualiza el listado de alumnos de la Rutas del usuario actual
     * 1- Si hay alumnos se eliminan de la base de datos
     * 2- Se agrega el listado de ruta
     * @param rutaAlumnos
     * @return ids {@link ArrayList<Long>}
     */
    public List<Long> updateAlumnosRuta(ArrayList<RutaAlumno> rutaAlumnos)
    {
        List<Long> ids = new ArrayList<>();
        SQLiteDatabase database = this.getWritableDatabase();

       //Limpiar alumnos
        database.delete( RutasAlumnosEntry.TABLE_NAME,
                RutasAlumnosEntry._ID + ">?",
                new String[]{ "0" }
                );
        //Insertar nuevos alumnos
        for( RutaAlumno rutaAlumno : rutaAlumnos )
        {
           ContentValues contentValues = new ContentValues();
           if( rutaAlumno.getId() != 0 )
           {
               contentValues.put( RutasAlumnosEntry._ID, rutaAlumno.getId() );
           }
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_ALUMNO, rutaAlumno.getId_alumno() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_RUTA, rutaAlumno.getId_ruta() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_RUTA_VEHICULO, rutaAlumno.getId_ruta_vehiculo() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_VEHICULO, rutaAlumno.getId_vehiculo() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_CONDUCTOR, rutaAlumno.getId_conductor() );
            contentValues.put( RutasAlumnosEntry.COLUMN_DESCRIPCION_RUTA, rutaAlumno.getId_conductor() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ID_MONITOR, rutaAlumno.getId_monitor() );
            contentValues.put( RutasAlumnosEntry.COLUMN_NOMBRE, rutaAlumno.getNombre() );
            contentValues.put( RutasAlumnosEntry.COLUMN_APELLIDO, rutaAlumno.getApellido() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ESTADO_IN, rutaAlumno.getEstado_in() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ESTADO_OUT, rutaAlumno.getEstado_out() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ESTADO_AUSENTE, rutaAlumno.getEstado_ausente() );
            contentValues.put( RutasAlumnosEntry.COLUMN_ORDEN, rutaAlumno.getOrden() );
            long id = database.insert( RutasAlumnosEntry.TABLE_NAME, null, contentValues );
            if( id > 0 )
            {
                ids.add( id );
            }
        }

        database.close();
        return ids;
    }

    /**
     * Retorna el listado de RutaAlumnos
     * @return ArrayList<RutaAlumno></>
     */
    public ArrayList<co.com.puli.trade.fdv.clases.RutaAlumno> getListAlumnosRuta()
    {
        ArrayList<co.com.puli.trade.fdv.clases.RutaAlumno> alumnos = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(
                RutasAlumnosEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                RutasAlumnosEntry.COLUMN_ORDEN +" ASC"
        );
        while( cursor.moveToNext() )
        {
            co.com.puli.trade.fdv.clases.RutaAlumno rutaAlumno = new co.com.puli.trade.fdv.clases.RutaAlumno(
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_NOMBRE ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_APELLIDO ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_ALUMNO ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_RUTA ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_DESCRIPCION_RUTA ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_RUTA_VEHICULO ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_VEHICULO ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_CONDUCTOR ) ),
                    cursor.getString( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ID_MONITOR ) ),
                    cursor.getInt( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ESTADO_IN ) ),
                    cursor.getInt( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ESTADO_OUT ) ),
                    cursor.getInt( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ESTADO_AUSENTE ) ),
                    cursor.getInt( cursor.getColumnIndex( RutasAlumnosEntry.COLUMN_ORDEN ) )
                    );

            //Consultar fecha inicio de ruta
            String selection = "SELECT MAX("+ MvtoInicioRutaEntry.COLUMN_FECHA +") FROM "+ MvtoInicioRutaEntry.TABLE_NAME
                    +" WHERE "+ MvtoInicioRutaEntry.COLUMN_ID_RUTA +"= ?";
            String selectionArgs[] = new String[]{ rutaAlumno.getIdRuta() };
            String fecha_inicio = "0000-00-00";
            cursor = database.rawQuery(selection, selectionArgs);
            if(cursor.moveToFirst())
            {
                fecha_inicio = cursor.getString( 0 );
            }

            if(fecha_inicio == null )
            {
                fecha_inicio = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            }

            //Consultar CheckIn
            selection = "SELECT COUNT(*) FROM "+ MvtoCheckEntry.TABLE_NAME
                    +" WHERE "+ MvtoCheckEntry.COLUMN_FECHA +">? AND "+ MvtoCheckEntry.COLUMN_TIPO_CHECKIN +"=? " +
                    "AND "+ MvtoCheckEntry.COLUMN_ID_VEHICULO +"=?  AND "+ MvtoCheckEntry.COLUMN_ID_ALUMNO +"=?";
            selectionArgs = new String[]{ fecha_inicio, "0", rutaAlumno.getIdVehiculo(), rutaAlumno.getId() };
            cursor = database.rawQuery(selection, selectionArgs);
            if(cursor.moveToFirst())
            {
                rutaAlumno.setCheckIn( cursor.getInt( 0 ) > 0 ? 1 : 0 );
            }

            //Consultar CheckOut
            selection = "SELECT COUNT(*) FROM "+ MvtoCheckEntry.TABLE_NAME
                    +" WHERE "+ MvtoCheckEntry.COLUMN_FECHA +">? AND "+ MvtoCheckEntry.COLUMN_TIPO_CHECKIN +"=? " +
                    "AND "+ MvtoCheckEntry.COLUMN_ID_VEHICULO +"=?  AND "+ MvtoCheckEntry.COLUMN_ID_ALUMNO +"=?";
            selectionArgs = new String[]{ fecha_inicio, "1", rutaAlumno.getIdVehiculo(), rutaAlumno.getId() };
            cursor = database.rawQuery(selection, selectionArgs);
            if(cursor.moveToFirst())
            {
                rutaAlumno.setCheckOut( cursor.getInt( 0 ) > 0 ? 1 : 0 );
            }

            //Consultar CheckAus
            selection = "SELECT COUNT(*) FROM "+ MvtoCheckEntry.TABLE_NAME
                    +" WHERE "+ MvtoCheckEntry.COLUMN_FECHA +">? AND "+ MvtoCheckEntry.COLUMN_TIPO_CHECKIN +"=? " +
                    "AND "+ MvtoCheckEntry.COLUMN_ID_VEHICULO +"=?  AND "+ MvtoCheckEntry.COLUMN_ID_ALUMNO +"=?";
            selectionArgs = new String[]{ fecha_inicio, "2", rutaAlumno.getIdVehiculo(), rutaAlumno.getId() };
            cursor = database.rawQuery(selection, selectionArgs);
            if(cursor.moveToFirst())
            {
                rutaAlumno.setCheckAusente( cursor.getInt( 0 ) > 0 ? 1 : 0 );
            }

            //Agregar alumno al listado
            alumnos.add( rutaAlumno  );
        }

        cursor.close();
        database.close();

        return alumnos;
    }

    /**
     * Retorna los datos del alumno RutaAlumno
     * @param id_ruta String
     * @param id_alumno String
     * @return RutaAlumno
     */
    public co.com.puli.trade.fdv.clases.RutaAlumno getAlumnoRuta( String id_ruta, String id_alumno)
    {
        SQLiteDatabase database = this.getReadableDatabase();
        String selection = RutasAlumnosEntry.COLUMN_ID_RUTA +"= ? AND "+ RutasAlumnosEntry.COLUMN_ID_ALUMNO +"= ?";
        String[] selectionArgs = { id_ruta, id_alumno };
        Cursor cursor = database.query(
                RutasAlumnosEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                UserEntry._ID + " ASC");

        if( cursor.moveToFirst() )
        {
            co.com.puli.trade.fdv.clases.RutaAlumno alumno = new co.com.puli.trade.fdv.clases.RutaAlumno(
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_NOMBRE)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_APELLIDO)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_ALUMNO)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_RUTA)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_DESCRIPCION_RUTA)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_RUTA_VEHICULO)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_VEHICULO)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_CONDUCTOR)),
                    cursor.getString(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ID_MONITOR)),
                    cursor.getInt(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ESTADO_IN)),
                    cursor.getInt(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ESTADO_OUT)),
                    cursor.getInt(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ESTADO_AUSENTE)),
                    cursor.getInt(cursor.getColumnIndex(RutasAlumnosEntry.COLUMN_ORDEN))
            );

            cursor.close();
            database.close();

            return alumno;
        }

        cursor.close();
        database.close();
        return null;
    }

    /**
     * Registra los datos del movimiento check IN
     * @param mvtoCheck
     * @return
     */
    public long setMvtoCheckIn( MvtoCheck mvtoCheck)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if( mvtoCheck.getId() > 0 )
        {
            values.put(MvtoCheckEntry._ID, mvtoCheck.getId());
        }
        values.put(MvtoCheckEntry.COLUMN_LAT, mvtoCheck.getLat());
        values.put(MvtoCheckEntry.COLUMN_LNG, mvtoCheck.getLng());
        values.put(MvtoCheckEntry.COLUMN_FECHA, mvtoCheck.getFecha());
        values.put(MvtoCheckEntry.COLUMN_ID_VEHICULO, mvtoCheck.getId_vehiculo());
        values.put(MvtoCheckEntry.COLUMN_ID_VEHICULO_RUTA, mvtoCheck.getId_vehiculo_ruta());
        values.put(MvtoCheckEntry.COLUMN_ID_ALUMNO, mvtoCheck.getId_alumno());
        values.put(MvtoCheckEntry.COLUMN_TIPO_CHECKIN, mvtoCheck.getTipo_checkin());

        long id = database.insert( MvtoCheckEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Registrar fin de ruta
     */
    public long setFinRuta( MvtoFinRuta mvtoFinRuta)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if( mvtoFinRuta.getId() > 0 )
        {
            values.put(MvtoInicioRutaEntry._ID, mvtoFinRuta.getId() );
        }
        values.put(MvtoFinRutaEntry.COLUMN_FECHA, mvtoFinRuta.getFecha() );
        values.put(MvtoFinRutaEntry.COLUMN_ID_RUTA, mvtoFinRuta.getId_ruta() );
        values.put(MvtoFinRutaEntry.COLUMN_LAT, mvtoFinRuta.getLat());
        values.put(MvtoFinRutaEntry.COLUMN_LNG, mvtoFinRuta.getLng());

        long id = database.insert( MvtoFinRutaEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Registra los datos del moviento de rastreo
     * @param mvtoRastreo {@link MvtoRastreo}
     */
    public long setMvtoRastreo(MvtoRastreo mvtoRastreo)
    {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if( mvtoRastreo.getId() > 0 )
        {
            values.put(MvtoRastreoEntry._ID, mvtoRastreo.getId() );
        }
        values.put(MvtoRastreoEntry.COLUMN_LAT, mvtoRastreo.getLat() );
        values.put(MvtoRastreoEntry.COLUMN_LNG, mvtoRastreo.getLng() );
        values.put(MvtoRastreoEntry.COLUMN_FECHA, mvtoRastreo.getFecha());
        values.put(MvtoRastreoEntry.COLUMN_ID_RUTA, mvtoRastreo.getId_ruta());

        long id = database.insert( MvtoRastreoEntry.TABLE_NAME, null, values );

        database.close();

        return id;
    }

    /**
     * Método encargado de validar si existen registros por actualizar en el Servidor
     * Verifica si hay datos en la base de datos local para Inspeccion, MvtoKilometros,
     * Inicio/Fin Ruta, ChecksIn/Out, MvtoRastreo
     * @return boolean
     */
    public boolean existsRegisterForUpdate()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        //Inspecciones
        String selection = "SELECT COUNT(*) FROM "+ InspeccionEntry.TABLE_NAME +" WHERE "+
                InspeccionEntry.COLUMN_ID_TIPO_INSPECCION +"> ?";
        String[] selectionArgs = { "0" };
        Cursor cursor = database.rawQuery(selection, selectionArgs);

       if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }

        //Mvto Kilometros
        selection = "SELECT COUNT(*) FROM "+ MvtoKilometrosEntry.TABLE_NAME +" WHERE "+
                MvtoKilometrosEntry.COLUMN_FECHA +"!= ?";
        selectionArgs = new String[]{ "NULL" };
        cursor = database.rawQuery(selection, selectionArgs);

        if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }

        //Inicios ruta
        selection = "SELECT COUNT(*) FROM "+ MvtoInicioRutaEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }

        //Finales ruta
        selection = "SELECT COUNT(*) FROM "+ MvtoFinRutaEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }

        //Checks
        selection = "SELECT COUNT(*) FROM "+ MvtoCheckEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }

        //Movimientos rastreo
        selection = "SELECT COUNT(*) FROM "+ MvtoRastreoEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            int count = cursor.getInt(0);
            if( count > 0 )
            {
                cursor.close();
                database.close();
                return true;
            }
        }


        cursor.close();
        database.close();
        return false;
    }

    /**
     * Método encargado retornar la cantidad de registros por actualizar en el Servidor
     * Verifica si hay datos en la base de datos local para Inspeccion, MvtoKilometros,
     * Inicio/Fin Ruta, ChecksIn/Out, MvtoRastreo
     * @return boolean
     */
    public int getCountForUpdate()
    {
        int registers = 0;
        SQLiteDatabase database = this.getReadableDatabase();

        //Inspecciones
        String selection = "SELECT COUNT(*) FROM "+ InspeccionEntry.TABLE_NAME +" WHERE "+
                InspeccionEntry.COLUMN_ID_TIPO_INSPECCION +"> ?";
        String[] selectionArgs = { "0" };
        Cursor cursor = database.rawQuery(selection, selectionArgs);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }

        //Mvto Kilometros
        selection = "SELECT COUNT(*) FROM "+ MvtoKilometrosEntry.TABLE_NAME +" WHERE "+
                MvtoKilometrosEntry.COLUMN_FECHA +"!= ?";
        selectionArgs = new String[]{ "NULL" };
        cursor = database.rawQuery(selection, selectionArgs);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }

        //Inicios ruta
        selection = "SELECT COUNT(*) FROM "+ MvtoInicioRutaEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }

        //Finales ruta
        selection = "SELECT COUNT(*) FROM "+ MvtoFinRutaEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }

        //Checks
        selection = "SELECT COUNT(*) FROM "+ MvtoCheckEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }

        //Movimientos rastreo
        selection = "SELECT COUNT(*) FROM "+ MvtoRastreoEntry.TABLE_NAME;
        cursor = database.rawQuery(selection, null);

        if( cursor.moveToFirst() )
        {
            registers += cursor.getInt(0);
        }


        cursor.close();
        database.close();
        return registers;
    }

    /**
     * Retorna los registros de inspección que requieren enviarse a actualización
     * @param fecha fecha del Mvto Kilometro a relacionar
     * @return List<Inspeccion>
     */
    public List<Inspeccion> getInspeccionesForUpdate(String fecha)
    {
        List<Inspeccion> inspeccions = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        String selection = InspeccionEntry.COLUMN_ID_TIPO_INSPECCION +"> ? " +
                "AND "+ InspeccionEntry.COLUMN_FECHA +">= ?";
        String[] selectionArgs = { "0", fecha };
        Cursor cursor = database.query(
                InspeccionEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                InspeccionEntry.COLUMN_ID_TIPO_INSPECCION,
                null,
                InspeccionEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            inspeccions.add(
                    new Inspeccion(
                            cursor.getInt(cursor.getColumnIndex(InspeccionEntry._ID)),
                            cursor.getInt(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_TIPO_INSPECCION)),
                            cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_VEHICULO)),
                            cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_ID_CONDUCTOR)),
                            cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_FECHA)),
                            cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_FECHA_DATE)),
                            cursor.getString(cursor.getColumnIndex(InspeccionEntry.COLUMN_RESULTADO))
                    ));
        }

        cursor.close();
        database.close();
        return inspeccions;
    }

    /**
     * Retorna los registros de Mvto Kilometros que requieren enviarse a actualización
     * @return List<Inspeccion>
     */
    public List<MvtoKilometros> getMtoKilometrosForUpdate()
    {
        List<MvtoKilometros> mvtoKilometros = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        String selection = MvtoKilometrosEntry.COLUMN_FECHA +"!= ?";
        String[] selectionArgs = { "NULL" };
        Cursor cursor = database.query(
                MvtoKilometrosEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                MvtoKilometrosEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            mvtoKilometros.add(
                    new MvtoKilometros(
                            cursor.getInt(cursor.getColumnIndex(MvtoKilometrosEntry._ID)),
                            cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_VALUE_KM)),
                            cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_ID_VEHICULO)),
                            cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_ID_CONDUCTOR)),
                            cursor.getString(cursor.getColumnIndex(MvtoKilometrosEntry.COLUMN_FECHA))
                    ));
        }

        cursor.close();
        database.close();
        return mvtoKilometros;
    }

    /**
     * Retorna los registros de Mvto Inicio Ruta que requieren enviarse a actualización
     * @return List<MvtoInicioRuta>
     */
    public List<MvtoInicioRuta> getMvtoIniciosRutaForUpdate()
    {
        List<MvtoInicioRuta> mvtoInicioRutas = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                MvtoInicioRutaEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MvtoInicioRutaEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            mvtoInicioRutas.add(
                    new MvtoInicioRuta(
                            cursor.getInt(cursor.getColumnIndex(MvtoInicioRutaEntry._ID)),
                            cursor.getString(cursor.getColumnIndex(MvtoInicioRutaEntry.COLUMN_FECHA)),
                            cursor.getString(cursor.getColumnIndex(MvtoInicioRutaEntry.COLUMN_ID_RUTA)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoInicioRutaEntry.COLUMN_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoInicioRutaEntry.COLUMN_LNG))
                    ));
        }

        cursor.close();
        database.close();
        return mvtoInicioRutas;
    }

    /**
     * Retorna los registros de Mvto Check Ruta que requieren enviarse a actualización
     * @return List<MvtoCheck>
     */
    public List<MvtoCheck> getMvtoChecksForUpdate()
    {
        List<MvtoCheck> mvtoChecks = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                MvtoCheckEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MvtoCheckEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            mvtoChecks.add(
                    new MvtoCheck(
                            cursor.getInt(cursor.getColumnIndex(MvtoCheckEntry._ID)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_LNG)),
                            cursor.getString(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_FECHA)),
                            cursor.getString(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_ID_VEHICULO)),
                            cursor.getString(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_ID_VEHICULO_RUTA)),
                            cursor.getString(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_ID_ALUMNO)),
                            cursor.getInt(cursor.getColumnIndex(MvtoCheckEntry.COLUMN_TIPO_CHECKIN))
                    ));
        }

        cursor.close();
        database.close();
        return mvtoChecks;
    }

    /**
     * Retorna los registros de Mvto Rastreo Ruta que requieren enviarse a actualización
     * @return List<MvtoRastreo>
     */
    public List<MvtoRastreo> getMvtoRastreoForUpdate()
    {
        List<MvtoRastreo> mvtoRastreos = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                MvtoRastreoEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MvtoRastreoEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            mvtoRastreos.add(
                    new MvtoRastreo(
                            cursor.getInt(cursor.getColumnIndex(MvtoRastreoEntry._ID)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoRastreoEntry.COLUMN_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoRastreoEntry.COLUMN_LNG)),
                            cursor.getString(cursor.getColumnIndex(MvtoRastreoEntry.COLUMN_FECHA)),
                            cursor.getString(cursor.getColumnIndex(MvtoRastreoEntry.COLUMN_ID_RUTA))
                    ));
        }

        cursor.close();
        database.close();
        return mvtoRastreos;
    }

    /**
     * Retorna los registros de Mvto Fin Ruta que requieren enviarse a actualización
     * @return List<MvtoInicioRuta>
     */
    public List<MvtoFinRuta> getMvtoFinalesRutaForUpdate()
    {
        List<MvtoFinRuta> mvtoFinRutas = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                MvtoFinRutaEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MvtoFinRutaEntry.COLUMN_FECHA +" ASC");

        while( cursor.moveToNext() )
        {
            mvtoFinRutas.add(
                    new MvtoFinRuta(
                            cursor.getInt(cursor.getColumnIndex(MvtoFinRutaEntry._ID)),
                            cursor.getString(cursor.getColumnIndex(MvtoFinRutaEntry.COLUMN_FECHA)),
                            cursor.getString(cursor.getColumnIndex(MvtoFinRutaEntry.COLUMN_ID_RUTA)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoFinRutaEntry.COLUMN_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(MvtoFinRutaEntry.COLUMN_LNG))
                    ));
        }

        cursor.close();
        database.close();
        return mvtoFinRutas;
    }

    /**
     * Eliminar todos los mvto kilometros con ID inferior al indicado,
     * De esa manera conservamos el último en la base de datos para control
     * @param id
     * @return int rows
     */
    public int deleteMvoKilometros( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( MvtoKilometrosEntry.TABLE_NAME,
                                MvtoKilometrosEntry._ID + "=?",
                                new String[]{ ""+id }
                            );
        database.close();
        return rows;
    }

    /**
     * Eliminar todos las inspecciones con ID inferior al indicado,
     * De esa manera conservamos el último en la base de datos para control
     * @param id
     * @return int rows
     */
    public int deleteInspecciones( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( InspeccionEntry.TABLE_NAME,
                                InspeccionEntry._ID + "=?",
                                new String[]{ ""+id }
                            );
        database.close();
        return rows;
    }

    /**
     * Eliminar el mvto inicio ruta con ID
     * @param id
     * @return int rows
     */
    public int deleteMvoInicioRuta( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( MvtoInicioRutaEntry.TABLE_NAME,
                MvtoInicioRutaEntry._ID + "=?",
                new String[]{ ""+id }
        );
        database.close();
        return rows;
    }

    /**
     * Eliminar el mvto check con ID
     * @param id
     * @return int rows
     */
    public int deleteMvoCheck( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( MvtoCheckEntry.TABLE_NAME,
                MvtoCheckEntry._ID + "=?",
                new String[]{ ""+id }
        );
        database.close();
        return rows;
    }

    /**
     * Eliminar el mvto rastreo con ID
     * @param id
     * @return int rows
     */
    public int deleteMvoRastreo( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( MvtoRastreoEntry.TABLE_NAME,
                MvtoRastreoEntry._ID + "=?",
                new String[]{ ""+id }
        );
        database.close();
        return rows;
    }

    /**
     * Eliminar el mvto find ruta con ID
     * @param id
     * @return int rows
     */
    public int deleteMvoFinRuta( int id )
    {
        SQLiteDatabase database = this.getWritableDatabase();

        int rows = database.delete( MvtoFinRutaEntry.TABLE_NAME,
                MvtoFinRutaEntry._ID + "=?",
                new String[]{ ""+id }
        );
        database.close();
        return rows;
    }

    /**
     * Limpiar los registros del usuario en la base de datos
     * Solo se eliminan aquellos que se utilizan como fuente de datos
     * Se conservan los movimientos del offline para actualización del servidor
     */
    public void logout()
    {
        SQLiteDatabase database = this.getWritableDatabase();

        //Eliminar usuario
        database.delete( UserEntry.TABLE_NAME,
                UserEntry._ID + ">?",
                new String[]{ "0" }
        );

        //Eliminar parametros generales
        database.delete( ParametroGeneralEntry.TABLE_NAME,
                ParametroGeneralEntry._ID + ">?",
                new String[]{ "0" }
        );

        //Eliminar Rutagramas
        database.delete( RutaGramaEntry.TABLE_NAME,
                RutaGramaEntry._ID + ">?",
                new String[]{ "0" }
        );

        //Eliminar Colegio
        database.delete( ColegioEntry.TABLE_NAME,
                ColegioEntry._ID + ">?",
                new String[]{ "0" }
        );

        //Eliminar tipos inspeccion
        database.delete( TipoInspeccionEntry.TABLE_NAME,
                TipoInspeccionEntry._ID + ">?",
                new String[]{ "0" }
        );

        //Eliminar tipos inspeccion
        database.delete( RutasAlumnosEntry.TABLE_NAME,
                RutasAlumnosEntry._ID + ">?",
                new String[]{ "0" }
        );

        database.close();
    }
}
