import java.util.Objects;

/**
 * Acuerdo vigente entre un proveedor y un producto. No se guarda historial de
 * condiciones, pero sí la referencia que usa el proveedor y el plazo habitual.
 */
public final class AcuerdoSuministro {

    private final Integer id;
    private final Proveedor proveedor;
    private final Producto producto;
    private String referenciaProveedor;
    private double precioCompra;
    private int plazoEntregaDias;

    public AcuerdoSuministro(Proveedor proveedor, Producto producto, String referenciaProveedor,
                             double precioCompra, int plazoEntregaDias) {
        this(null, proveedor, producto, referenciaProveedor, precioCompra, plazoEntregaDias);
    }

    public AcuerdoSuministro(Integer id, Proveedor proveedor, Producto producto,
                             String referenciaProveedor, double precioCompra, int plazoEntregaDias) {
        this.id = id;
        this.proveedor = Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
        this.producto = Objects.requireNonNull(producto, "El producto es obligatorio.");
        this.referenciaProveedor = obligatorio(referenciaProveedor, "La referencia del proveedor");
        if (!Double.isFinite(precioCompra) || precioCompra <= 0) {
            throw new IllegalArgumentException("El precio de compra debe ser mayor que cero.");
        }
        if (plazoEntregaDias < 0) {
            throw new IllegalArgumentException("El plazo de entrega no puede ser negativo.");
        }
        this.precioCompra = RedondeoMonetario.monetario(precioCompra);
        this.plazoEntregaDias = plazoEntregaDias;
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatoria.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public Proveedor getProveedor() { return proveedor; }
    public Producto getProducto() { return producto; }
    public String getReferenciaProveedor() { return referenciaProveedor; }
    public double getPrecioCompra() { return precioCompra; }
    public int getPlazoEntregaDias() { return plazoEntregaDias; }

    public void cambiarCondiciones(String referencia, double precio, int plazo) {
        this.referenciaProveedor = obligatorio(referencia, "La referencia del proveedor");
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio de compra debe ser mayor que cero.");
        }
        this.precioCompra = RedondeoMonetario.monetario(precio);
        if (plazo < 0) {
            throw new IllegalArgumentException("El plazo de entrega no puede ser negativo.");
        }
        this.plazoEntregaDias = plazo;
    }

    @Override public boolean equals(Object o) {
        return o instanceof AcuerdoSuministro otro
                && proveedor.equals(otro.proveedor) && producto.equals(otro.producto);
    }

    @Override public int hashCode() { return Objects.hash(proveedor, producto); }

    @Override public String toString() {
        return proveedor.getCodigoInterno() + " -> " + producto.getReferenciaComercial()
                + " (ref. " + referenciaProveedor + ")";
    }
}
