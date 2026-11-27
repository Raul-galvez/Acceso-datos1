import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Devolución parcial o total de una línea concreta.
 *
 * No modifica la línea original: guarda su propia cantidad, motivo, fecha,
 * empleado, importe reintegrado (calculado con el precio cobrado) y si la
 * mercancía puede volver a venderse.
 */
public final class Devolucion {

    private final Integer id;
    private final LineaVenta linea;
    private final double cantidad;
    private final String motivo;
    private final LocalDateTime fechaHora;
    private final Empleado empleado;
    private final double importeReintegrado;
    private final boolean aptaParaReventa;

    public Devolucion(LineaVenta linea, double cantidad, String motivo,
                      LocalDateTime fechaHora, Empleado empleado, boolean aptaParaReventa) {
        this(null, linea, cantidad, motivo, fechaHora, empleado, aptaParaReventa);
    }

    public Devolucion(Integer id, LineaVenta linea, double cantidad, String motivo,
                      LocalDateTime fechaHora, Empleado empleado, boolean aptaParaReventa) {
        this.id = id;
        this.linea = Objects.requireNonNull(linea, "La línea devuelta es obligatoria.");
        this.cantidad = linea.getProducto().normalizarCantidad(cantidad);
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la devolución es obligatorio.");
        }
        this.motivo = motivo.trim();
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora son obligatorias.");
        this.empleado = Objects.requireNonNull(empleado, "El empleado que acepta la devolución es obligatorio.");
        this.aptaParaReventa = aptaParaReventa;
        this.importeReintegrado = linea.getProducto()
                .calcularImporteConPrecio(this.cantidad, linea.getPrecioUnitario());
    }

    public Integer getId() { return id; }
    public LineaVenta getLinea() { return linea; }
    public double getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public double getImporteReintegrado() { return importeReintegrado; }
    public boolean isAptaParaReventa() { return aptaParaReventa; }

    @Override public String toString() {
        return linea.getProducto().getReferenciaComercial() + " - "
                + RedondeoMonetario.formatear(cantidad) + " uds. -> "
                + RedondeoMonetario.formatear(importeReintegrado) + " € ("
                + (aptaParaReventa ? "apta" : "no apta") + ")";
    }
}
