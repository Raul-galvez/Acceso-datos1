import java.util.Objects;

/**
 * Producto del catálogo de la tienda.
 *
 * La modalidad es final: pertenece al producto durante toda su comercialización.
 * El precio sí cambia, pero el cambio no se propaga a las líneas ya cerradas
 * porque cada {@link LineaVenta} guarda el precio realmente cobrado.
 */
public final class Producto {

    private final Integer id;
    private final String referenciaComercial;
    private String descripcion;
    private double precioVenta;
    private EstadoComercializacion estado;
    private final ModalidadVenta modalidad;

    public Producto(String referenciaComercial, String descripcion, double precioVenta,
                    EstadoComercializacion estado, ModalidadVenta modalidad) {
        this(null, referenciaComercial, descripcion, precioVenta, estado, modalidad);
    }

    public Producto(Integer id, String referenciaComercial, String descripcion, double precioVenta,
                    EstadoComercializacion estado, ModalidadVenta modalidad) {
        this.id = id;
        this.referenciaComercial = obligatorio(referenciaComercial, "La referencia comercial");
        this.descripcion = obligatorio(descripcion, "La descripción");
        this.modalidad = Objects.requireNonNull(modalidad, "La modalidad de venta es obligatoria.");
        this.estado = Objects.requireNonNull(estado, "El estado de comercialización es obligatorio.");
        this.precioVenta = 0; // el setter valida antes de asignar
        cambiarPrecio(precioVenta);
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatoria.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getReferenciaComercial() { return referenciaComercial; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioVenta() { return precioVenta; }
    public EstadoComercializacion getEstado() { return estado; }
    public ModalidadVenta getModalidad() { return modalidad; }

    public void cambiarPrecio(double nuevoPrecio) {
        if (!Double.isFinite(nuevoPrecio) || nuevoPrecio <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }
        this.precioVenta = RedondeoMonetario.monetario(nuevoPrecio);
    }

    public void cambiarDescripcion(String nuevaDescripcion) {
        this.descripcion = obligatorio(nuevaDescripcion, "La descripción");
    }

    public void retirar() { this.estado = EstadoComercializacion.RETIRADO; }

    public void reactivar() { this.estado = EstadoComercializacion.ACTIVO; }

    /** Valida y calcula delegando en la modalidad; aquí no hay switch por tipo. */
    public double calcularImporte(double cantidad) {
        return modalidad.calcularImporte(cantidad, precioVenta);
    }

    public double normalizarCantidad(double cantidad) {
        return modalidad.normalizarCantidad(cantidad);
    }

    /** Calcula con un precio explícito: tickets y devoluciones usan el precio cobrado. */
    public double calcularImporteConPrecio(double cantidad, double precioUnitario) {
        return modalidad.calcularImporte(cantidad, precioUnitario);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Producto otro && referenciaComercial.equals(otro.referenciaComercial);
    }

    @Override public int hashCode() { return Objects.hash(referenciaComercial); }

    @Override public String toString() {
        return referenciaComercial + " - " + descripcion + " ("
                + RedondeoMonetario.formatear(precioVenta) + " €, " + modalidad.codigo() + ")";
    }
}
