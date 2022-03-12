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
    private String id_fdv;
    private String id_pdv;
    private int tipo_checkin;

    public MvtoCheck(Double lat, Double lng, String fecha, String id_fdv, String id_pdv,
                     int tipo_checkin) {
        id = 0;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_fdv = id_fdv;
        this.id_pdv = id_pdv;
        this.tipo_checkin = tipo_checkin;
    }

    public MvtoCheck(int id, Double lat, Double lng, String fecha, String id_fdv, String id_pdv,
                     int tipo_checkin) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.fecha = fecha;
        this.id_fdv = id_fdv;
        this.id_pdv = id_pdv;
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

    public String getId_fdv() {
        return id_fdv;
    }

    public void setId_fdv(String id_fdv) {
        this.id_fdv = id_fdv;
    }

    public String getId_pdv() {
        return id_pdv;
    }

    public void setId_pdv(String id_pdv) {
        this.id_pdv = id_pdv;
    }

    public int getTipo_checkin() {
        return tipo_checkin;
    }

    public void setTipo_checkin(int tipo_checkin) {
        this.tipo_checkin = tipo_checkin;
    }
}
