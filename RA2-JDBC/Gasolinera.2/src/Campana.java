import java.time.LocalDate;
import java.util.Objects;

/** Campaña promocional: sólo se gestiona la inscripción, no los descuentos. */
public final class Campana {

    private final Integer id;
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    public Campana(String codigo, String nombre, String descripcion,
                   LocalDate fechaInicio, LocalDate fechaFin) {
        this(null, codigo, nombre, descripcion, fechaInicio, fechaFin);
    }

    public Campana(Integer id, String codigo, String nombre, String descripcion,
                   LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.codigo = obligatorio(codigo, "El código de campaña");
        this.nombre = obligatorio(nombre, "El nombre de campaña");
        this.descripcion = obligatorio(descripcion, "La descripción de campaña");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha de inicio es obligatoria.");
        this.fechaFin = Objects.requireNonNull(fechaFin, "La fecha de fin es obligatoria.");
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        }
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatoria.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }

    @Override public boolean equals(Object o) {
        return o instanceof Campana otra && codigo.equals(otra.codigo);
    }

    @Override public int hashCode() { return Objects.hash(codigo); }

    @Override public String toString() { return codigo + " - " + nombre; }
}
