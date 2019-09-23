package co.com.puli.trade.fdv.clases;

import java.util.Objects;

/**
 * Clase para gestión de propiedades de los Puntos de Venta
 * */

public class PDV
{
    private String id, nombre, nombreContacto, apellidoContacto, direccion, telefono, celular, email, lat, lng, zona;
    private int checkIn, checkOut, checkAusente;

    public PDV(String id, String nombre, String nombreContacto, String apellidoContacto, String direccion, String telefono,
               String celular, String email, String lat, String lng, String zona, int checIn, int checkOut, int checkAusente) {
        this.id = id;
        this.nombre = nombre;
        this.nombreContacto = nombreContacto;
        this.apellidoContacto = apellidoContacto;
        this.direccion = direccion;
        this.telefono = telefono;
        this.celular = celular;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.zona = zona;
        this.checkIn = checIn;
        this.checkOut = checkOut;
        this.checkAusente = checkAusente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getApellidoContacto() {
        return apellidoContacto;
    }

    public void setApellidoContacto(String apellidoContacto) {
        this.apellidoContacto = apellidoContacto;
    }

    public String getNombreCompletoContacto(){
        return getNombreContacto() +" "+ getApellidoContacto();
    }
    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLat() {
        try {
            return Double.parseDouble( lat );
        }catch( Exception e )
        {
            return null;
        }
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public Double getLng() {
        try {
            return Double.parseDouble( lng );
        }catch( Exception e )
        {
            return null;
        }
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
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
        if (!(o instanceof PDV)) return false;
        PDV pdv = (PDV) o;
        return getId().equals( pdv.getId() );
    }

    @Override
    public int hashCode() {

        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "PDV{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", nombreContacto='" + nombreContacto + '\'' +
                ", apellidoContacto='" + apellidoContacto + '\'' +
                ", direccion='" + direccion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", celular='" + celular + '\'' +
                ", email='" + email + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", zona='" + zona + '\'' +
                '}';
    }
}
