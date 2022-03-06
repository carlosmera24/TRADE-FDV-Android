package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 31/12/21
 */
public class TerminoAndCondition
{
    private int id;
    private String estado;
    private String mensage;
    private String codigoUsuario;

    public TerminoAndCondition(String estado, String mensage, String codigoUsuario) {
        id = 0;
        this.estado = estado;
        this.mensage = mensage;
        this.codigoUsuario = codigoUsuario;
    }

    public TerminoAndCondition(int id, String estado, String mensage, String codigoUsuario) {
        this.id = id;
        this.estado = estado;
        this.mensage = mensage;
        this.codigoUsuario = codigoUsuario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensage() {
        return mensage;
    }

    public void setMensage(String mensage) {
        this.mensage = mensage;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }
}
