package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 13/11/21
 */
public class Inspeccion
{
    private int id;
    private int id_tipo_inspeccion;
    private String id_vehiculo;
    private String id_conductor;
    private String fecha;
    private String fecha_date;
    private String resultado;

    public Inspeccion(){}

    public Inspeccion(int id_tipo_inspeccion, String id_vehiculo, String id_conductor, String fecha, String fecha_date, String resultado) {
        id = 0;
        this.id_tipo_inspeccion = id_tipo_inspeccion;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        this.fecha = fecha;
        this.fecha_date = fecha_date;
        this.resultado = resultado;
    }

    public Inspeccion(int id, int id_tipo_inspeccion, String id_vehiculo, String id_conductor, String fecha, String fecha_date, String resultado) {
        this.id = id;
        this.id_tipo_inspeccion = id_tipo_inspeccion;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        this.fecha = fecha;
        this.fecha_date = fecha_date;
        this.resultado = resultado;
    }

    public int getId() {
        return id;
    }

    public int getId_tipo_inspeccion() {
        return id_tipo_inspeccion;
    }

    public void setId_tipo_inspeccion(int id_tipo_inspeccion) {
        this.id_tipo_inspeccion = id_tipo_inspeccion;
    }

    public String getId_vehiculo() {
        return id_vehiculo;
    }

    public void setId_vehiculo(String id_vehiculo) {
        this.id_vehiculo = id_vehiculo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFecha_date() {
        return fecha_date;
    }

    public void setFecha_date(String fecha_date) {
        this.fecha_date = fecha_date;
    }

    public String getId_conductor() {
        return id_conductor;
    }

    public void setId_conductor(String id_conductor) {
        this.id_conductor = id_conductor;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    @Override
    public String toString() {
        return "Inspeccion{" +
                "id=" + id +
                ", id_tipo_inspeccion=" + id_tipo_inspeccion +
                ", id_vehiculo='" + id_vehiculo + '\'' +
                ", id_conductor='" + id_conductor + '\'' +
                ", fecha='" + fecha + '\'' +
                ", fecha_date='" + fecha_date + '\'' +
                ", resultado='" + resultado + '\'' +
                '}';
    }
}
