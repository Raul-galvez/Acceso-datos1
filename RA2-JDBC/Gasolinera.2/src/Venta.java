import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Venta de tienda. La estación y el empleado se guardan en la propia venta: un
 * cambio de adscripción posterior no altera la autoría ni el lugar de tickets
 * anteriores. El cliente es opcional (compras anónimas).
 */
public final class Venta {

    private final Integer id;
    private final String referenciaTicket;
    private final LocalDateTime fechaHora;
    private final Estacion estacion;
    private final Empleado empleado;
    private final ClienteTienda cliente;
    private final List<LineaVenta> lineas = new ArrayList<>();
    private final List<CobroTienda> cobros = new ArrayList<>();
    private boolean confirmadaPagada;

    public Venta(String referenciaTicket, LocalDateTime fechaHora, Estacion estacion,
                 Empleado empleado, ClienteTienda cliente, List<LineaVenta> lineas) {
        this(null, referenciaTicket, fechaHora, estacion, empleado, cliente, lineas);
    }

    public Venta(Integer id, String referenciaTicket, LocalDateTime fechaHora, Estacion estacion,
                 Empleado empleado, ClienteTienda cliente, List<LineaVenta> lineas) {
        this.id = id;
        this.referenciaTicket = obligatorio(referenciaTicket, "La referencia del ticket");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora de la venta son obligatorias.");
        this.estacion = Objects.requireNonNull(estacion, "La estación de la venta es obligatoria.");
        this.empleado = Objects.requireNonNull(empleado, "El empleado que registra la venta es obligatorio.");
        this.cliente = cliente;
        Objects.requireNonNull(lineas, "Las líneas son obligatorias.");
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("Un carrito vacío todavía no es una venta registrada.");
        }
        this.lineas.addAll(lineas.stream().map(l -> l.conVenta(this)).collect(Collectors.toList()));
        comprobarOrdenesUnicos();
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatoria.");
        }
        return valor.trim();
    }

    private void comprobarOrdenesUnicos() {
        long distintos = lineas.stream().map(LineaVenta::getNumeroOrden).distinct().count();
        if (distintos != lineas.size()) {
            throw new IllegalArgumentException("El número de orden no puede repetirse dentro de un ticket.");
        }
    }

    public Integer getId() { return id; }
    public String getReferenciaTicket() { return referenciaTicket; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Estacion getEstacion() { return estacion; }
    public Empleado getEmpleado() { return empleado; }

    public java.util.Optional<ClienteTienda> getCliente() { return java.util.Optional.ofNullable(cliente); }

    public List<LineaVenta> getLineas() { return Collections.unmodifiableList(lineas); }
    public List<CobroTienda> getCobros() { return Collections.unmodifiableList(cobros); }

    public double getImporteTotal() {
        return RedondeoMonetario.monetario(lineas.stream().mapToDouble(LineaVenta::getImporte).sum());
    }

    public double getTotalCobrado() {
        return RedondeoMonetario.monetario(cobros.stream().mapToDouble(CobroTienda::getImporte).sum());
    }

    public void anadirCobro(CobroTienda cobro) {
        if (confirmadaPagada) {
            throw new IllegalStateException("La venta ya está confirmada como pagada.");
        }
        cobros.add(Objects.requireNonNull(cobro, "El cobro es obligatorio."));
    }

    /** Sólo se confirma si la suma de cobros coincide exactamente con el importe. */
    public void confirmarPagada() {
        double total = getImporteTotal();
        double cobrado = getTotalCobrado();
        if (!RedondeoMonetario.iguales(total, cobrado)) {
            throw new IllegalStateException("No se puede confirmar el pago: cobrado "
                    + RedondeoMonetario.formatear(cobrado) + " €, importe "
                    + RedondeoMonetario.formatear(total) + " €.");
        }
        confirmadaPagada = true;
    }

    public boolean estaConfirmadaPagada() { return confirmadaPagada; }

    public EstadoCobro getEstadoCobro() {
        if (confirmadaPagada) return EstadoCobro.PAGADA;
        if (getTotalCobrado() <= 0) return EstadoCobro.PENDIENTE;
        return EstadoCobro.PARCIAL;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Venta otra && referenciaTicket.equals(otra.referenciaTicket);
    }

    @Override public int hashCode() { return Objects.hash(referenciaTicket); }

    @Override public String toString() {
        return referenciaTicket + " - " + fechaHora + " - "
                + RedondeoMonetario.formatear(getImporteTotal()) + " € (" + getEstadoCobro() + ")";
    }
}
