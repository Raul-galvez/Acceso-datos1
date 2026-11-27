import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de dominio de este apartado: concentra las reglas de alta, baja y
 * consulta de estaciones para que {@code Main} no tenga que conocerlas.
 * No abre conexiones ni ejecuta SQL: la persistencia MySQL se añadirá después.
 */
public final class GestionEstaciones {

    private final Set<Estacion> estaciones = new LinkedHashSet<>();

    public GestionEstaciones() { }

    public GestionEstaciones(Collection<Estacion> iniciales) {
        Objects.requireNonNull(iniciales, "La colección inicial es obligatoria.").forEach(estaciones::add);
    }

    public Estacion darDeAlta(AltaEstacion alta) {
        Objects.requireNonNull(alta, "El alta es obligatoria.");
        if (buscarPorCodigo(alta.codigoInterno()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una estación con el código " + alta.codigoInterno() + ".");
        }
        String codigoFicha = alta.configuracion().getCodigoConfiguracion();
        boolean fichaRepetida = estaciones.stream()
                .map(e -> e.getConfiguracion().getCodigoConfiguracion())
                .anyMatch(c -> c.equals(codigoFicha));
        if (fichaRepetida) {
            throw new IllegalArgumentException("La ficha " + codigoFicha + " ya está asignada a otra estación.");
        }
        if (existeEquipo(alta.codigoEquipo())) {
            throw new IllegalArgumentException("Ya existe el equipo " + alta.codigoEquipo() + " en la empresa.");
        }
        Estacion estacion = alta.construir();
        estaciones.add(estacion);
        return estacion;
    }

    public void añadirSurtidor(Estacion estacion, Surtidor surtidor) {
        Objects.requireNonNull(surtidor, "El surtidor es obligatorio.");
        if (existeEquipo(surtidor.getCodigoEquipo())) {
            throw new IllegalArgumentException("Ya existe el equipo " + surtidor.getCodigoEquipo() + " en la empresa.");
        }
        obtenerGestionada(estacion).añadirSurtidor(surtidor);
    }

    public void darDeBajaSurtidor(Estacion estacion, Surtidor surtidor) {
        obtenerGestionada(estacion).darDeBajaSurtidor(surtidor);
    }

    public Optional<Estacion> buscarPorCodigo(String codigoInterno) {
        return estaciones.stream()
                .filter(e -> e.getCodigoInterno().equals(codigoInterno))
                .findFirst();
    }

    public List<Estacion> listar() {
        return estaciones.stream()
                .sorted(Comparator.comparing(Estacion::getCodigoInterno))
                .collect(Collectors.toList());
    }

    public List<Surtidor> todosLosSurtidores() {
        return estaciones.stream()
                .flatMap(e -> e.getSurtidores().stream())
                .collect(Collectors.toList());
    }

    public Set<Estacion> getEstaciones() { return Collections.unmodifiableSet(estaciones); }

    private Estacion obtenerGestionada(Estacion estacion) {
        return buscarPorCodigo(Objects.requireNonNull(estacion, "La estación es obligatoria.").getCodigoInterno())
                .orElseThrow(() -> new IllegalArgumentException("Esa estación no está dada de alta."));
    }

    private boolean existeEquipo(String codigoEquipo) {
        return todosLosSurtidores().stream()
                .anyMatch(s -> s.getCodigoEquipo().equals(codigoEquipo));
    }
}
