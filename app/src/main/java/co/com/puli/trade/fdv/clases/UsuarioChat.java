package co.com.puli.trade.fdv.clases;

/**
 * Clase utilizada para registrar usuarios disponibles para chat
 * Created by carlos on 17/02/16.
 */
public class UsuarioChat
{
    private int id_usuario, id_perfil;
    private String nombre;

    public UsuarioChat(int id_usuario, int id_perfil, String nombre)
    {
        this.id_usuario = id_usuario;
        this.id_perfil = id_perfil;
        this.nombre = nombre;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public int getId_perfil() {
        return id_perfil;
    }

    public void setId_perfil(int id_perfil) {
        this.id_perfil = id_perfil;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    @Override
    public String toString() {
        return "UsuarioChat{" +
                "id_usuario=" + id_usuario +
                ", id_perfil=" + id_perfil +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
