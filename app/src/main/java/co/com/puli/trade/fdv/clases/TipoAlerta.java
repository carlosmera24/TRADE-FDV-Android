package co.com.puli.trade.fdv.clases;

/**
 * Clase encargada de contener la información de los objetos Alertas
 * Created by carlos on 9/02/16.
 */
public class TipoAlerta
{
    private int id, nivel;
    private String descripcion;

    /**
     * Constructor de la clase
     * @param id ID del tipo de alerta
     * @param descripcion Nombre del tipo de alerta
     * @param nivel ID del nivel del tipo de alerta
     * */
    public TipoAlerta(int id, String descripcion, int nivel)
    {
        this.id = id;
        this.descripcion = descripcion;
        this.nivel = nivel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
