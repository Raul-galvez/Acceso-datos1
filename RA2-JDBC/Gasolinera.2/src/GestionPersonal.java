import java.time.LocalDate;
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
 * Reglas de alta, adscripción, supervisión y funciones del personal.
 * No ejecuta SQL: la fase JDBC traducirá estas operaciones a transacciones.
 */
public final class GestionPersonal {

    private final Set<Empleado> empleados = new LinkedHashSet<>();

    public GestionPersonal() { }

    public GestionPersonal(Collection<Empleado> iniciales) {
        Objects.requireNonNull(iniciales, "La colección inicial es obligatoria.").forEach(this::darDeAlta);
    }

    public Empleado darDeAlta(Empleado empleado) {
        Objects.requireNonNull(empleado, "El empleado es obligatorio.");
        if (buscarPorNumero(empleado.getNumeroInterno()).isPresent()) {
            throw new IllegalArgumentException("Ya existe el número interno " + empleado.getNumeroInterno() + ".");
        }
        boolean documentoRepetido = empleados.stream()
                .anyMatch(e -> e.getDocumentoIdentidad().equals(empleado.getDocumentoIdentidad()));
        if (documentoRepetido) {
            throw new IllegalArgumentException("Ese documento de identidad ya está registrado.");
        }
        validarSupervisor(empleado, empleado.getSupervisor());
        empleados.add(empleado);
        return empleado;
    }

    public void cambiarSupervisor(Empleado empleado, Empleado supervisor) {
        Empleado gestionado = obtenerGestionada(empleado);
        validarSupervisor(gestionado, supervisor);
        gestionado.cambiarSupervisor(supervisor);
    }

    public void cambiarEstacion(Empleado empleado, Estacion estacion) {
        obtenerGestionada(empleado).cambiarEstacion(estacion);
    }

    public void asignarFuncion(Empleado empleado, Funcion funcion, LocalDate inicio) {
        obtenerGestionada(empleado).asignarFuncion(funcion, inicio);
    }

    public void finalizarFuncion(Empleado empleado, String codigoFuncion, LocalDate fin) {
        obtenerGestionada(empleado).finalizarFuncion(codigoFuncion, fin);
    }

    public List<Funcion> funcionesVigentes(Empleado empleado, LocalDate fecha) {
        return obtenerGestionada(empleado).funcionesVigentes(fecha);
    }

    public Optional<Empleado> buscarPorNumero(String numeroInterno) {
        return empleados.stream()
                .filter(e -> e.getNumeroInterno().equals(numeroInterno))
                .findFirst();
    }

    public List<Empleado> sinSupervisor() {
        return empleados.stream()
                .filter(e -> e.getSupervisor() == null)
                .sorted(Comparator.comparing(Empleado::getNumeroInterno))
                .collect(Collectors.toList());
    }

    public List<Empleado> supervisoresDe(Empleado supervisor) {
        return empleados.stream()
                .filter(e -> e.getSupervisor() != null && e.getSupervisor().equals(supervisor))
                .collect(Collectors.toList());
    }

    public List<Empleado> listar() {
        return empleados.stream()
                .sorted(Comparator.comparing(Empleado::getNumeroInterno))
                .collect(Collectors.toList());
    }

    public Set<Empleado> getEmpleados() { return Collections.unmodifiableSet(empleados); }

    private Empleado obtenerGestionada(Empleado empleado) {
        Objects.requireNonNull(empleado, "El empleado es obligatorio.");
        return buscarPorNumero(empleado.getNumeroInterno())
                .orElseThrow(() -> new IllegalArgumentException("Ese empleado no está dado de alta."));
    }

    private void validarSupervisor(Empleado empleado, Empleado supervisor) {
        if (supervisor == null) {
            return;
        }
        Empleado supervisorGestionado = obtenerGestionada(supervisor);
        if (supervisorGestionado.equals(empleado)) {
            throw new IllegalArgumentException("Nadie puede supervisarse a sí mismo.");
        }
        // Si el candidato ya depende del empleado, cerrar el ciclo crearía un bucle.
        Empleado actual = supervisorGestionado.getSupervisor();
        while (actual != null) {
            if (actual.equals(empleado)) {
                throw new IllegalArgumentException("La supervisión no puede formar un ciclo.");
            }
            actual = actual.getSupervisor();
        }
    }
}
