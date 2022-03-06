package co.com.puli.trade.fdv.database.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by carlos on 27/11/21
 */
public class MvtoKilometros
{
    private int id;
    private String km;
    private String id_vehiculo;
    private String id_conductor;
    private String fecha;

    public MvtoKilometros() {
    }

    public MvtoKilometros(int id, String km, String id_vehiculo, String id_conductor) {
        this.id = id;
        this.km = km;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.fecha = sdf.format( Calendar.getInstance().getTime() );
    }

    public MvtoKilometros(int id, String km, String id_vehiculo, String id_conductor, String fecha) {
        this.id = id;
        this.km = km;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        this.fecha = fecha;
    }

    public MvtoKilometros(String km, String id_vehiculo, String id_conductor) {
        this.id = 0;
        this.km = km;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.fecha = sdf.format( Calendar.getInstance().getTime() );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getId_vehiculo() {
        return id_vehiculo;
    }

    public void setId_vehiculo(String id_vehiculo) {
        this.id_vehiculo = id_vehiculo;
    }

    public String getId_conductor() {
        return id_conductor;
    }

    public void setId_conductor(String id_conductor) {
        this.id_conductor = id_conductor;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "MvtoKilometros{" +
                "id=" + id +
                ", km='" + km + '\'' +
                ", id_vehiculo='" + id_vehiculo + '\'' +
                ", id_conductor='" + id_conductor + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
