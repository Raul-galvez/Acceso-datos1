import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cobro de una venta de tienda. Se mantiene separado del {@link Pago} de
 * repostaje de CSV/JSON para no reinterpretar los registros antiguos. En MySQL
 * ambos podrán convivir en su propia tabla con una correspondencia documentada.
 */
public final class CobroTienda {

    private final Integer id;
    private final Venta venta;
    private final double importe;
    private final LocalDateTime fechaHora;
    private final MedioPago medioPago;

    public CobroTienda(Venta venta, double importe, LocalDateTime fechaHora, MedioPago medioPago) {
        this(null, venta, importe, fechaHora, medioPago);
    }

    public CobroTienda(Integer id, Venta venta, double importe,
                       LocalDateTime fechaHora, MedioPago medioPago) {
        this.id = id;
        this.venta = Objects.requireNonNull(venta, "La venta es obligatoria.");
        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe cobrado debe ser mayor que cero.");
        }
        this.importe = RedondeoMonetario.monetario(importe);
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora del cobro son obligatorias.");
        this.medioPago = Objects.requireNonNull(medioPago, "El medio de pago es obligatorio.");
    }

    public Integer getId() { return id; }
    public Venta getVenta() { return venta; }
    public double getImporte() { return importe; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public MedioPago getMedioPago() { return medioPago; }

    @Override public String toString() {
        return RedondeoMonetario.formatear(importe) + " € por " + medioPago;
    }
}
