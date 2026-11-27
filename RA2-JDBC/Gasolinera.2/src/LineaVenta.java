import java.util.Objects;

/**
 * Línea de un ticket. Guarda el precio unitario realmente aplicado y el importe
 * resultante: un cambio posterior en el catálogo no puede recalcular el pasado.
 * La cantidad queda normalizada según la modalidad del producto.
 */
public final class LineaVenta {

    private final Integer id;
    private final Venta venta;
    private final int numeroOrden;
    private final Producto producto;
    private final double cantidad;
    private final double precioUnitario;
    private final double importe;

    public LineaVenta(int numeroOrden, Producto producto, double cantidad) {
        this(null, null, numeroOrden, producto, cantidad,
                Objects.requireNonNull(producto, "El producto es obligatorio.").getPrecioVenta());
    }

    public LineaVenta(Integer id, Venta venta, int numeroOrden, Producto producto,
                      double cantidad, double precioUnitario) {
        this.id = id;
        this.venta = venta;
        this.numeroOrden = numeroOrden;
        this.producto = Objects.requireNonNull(producto, "El producto es obligatorio.");
        if (numeroOrden <= 0) {
            throw new IllegalArgumentException("El número de orden debe ser mayor que cero.");
        }
        this.cantidad = producto.normalizarCantidad(cantidad);
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario cobrado debe ser mayor que cero.");
        }
        this.precioUnitario = RedondeoMonetario.monetario(precioUnitario);
        this.importe = producto.calcularImporteConPrecio(this.cantidad, this.precioUnitario);
    }

    /** Prepara la misma línea ya asociada a un ticket; no cambia precio ni importe. */
    public LineaVenta conVenta(Venta destino) {
        return new LineaVenta(id, destino, numeroOrden, producto, cantidad, precioUnitario);
    }

    public Integer getId() { return id; }
    public Venta getVenta() { return venta; }
    public int getNumeroOrden() { return numeroOrden; }
    public Producto getProducto() { return producto; }
    public double getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getImporte() { return importe; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof LineaVenta otra)) return false;
        String ref = venta == null ? null : venta.getReferenciaTicket();
        String otraRef = otra.venta == null ? null : otra.venta.getReferenciaTicket();
        return numeroOrden == otra.numeroOrden && Objects.equals(ref, otraRef);
    }

    @Override public int hashCode() { return Objects.hash(venta == null ? null : venta.getReferenciaTicket(), numeroOrden); }

    @Override public String toString() {
        return numeroOrden + ". " + producto.getReferenciaComercial() + " x "
                + RedondeoMonetario.formatear(cantidad) + " @ "
                + RedondeoMonetario.formatear(precioUnitario) + " = "
                + RedondeoMonetario.formatear(importe) + " €";
    }
}
