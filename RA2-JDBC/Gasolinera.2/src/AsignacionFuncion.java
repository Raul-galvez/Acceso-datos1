import java.time.LocalDate;
import java.util.Objects;

/**
 * Periodo en el que una persona desempeñó una función concreta.
 *
 * {@code fin == null} significa "vigente actualmente". Se guarda el historial: al
 * retirar y recuperar una función se crea un periodo nuevo, nunca se reescribe
 * el anterior. Dos periodos de la MISMA función no pueden solaparse; funciones
 * distintas sí pueden coexistir.
 */
public record AsignacionFuncion(Funcion funcion, LocalDate inicio, LocalDate fin) {

    public AsignacionFuncion {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(inicio, "La fecha de inicio es obligatoria.");
        if (fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio.");
        }
    }

    public boolean vigente() { return fin == null; }

    public boolean vigenteEn(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        return !fecha.isBefore(inicio) && (fin == null || !fecha.isAfter(fin));
    }

    public boolean solapaCon(AsignacionFuncion otra) {
        if (otra == null || !funcion.codigo().equals(otra.funcion().codigo())) {
            return false;
        }
        LocalDate finProp = fin == null ? LocalDate.MAX : fin;
        LocalDate finOtra = otra.fin() == null ? LocalDate.MAX : otra.fin();
        return !inicio.isAfter(finOtra) && !otra.inicio().isAfter(finProp);
    }

    @Override public String toString() {
        return funcion.descripcion() + " [" + inicio + " - " + (fin == null ? "vigente" : fin) + "]";
    }
}
