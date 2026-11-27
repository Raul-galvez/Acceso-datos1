import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Tarjeta de fidelización. Puede registrarse antes de entregarse: en ese caso
 * no tiene titular ni fecha de asignación. Un cliente también puede no tenerla.
 */
public final class TarjetaFidelizacion {

    private final Integer id;
    private final String numeroTarjeta;
    private final LocalDate fechaEmision;
    private LocalDate fechaAsignacion;
    private ClienteTienda titular;

    public TarjetaFidelizacion(String numeroTarjeta, LocalDate fechaEmision) {
        this(null, numeroTarjeta, fechaEmision);
    }

    public TarjetaFidelizacion(Integer id, String numeroTarjeta, LocalDate fechaEmision) {
        if (numeroTarjeta == null || numeroTarjeta.isBlank()) {
            throw new IllegalArgumentException("El número de tarjeta es obligatorio.");
        }
        this.id = id;
        this.numeroTarjeta = numeroTarjeta.trim();
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "La fecha de emisión es obligatoria.");
    }

    public Integer getId() { return id; }
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public LocalDate getFechaAsignacion() { return fechaAsignacion; }

    public void asignar(ClienteTienda cliente, LocalDate fecha) {
        ClienteTienda nuevoTitular = Objects.requireNonNull(cliente, "El titular es obligatorio.");
        LocalDate nuevaFecha = Objects.requireNonNull(fecha, "La fecha de asignación es obligatoria.");
        if (nuevaFecha.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("No se puede asignar una tarjeta antes de emitirla.");
        }
        this.titular = nuevoTitular;
        this.fechaAsignacion = nuevaFecha;
    }

    public void liberar() {
        this.titular = null;
        this.fechaAsignacion = null;
    }

    public Optional<ClienteTienda> getTitular() { return Optional.ofNullable(titular); }

    public boolean estaAsignada() { return titular != null; }

    @Override public boolean equals(Object o) {
        return o instanceof TarjetaFidelizacion otra && numeroTarjeta.equals(otra.numeroTarjeta);
    }

    @Override public int hashCode() { return Objects.hash(numeroTarjeta); }

    @Override public String toString() {
        return numeroTarjeta + (estaAsignada() ? " -> " + titular : " (sin titular)");
    }
}
