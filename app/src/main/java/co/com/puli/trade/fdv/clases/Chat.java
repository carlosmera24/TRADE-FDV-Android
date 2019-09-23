package co.com.puli.trade.fdv.clases;

/**
 * Clase para la gestión de los atributos del chat
 * Created by carlos on 7/12/15.
 */
public class Chat
{
    private String id, id_origen, id_destino, mensaje, fecha;

    public Chat(String id, String id_origen, String id_destino, String mensaje, String fecha)
    {
        this.id = id;
        this.id_origen = id_origen;
        this.id_destino = id_destino;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_origen() {
        return id_origen;
    }

    public void setId_origen(String id_origen) {
        this.id_origen = id_origen;
    }

    public String getId_destino() {
        return id_destino;
    }

    public void setId_destino(String id_destino) {
        this.id_destino = id_destino;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
