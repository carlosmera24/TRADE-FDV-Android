package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 13/11/21
 */
public class Usuario
{
    private int id;
    private String usuario;
    private int id_perfil;
    private String id_conductor;
    private String id_fdv;
    private String id_ruta;
    private String nombre_usuario;
    private String token;
    private String imagen;
    public String empresa;

    public Usuario(){}

    public Usuario(int id,
                   String usuario,
                   int id_perfil,
                   String id_conductor,
                   String id_fdv,
                   String id_ruta,
                   String nombre_usuario,
                   String token,
                   String imagen,
                   String empresa)
    {
        this.id = id;
        this.usuario = usuario;
        this.id_perfil = id_perfil;
        this.id_conductor = id_conductor;
        this.id_fdv = id_fdv;
        this.id_ruta = id_ruta;
        this.nombre_usuario = nombre_usuario;
        this.token = token;
        this.imagen = imagen;
        this.empresa = empresa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getId_perfil() {
        return id_perfil;
    }

    public void setId_perfil(int id_perfil) {
        this.id_perfil = id_perfil;
    }

    public String getId_conductor() {
        return id_conductor;
    }

    public void setId_conductor(String id_conductor) {
        this.id_conductor = id_conductor;
    }

    public String getId_fdv() {
        return id_fdv;
    }

    public void getId_fdv(String id_fdv) {
        this.id_fdv = id_fdv;
    }

    public String getId_ruta() {
        return id_ruta;
    }

    public void setId_ruta(String id_ruta) {
        this.id_ruta = id_ruta;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setId_fdv(String id_fdv) {
        this.id_fdv = id_fdv;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", id_perfil=" + id_perfil +
                ", id_conductor='" + id_conductor + '\'' +
                ", id_fdv='" + id_fdv + '\'' +
                ", id_ruta='" + id_ruta + '\'' +
                ", nombre_usuario='" + nombre_usuario + '\'' +
                ", token='" + token + '\'' +
                ", imagen='" + imagen + '\'' +
                ", empresa='" + empresa + '\'' +
                '}';
    }
}
