import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Inscripciones: se permite inscribirse sin tarjeta, pero no dos veces. */
public final class GestionCampanas {

    private final Set<Campana> campanas = new LinkedHashSet<>();
    private final Set<InscripcionCampana> inscripciones = new LinkedHashSet<>();

    public GestionCampanas(Collection<Campana> campanasIniciales) {
        Objects.requireNonNull(campanasIniciales).forEach(this::registrar);
    }

    public Campana registrar(Campana campana) {
        Objects.requireNonNull(campana, "La campaña es obligatoria.");
        if (campanas.contains(campana)) {
            throw new IllegalArgumentException("Ya existe la campaña " + campana.getCodigo() + ".");
        }
        campanas.add(campana);
        return campana;
    }

    public InscripcionCampana inscribir(Campana campana, ClienteTienda cliente, LocalDate fecha) {
        if (!campanas.contains(campana)) {
            throw new IllegalArgumentException("La campaña no está registrada.");
        }
        boolean repetida = inscripciones.stream()
                .anyMatch(i -> i.campana().equals(campana) && i.cliente().equals(cliente));
        if (repetida) {
            throw new IllegalArgumentException("Ese cliente ya está inscrito en " + campana.getCodigo() + ".");
        }
        InscripcionCampana candidata = new InscripcionCampana(null, campana, cliente, fecha);
        inscripciones.add(candidata);
        return candidata;
    }

    public List<InscripcionCampana> inscripcionesDe(Campana campana) {
        return inscripciones.stream().filter(i -> i.campana().equals(campana)).collect(Collectors.toList());
    }

    public List<InscripcionCampana> campanasDe(ClienteTienda cliente) {
        return inscripciones.stream().filter(i -> i.cliente().equals(cliente)).collect(Collectors.toList());
    }

    public List<Campana> sinInscritos() {
        return campanas.stream()
                .filter(c -> inscripcionesDe(c).isEmpty())
                .collect(Collectors.toList());
    }

    public Set<Campana> getCampanas() { return Collections.unmodifiableSet(campanas); }
    public Set<InscripcionCampana> getInscripciones() { return Collections.unmodifiableSet(inscripciones); }
}
