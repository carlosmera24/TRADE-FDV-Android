package co.com.puli.trade.fdv.clases;

/**
 * Created by carlos on 10/10/16.
 */

public class SolicitudesServicio
{
    private String id, id_usuario, id_tipo, desc_tipo_servicio, desc, fecha, lat, lng, status;

    public SolicitudesServicio(String id, String id_usuario, String id_tipo, String desc_tipo_servicio, String desc, String fecha, String lat, String lng, String status) {
        this.id = id;
        this.id_usuario = id_usuario;
        this.id_tipo = id_tipo;
        this.desc_tipo_servicio = desc_tipo_servicio;
        this.desc = desc;
        this.fecha = fecha;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getId_tipo() {
        return id_tipo;
    }

    public void setId_tipo(String id_tipo) {
        this.id_tipo = id_tipo;
    }

    public String getDesc_tipo_servicio() {
        return desc_tipo_servicio;
    }

    public void setDesc_tipo_servicio(String desc_tipo_servicio) {
        this.desc_tipo_servicio = desc_tipo_servicio;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolicitudesServicio)) return false;

        SolicitudesServicio that = (SolicitudesServicio) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "SolicitudesServicio{" +
                "id='" + id + '\'' +
                ", id_usuario='" + id_usuario + '\'' +
                ", id_tipo='" + id_tipo + '\'' +
                ", desc_tipo_servicio='" + desc_tipo_servicio + '\'' +
                ", desc='" + desc + '\'' +
                ", fecha='" + fecha + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
