package co.com.puli.trade.fdv.clases;

/**
 * Gestión de propiedades del Ruta del Alumno
 * Created by Carlos E. Mera Ruiz on 10/11/15.
 */
public class RutaAlumno
{
    private String nombre, apellido, id, idRuta, descRuta, idRutaVehiculo, idVehiculo, idConductor, idMonitor;
    private int checkIn, checkOut, checkAusente, orden;

    public RutaAlumno(String nombre, String apellido, String id, String idRuta, String descRuta, String idRutaVehiculo,
                      String idVehiculo, String idConductor, String idMonitor, int checkIn, int checkOut, int checkAusente, int orden)
    {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.idRuta = idRuta;
        this.descRuta = descRuta;
        this.idRutaVehiculo = idRutaVehiculo;
        this.idVehiculo = idVehiculo;
        this.idConductor = idConductor;
        this.idMonitor = idMonitor;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkAusente = checkAusente;
        this.orden = orden;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public String getDescRuta() {
        return descRuta;
    }

    public void setDescRuta(String descRuta) {
        this.descRuta = descRuta;
    }

    public String getIdRutaVehiculo() {
        return idRutaVehiculo;
    }

    public void setIdRutaVehiculo(String idRutaVehiculo) {
        this.idRutaVehiculo = idRutaVehiculo;
    }

    public String getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(String idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public String getIdMonitor() {
        return idMonitor;
    }

    public void setIdMonitor(String idMonitor) {
        this.idMonitor = idMonitor;
    }

    public int getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(int checkIn) {
        this.checkIn = checkIn;
    }

    public int getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(int checkOut) {
        this.checkOut = checkOut;
    }

    public int getCheckAusente() {
        return checkAusente;
    }

    public void setCheckAusente(int checkAusente) {
        this.checkAusente = checkAusente;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public boolean isCheckIn()
    {
        return getCheckIn() >= 1 ? true : false;
    }

    public boolean isCheckOut()
    {
        return getCheckOut() >= 1 ? true : false;
    }

    public boolean isCheckAusente()
    {
        return getCheckAusente() >= 1 ? true : false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RutaAlumno)) return false;

        RutaAlumno that = (RutaAlumno) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", id='" + id + '\'' +
                ", idRuta='" + idRuta + '\'' +
                ", descRuta='" + descRuta + '\'' +
                ", idRutaVehiculo='" + idRutaVehiculo + '\'' +
                ", idVehiculo='" + idVehiculo + '\'' +
                ", idConductor='" + idConductor + '\'' +
                ", idMonitor='" + idMonitor + '\'' +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", checkAusente=" + checkAusente +
                ", orden=" + orden +
                '}';
    }

    public String getNombreCompleto(){ return getNombre() +" "+ getApellido(); }
}
