package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 13/11/21
 */
public class Empresa
{
    private int id;
    private String nombre;
    private Double lat;
    private Double lng;

    public Empresa(){}

    public Empresa(String nombre, Double lat, Double lng) {
        this.nombre = nombre;
        this.lat = lat;
        this.lng = lng;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
