import java.time.LocalDate;
import java.util.Objects;

/** Inscripción de un cliente registrado en una campaña. */
public record InscripcionCampana(Integer id, Campana campana, ClienteTienda cliente,
                                 LocalDate fechaInscripcion) {

    public InscripcionCampana {
        Objects.requireNonNull(campana, "La campaña es obligatoria.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(fechaInscripcion, "La fecha de inscripción es obligatoria.");
    }
}
