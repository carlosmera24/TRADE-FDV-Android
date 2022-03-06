package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 27/11/21
 */
public class TipoInspeccion
{
    private int id;
    private String descripcion;

    public TipoInspeccion(){}

    public TipoInspeccion(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
