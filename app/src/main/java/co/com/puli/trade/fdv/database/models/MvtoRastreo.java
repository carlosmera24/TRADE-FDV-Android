package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 31/12/21
 */
public class MvtoRastreo
{
    private int id;
    private double lat;
    private double lng;
    private String fecha;
    private String id_ruta;

    public MvtoRastreo(double lat, double lng, String fecha, String id_ruta) {
        id = 0;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_ruta = id_ruta;
    }

    public MvtoRastreo(int id, double lat, double lng, String fecha, String id_ruta) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_ruta = id_ruta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getId_ruta() {
        return id_ruta;
    }

    public void setId_ruta(String id_ruta) {
        this.id_ruta = id_ruta;
    }
}
