package co.com.puli.trade.fdv.database.models;

/**
 * Created by carlos on 2/12/21
 */
public class RutaAlumno
{
    private int id;
    private String id_alumno;
    private String id_ruta;
    private String id_ruta_vehiculo;
    private String id_vehiculo;
    private String id_conductor;
    private String desc_ruta;
    private String id_monitor;
    private String nombre;
    private String apellido;
    private int estado_in;
    private int estado_out;
    private int estado_ausente;
    private int orden;

    public RutaAlumno() {
    }

    public RutaAlumno(int id, String id_alumno, String id_ruta, String id_ruta_vehiculo, String id_vehiculo, String id_conductor, String desc_ruta, String id_monitor, String nombre, String apellido, int estado_in, int estado_out, int estado_ausente, int orden) {
        this.id = id;
        this.id_alumno = id_alumno;
        this.id_ruta = id_ruta;
        this.id_ruta_vehiculo = id_ruta_vehiculo;
        this.id_vehiculo = id_vehiculo;
        this.id_conductor = id_conductor;
        this.desc_ruta = desc_ruta;
        this.id_monitor = id_monitor;
        this.nombre = nombre;
        this.apellido = apellido;
        this.estado_in = estado_in;
        this.estado_out = estado_out;
        this.estado_ausente = estado_ausente;
        this.orden = orden;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getId_alumno() {
        return id_alumno;
    }

    public void setId_alumno(String id_alumno) {
        this.id_alumno = id_alumno;
    }

    public String getId_ruta() {
        return id_ruta;
    }

    public void setId_ruta(String id_ruta) {
        this.id_ruta = id_ruta;
    }

    public String getId_ruta_vehiculo() {
        return id_ruta_vehiculo;
    }

    public void setId_ruta_vehiculo(String id_ruta_vehiculo) {
        this.id_ruta_vehiculo = id_ruta_vehiculo;
    }

    public String getId_vehiculo() {
        return id_vehiculo;
    }

    public void setId_vehiculo(String id_vehiculo) {
        this.id_vehiculo = id_vehiculo;
    }

    public String getId_conductor() {
        return id_conductor;
    }

    public void setId_conductor(String id_conductor) {
        this.id_conductor = id_conductor;
    }

    public String getDesc_ruta() {
        return desc_ruta;
    }

    public void setDesc_ruta(String desc_ruta) {
        this.desc_ruta = desc_ruta;
    }

    public String getId_monitor() {
        return id_monitor;
    }

    public void setId_monitor(String id_monitor) {
        this.id_monitor = id_monitor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getEstado_in() {
        return estado_in;
    }

    public void setEstado_in(int estado_in) {
        this.estado_in = estado_in;
    }

    public int getEstado_out() {
        return estado_out;
    }

    public void setEstado_out(int estado_out) {
        this.estado_out = estado_out;
    }

    public int getEstado_ausente() {
        return estado_ausente;
    }

    public void setEstado_ausente(int estado_ausente) {
        this.estado_ausente = estado_ausente;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
