package co.com.puli.trade.fdv.clases;

import java.util.Objects;

/**
 * Clase para el control de las propiedades de las Categorías de los productos
 * */
public class Categoria {
    private String id, nombre;

    public Categoria(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return getId().equals( categoria.getId() );
    }

    @Override
    public int hashCode() {

        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
