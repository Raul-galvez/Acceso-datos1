import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Incidencia registrada en una estación. Sólo se conserva el responsable actual:
 * no se confunde con el supervisor del empleado ni con sus funciones.
 */
public final class Incidencia {

    private final Integer id;
    private final String referencia;
    private final String descripcion;
    private final LocalDateTime fechaApertura;
    private final Estacion estacion;
    private EstadoIncidencia estado;
    private Empleado responsable;

    public Incidencia(String referencia, String descripcion, LocalDateTime fechaApertura,
                      Estacion estacion, EstadoIncidencia estado, Empleado responsable) {
        this(null, referencia, descripcion, fechaApertura, estacion, estado, responsable);
    }

    public Incidencia(Integer id, String referencia, String descripcion, LocalDateTime fechaApertura,
                      Estacion estacion, EstadoIncidencia estado, Empleado responsable) {
        this.id = id;
        this.referencia = obligatorio(referencia, "La referencia de la incidencia");
        this.descripcion = obligatorio(descripcion, "La descripción");
        this.fechaApertura = Objects.requireNonNull(fechaApertura, "La fecha de apertura es obligatoria.");
        this.estacion = Objects.requireNonNull(estacion, "La estación es obligatoria.");
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio.");
        this.responsable = responsable;
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatoria.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getReferencia() { return referencia; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public Estacion getEstacion() { return estacion; }
    public EstadoIncidencia getEstado() { return estado; }
    public Empleado getResponsable() { return responsable; }

    public void asignarResponsable(Empleado empleado) {
        this.responsable = Objects.requireNonNull(empleado, "El responsable es obligatorio.");
        if (estado == EstadoIncidencia.ABIERTA) {
            this.estado = EstadoIncidencia.EN_CURSO;
        }
    }

    public void cambiarEstado(EstadoIncidencia nuevo) {
        this.estado = Objects.requireNonNull(nuevo, "El estado es obligatorio.");
    }

    @Override public boolean equals(Object o) {
        return o instanceof Incidencia otra && referencia.equals(otra.referencia);
    }

    @Override public int hashCode() { return Objects.hash(referencia); }

    @Override public String toString() {
        return referencia + " - " + descripcion + " [" + estado + "] "
                + (responsable == null ? "sin responsable" : responsable.getNumeroInterno());
    }
}
