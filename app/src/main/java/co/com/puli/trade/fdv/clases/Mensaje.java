package co.com.puli.trade.fdv.clases;

/**
 * Gestión de las propiedaes del Mensaje
 * Created by carlos on 26/11/15.
 */
public class Mensaje
{
    private String id, fecha, texto;

    public Mensaje(String id, String fecha, String texto)
    {
        this.id = id;
        this.fecha = fecha;
        this.texto = texto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
