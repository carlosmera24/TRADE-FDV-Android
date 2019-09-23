package co.com.puli.trade.fdv.clases;

import android.location.Location;

/**
 * Created by carlos on 9/12/16.
 */

public class RutaGrama
{
    String id, id_tipo, direccion, lat, lng;

    public RutaGrama(String id, String id_tipo, String direccion, String lat, String lng) {
        this.id = id;
        this.id_tipo = id_tipo;
        this.direccion = direccion;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_tipo() {
        return id_tipo;
    }

    public void setId_tipo(String id_tipo) {
        this.id_tipo = id_tipo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLat() {
        return lat;
    }

    public Double getLatDouble()
    {
        try {
            return Double.parseDouble(lat);
        }catch( Exception e )
        {
            return null;
        }
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public Double getLngDouble()
    {
        try {
            return Double.parseDouble( lng );
        }catch( Exception e )
        {
            return null;
        }
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    /**
     * Método encargado de retornar las coordenadas (Lat,Lng) como Location
     * @return  Location coordenadas, null si no es posible crear la coordenada
     * */
    public Location getLocation()
    {
        if( getLatDouble() != null && getLngDouble() != null) {
            Location coord = new Location("");
            coord.setLatitude(getLatDouble());
            coord.setLongitude(getLngDouble());
            return coord;
        }else{
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RutaGrama)) return false;

        RutaGrama rutaGrama = (RutaGrama) o;

        return getId().equals(rutaGrama.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "RutaGrama{" +
                "id='" + id + '\'' +
                ", id_tipo='" + id_tipo + '\'' +
                ", direccion='" + direccion + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                '}';
    }
}
