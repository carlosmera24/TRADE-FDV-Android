package co.com.puli.trade.fdv.clases;

import java.util.Objects;

/**
 * Clase utilizada para manejar objetos de tipo ciudad desde la BBDD
 * */
public class Ciudad
{
    private int id;
    private String descripcion;

    public Ciudad(int id, String descripcion)
    {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ciudad ciudad = (Ciudad) o;
        return id == ciudad.id;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
