package co.com.puli.trade.fdv.database;

import android.provider.BaseColumns;

/**
 * Created by carlos on 13/11/21
 */
public final class DatabaseContract
{
    public DatabaseContract(){}

    public static class UserEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_ID_USER = "id_usuario";
        public static final String COLUMN_USER = "usuario";
        public static final String COLUMN_ID_PERFIL = "id_perfil";
        public static final String COLUMN_ID_CONDUCTOR = "id_conductor";
        public static final String COLUMN_ID_FDV = "id_fdv";
        public static final String COLUMN_ID_RUTA = "id_ruta";
        public static final String COLUMN_NOMBRE_USUARIO = "nombre_usuario";
        public static final String COLUMN_TOKEN = "token";
        public static final String COLUMN_IMAGEN = "imagen";
        public static final String COLUMN_STATUS_RUTA = "status_ruta";
        public static final String COLUMN_EMPRESA = "empresa";
    }

    public static class ParametroGeneralEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "parametro_general";
        public static final String COLUMN_NAME = "name"; //Key
        public static final String COLUMN_VAR1 = "var1";
        public static final String COLUMN_VAR2 = "var2";
        public static final String COLUMN_VAR3 = "var3";
        public static final String COLUMN_VAR4 = "var4";
        public static final String COLUMN_VAR5 = "var5";
    }

    public static class RutaGramaEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "ruta_grama";
        public static final String COLUMN_ID_TIPO = "id_tipo";
        public static final String COLUMN_DIRECCION = "direccion";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
    }

    public static class TerminosCondicionesEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "terminos_condiciones";
        public static final String COLUMN_ESTADO = "estado";
        public static final String COLUMN_MENSAJE = "mensaje";
        public static final String COLUMN_CODIGO_USUARIO = "cogido_usuario";
    }

    public static class InspeccionEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "inspeccion";
        public static final String COLUMN_ID_TIPO_INSPECCION = "id_tipo_inspeccion";
        public static final String COLUMN_ID_FDV = "id_fdv";
        public static final String COLUMN_FECHA = "fecha_datetime";
        public static final String COLUMN_FECHA_DATE = "fecha_date";
        public static final String COLUMN_RESULTADO = "resultado";
    }

    public static class MvtoKilometrosEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "mvto_kilometros";
        public static final String COLUMN_VALUE_KM = "valor_kilometros";
        public static final String COLUMN_ID_VEHICULO = "id_vehiculo";
        public static final String COLUMN_ID_CONDUCTOR = "id_conductor";
        public static final String COLUMN_FECHA = "fecha";
    }

    public static class TipoInspeccionEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "tipo_inspeccion";
        public static final String COLUMN_DESCRIPTION = "descripcion";
    }

    public static class MvtoInicioRutaEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "mvto_inicio_ruta";
        public static final String COLUMN_ID_RUTA = "id_ruta";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
    }

    public static class MvtoFinRutaEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "mvto_fin_ruta";
        public static final String COLUMN_ID_RUTA = "id_ruta";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
    }

    public static class RutasPDVEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "rutas_pdvs";
        public static final String COLUMN_ID_PDV = "id_pdv";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_NOMBRE_CONTACTO = "nombre_contacto";
        public static final String COLUMN_APELLIDO_CONTACTO = "apellido_contacto";
        public static final String COLUMN_DIRECCION = "direccion";
        public static final String COLUMN_TELEFONO = "telefono";
        public static final String COLUMN_CELULAR = "celular";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_ZONA = "zona";
        public static final String COLUMN_ESTADO_IN = "estado_in";
        public static final String COLUMN_ESTADO_OUT = "estado_out";
        public static final String COLUMN_ESTADO_AUSENTE = "estado_ausente";
    }

    public static class MvtoCheckEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "mvto_check";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_ID_PDV = "id_pdv";
        public static final String COLUMN_ID_FDV = "id_fdv";
        public static final String COLUMN_TIPO_CHECKIN = "tipo_checkin";
    }

    public static class MvtoRastreoEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "mvto_rastreo";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_ID_RUTA = "id_ruta";
    }

    public static class RutasUsuarioEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "rutas_usuario";
        public static final String COLUMN_RUTA_CODIGO = "ruta_codigo";
        public static final String COLUMN_DESCRIPCION = "descripcion";
    }
}
