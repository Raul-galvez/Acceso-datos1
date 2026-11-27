import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Alta y asignación actual de incidencias por estación. */
public final class GestionIncidencias {

    private final Set<Incidencia> incidencias = new LinkedHashSet<>();

    public GestionIncidencias() { }

    public GestionIncidencias(Collection<Incidencia> iniciales) {
        Objects.requireNonNull(iniciales, "Las incidencias iniciales son obligatorias.")
                .forEach(this::registrar);
    }

    public Incidencia registrar(Incidencia incidencia) {
        Objects.requireNonNull(incidencia, "La incidencia es obligatoria.");
        if (incidencias.contains(incidencia)) {
            throw new IllegalArgumentException("La referencia " + incidencia.getReferencia()
                    + " ya está registrada.");
        }
        incidencias.add(incidencia);
        return incidencia;
    }

    public void asignar(Incidencia incidencia, Empleado responsable) {
        obtener(incidencia).asignarResponsable(responsable);
    }

    public List<Incidencia> sinResponsable() {
        return incidencias.stream()
                .filter(i -> i.getResponsable() == null)
                .collect(Collectors.toList());
    }

    public List<Incidencia> de(Empleado responsable) {
        return incidencias.stream()
                .filter(i -> i.getResponsable() != null && i.getResponsable().equals(responsable))
                .collect(Collectors.toList());
    }

    public Set<Incidencia> getIncidencias() { return Collections.unmodifiableSet(incidencias); }

    private Incidencia obtener(Incidencia incidencia) {
        Objects.requireNonNull(incidencia, "La incidencia es obligatoria.");
        return incidencias.stream()
                .filter(i -> i.equals(incidencia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Esa incidencia no está registrada."));
    }
}
