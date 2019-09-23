package co.com.puli.trade.fdv.clases;

/**
 * Created by carlos on 27/07/16.
 */
public class TipoInspeccion
{
    private int id, valoracion;
    private String descripcion;

    public TipoInspeccion(int id, String descripcion, int valoracion)
    {
        this.id = id;
        this.valoracion = valoracion;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValoracion() {
        return valoracion;
    }

    public void setValoracion(int valoracion) {
        this.valoracion = valoracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "TipoInspeccion{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", valoracion=" + valoracion +
                '}';
    }
}
