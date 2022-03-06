package co.com.puli.trade.fdv.database.models;

public class MvtoInicioRuta
{
    private int id;
    private String fecha;
    private String id_ruta;
    private Double lat;
    private Double lng;

    public MvtoInicioRuta(String fecha, String id_ruta, Double lat, Double lng) {
        this.id = 0;
        this.fecha = fecha;
        this.id_ruta = id_ruta;
        this.lat = lat;
        this.lng = lng;
    }

    public MvtoInicioRuta(int id, String fecha, String id_ruta, Double lat, Double lng)
    {
        this.id = id;
        this.fecha = fecha;
        this.id_ruta = id_ruta;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
