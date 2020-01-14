package co.com.puli.trade.fdv.clases;

/**
 * Clase para manejar las propiedades de los productos asociados a la tabla tblProductosTrade
 * */
public class Producto
{
    private String id, nombre, url_imagen, id_empresa, empresa, id_categoria, categoria;
    private int catidad;

    public Producto(String id, String nombre, String url_imagen, String id_empresa, String empresa, String id_categoria, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.url_imagen = url_imagen;
        this.id_empresa = id_empresa;
        this.empresa = empresa;
        this.id_categoria = id_categoria;
        this.categoria = categoria;
        catidad = 0;
    }


    /**
     * Constructor del producto
     * @param id
     * @param nombre
     * @param url_imagen
     * @param id_empresa
     * @param empresa
     * @param id_categoria
     * @param categoria
     * @param catidad
     */
    public Producto(String id, String nombre, String url_imagen, String id_empresa, String empresa, String id_categoria, String categoria, int catidad) {
        this.id = id;
        this.nombre = nombre;
        this.url_imagen = url_imagen;
        this.id_empresa = id_empresa;
        this.empresa = empresa;
        this.catidad = catidad;
        this.id_categoria = id_categoria;
        this.categoria = categoria;
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

    public String getUrl_imagen() {
        return url_imagen;
    }

    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }

    public String getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(String id_empresa) {
        this.id_empresa = id_empresa;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getId_categoria() {
        return id_categoria;
    }

    public void setId_categoria(String id_categoria) {
        this.id_categoria = id_categoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getCatidad() {
        return catidad;
    }

    public void setCatidad(int catidad) {
        this.catidad = catidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return getId().equals( producto.getId() );
    }

    @Override
    public int hashCode() {

        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", url_imagen='" + url_imagen + '\'' +
                ", id_empresa='" + id_empresa + '\'' +
                ", empresa='" + empresa + '\'' +
                ", id_categoria='" + id_categoria + '\'' +
                ", categoria='" + categoria + '\'' +
                ", catidad=" + catidad +
                '}';
    }
}
