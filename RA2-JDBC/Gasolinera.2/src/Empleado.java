import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Persona que trabaja en una estación.
 *
 * La identidad (número interno, documento, antigüedad) se conserva aunque
 * cambie de estación o de funciones. Las ventas guardan su propia referencia a
 * la estación y al empleado, así que una reassignment posterior no altera el
 * histórico. Las funciones se modelan por composición: una lista de periodos,
 * no campos dentro de Empleado.
 */
public final class Empleado {

    private final Integer id; // null hasta que lo genera MySQL
    private final String numeroInterno;
    private final String documentoIdentidad;
    private final String nombre;
    private final String telefono;   // puede faltar si hay email
    private final String email;      // puede faltar si hay teléfono
    private final LocalDate fechaIncorporacion;
    private Estacion estacion;
    private Empleado supervisor;     // null = sin supervisor
    private final List<AsignacionFuncion> historialFunciones = new ArrayList<>();

    public Empleado(String numeroInterno, String documentoIdentidad, String nombre,
                    String telefono, String email, LocalDate fechaIncorporacion, Estacion estacion) {
        this(null, numeroInterno, documentoIdentidad, nombre, telefono, email, fechaIncorporacion, estacion);
    }

    public Empleado(Integer id, String numeroInterno, String documentoIdentidad, String nombre,
                    String telefono, String email, LocalDate fechaIncorporacion, Estacion estacion) {
        this.id = id;
        this.numeroInterno = obligatorio(numeroInterno, "El número interno");
        this.documentoIdentidad = obligatorio(documentoIdentidad, "El documento de identidad");
        this.nombre = obligatorio(nombre, "El nombre del empleado");
        this.telefono = opcional(telefono);
        this.email = opcional(email);
        if (this.telefono == null && this.email == null) {
            throw new IllegalArgumentException("Debe constar al menos teléfono o email del empleado.");
        }
        this.fechaIncorporacion = Objects.requireNonNull(fechaIncorporacion, "La fecha de incorporación es obligatoria.");
        this.estacion = Objects.requireNonNull(estacion, "El empleado debe estar adscrito a una estación.");
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String opcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getNumeroInterno() { return numeroInterno; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaIncorporacion() { return fechaIncorporacion; }
    public Estacion getEstacion() { return estacion; }
    public Empleado getSupervisor() { return supervisor; }

    public List<AsignacionFuncion> getHistorialFunciones() {
        return Collections.unmodifiableList(historialFunciones);
    }

    public void cambiarEstacion(Estacion nueva) {
        this.estacion = Objects.requireNonNull(nueva, "La nueva estación es obligatoria.");
    }

    public void cambiarSupervisor(Empleado nuevo) {
        this.supervisor = nuevo;
    }

    /** Añade un periodo; el solape se rechaza aquí y también en la BD de la fase siguiente. */
    public void asignarFuncion(Funcion funcion, LocalDate inicio) {
        Objects.requireNonNull(inicio, "La fecha de inicio es obligatoria.");
        AsignacionFuncion candidata = new AsignacionFuncion(funcion, inicio, null);
        boolean solape = historialFunciones.stream().anyMatch(a -> a.solapaCon(candidata));
        if (solape) {
            throw new IllegalArgumentException("Ya hay un periodo vigente o solapado de " + funcion.codigo()
                    + " para " + numeroInterno + ".");
        }
        historialFunciones.add(candidata);
    }

    /** Cierra el periodo vigente de ese tipo sin borrar los anteriores. */
    public void finalizarFuncion(String codigoFuncion, LocalDate fin) {
        Objects.requireNonNull(fin, "La fecha de fin es obligatoria.");
        int indice = indiceVigente(codigoFuncion);
        if (indice < 0) {
            throw new IllegalArgumentException(numeroInterno + " no tiene una asignación vigente de " + codigoFuncion + ".");
        }
        AsignacionFuncion anterior = historialFunciones.get(indice);
        historialFunciones.set(indice,
                new AsignacionFuncion(anterior.funcion(), anterior.inicio(), fin));
    }

    public List<Funcion> funcionesVigentes(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        return historialFunciones.stream()
                .filter(a -> a.vigenteEn(fecha))
                .map(AsignacionFuncion::funcion)
                .collect(Collectors.toList());
    }

    public Optional<AsignacionFuncion> asignacionVigente(String codigoFuncion) {
        return historialFunciones.stream()
                .filter(a -> a.vigente() && a.funcion().codigo().equals(codigoFuncion))
                .findFirst();
    }

    private int indiceVigente(String codigoFuncion) {
        for (int i = 0; i < historialFunciones.size(); i++) {
            AsignacionFuncion a = historialFunciones.get(i);
            if (a.vigente() && a.funcion().codigo().equals(codigoFuncion)) {
                return i;
            }
        }
        return -1;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Empleado otro && numeroInterno.equals(otro.numeroInterno);
    }

    @Override public int hashCode() { return Objects.hash(numeroInterno); }

    @Override public String toString() {
        return numeroInterno + " - " + nombre + " (" + estacion.getCodigoInterno() + ")";
    }
}
