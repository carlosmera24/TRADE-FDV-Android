package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 21/12/21
 */
public class MvtoCheck
{
    private int id;
    private Double lat;
    private Double lng;
    private String fecha;
    private String id_vehiculo;
    private String id_vehiculo_ruta;
    private String id_alumno;
    private int tipo_checkin;

    public MvtoCheck(Double lat, Double lng, String fecha, String id_vehiculo, String id_vehiculo_ruta, String id_alumno, int tipo_checkin) {
        id = 0;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_vehiculo = id_vehiculo;
        this.id_vehiculo_ruta = id_vehiculo_ruta;
        this.id_alumno = id_alumno;
        this.tipo_checkin = tipo_checkin;
    }

    public MvtoCheck(int id, Double lat, Double lng, String fecha, String id_vehiculo, String id_vehiculo_ruta, String id_alumno, int tipo_checkin) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_vehiculo = id_vehiculo;
        this.id_vehiculo_ruta = id_vehiculo_ruta;
        this.id_alumno = id_alumno;
        this.tipo_checkin = tipo_checkin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getId_vehiculo() {
        return id_vehiculo;
    }

    public void setId_vehiculo(String id_vehiculo) {
        this.id_vehiculo = id_vehiculo;
    }

    public String getId_vehiculo_ruta() {
        return id_vehiculo_ruta;
    }

    public void setId_vehiculo_ruta(String id_vehiculo_ruta) {
        this.id_vehiculo_ruta = id_vehiculo_ruta;
    }

    public String getId_alumno() {
        return id_alumno;
    }

    public void setId_alumno(String id_alumno) {
        this.id_alumno = id_alumno;
    }

    public int getTipo_checkin() {
        return tipo_checkin;
    }

    public void setTipo_checkin(int tipo_checkin) {
        this.tipo_checkin = tipo_checkin;
    }
}
