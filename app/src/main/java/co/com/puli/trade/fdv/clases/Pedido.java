package co.com.puli.trade.fdv.clases;

public class Pedido
{
    private String fecha;
    private int cantidad;

    public Pedido(String fecha, int cantidad) {
        this.fecha = fecha;
        this.cantidad = cantidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
