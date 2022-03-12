package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 13/11/21
 */
public class Inspeccion
{
    private int id;
    private int id_tipo_inspeccion;
    private String id_fdv;
    private String fecha;
    private String fecha_date;
    private String resultado;

    public Inspeccion(){}

    public Inspeccion(int id_tipo_inspeccion, String id_fdv, String fecha, String fecha_date, String resultado) {
        id = 0;
        this.id_tipo_inspeccion = id_tipo_inspeccion;
        this.id_fdv = id_fdv;
        this.fecha = fecha;
        this.fecha_date = fecha_date;
        this.resultado = resultado;
    }

    public Inspeccion(int id, int id_tipo_inspeccion, String id_fdv, String fecha, String fecha_date, String resultado) {
        this.id = id;
        this.id_tipo_inspeccion = id_tipo_inspeccion;
        this.id_fdv = id_fdv;
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

    public String getId_fdv() {
        return id_fdv;
    }

    public void setId_fd(String id_fdv) {
        this.id_fdv = id_fdv;
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
                ", id_fd='" + id_fdv + '\'' +
                ", fecha='" + fecha + '\'' +
                ", fecha_date='" + fecha_date + '\'' +
                ", resultado='" + resultado + '\'' +
                '}';
    }
}
