import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Estación de servicio. Junto a su ficha de facturación forma una única alta:
 * el constructor es de paquete y la vía normal de creación es
 * {@link AltaEstacion#construir()}, que entrega estación y ficha a la vez.
 *
 * La colección es un {@link LinkedHashSet}: la igualdad de {@link Surtidor} se
 * basa en el código de equipo, así que no pueden entrar dos equipos con el mismo
 * código. El número visible se valida aparte dentro de la estación.
 */
public final class Estacion {

    private final Integer id; // null hasta que lo genera la base de datos
    private final String codigoInterno;
    private final String nombreComercial;
    private final String direccion;
    private final String telefono;
    private final LocalDate fechaApertura;
    private final ConfiguracionFacturacion configuracion;
    private final Set<Surtidor> surtidores = new LinkedHashSet<>();

    Estacion(String codigoInterno, String nombreComercial, String direccion, String telefono,
             LocalDate fechaApertura, ConfiguracionFacturacion configuracion) {
        this(null, codigoInterno, nombreComercial, direccion, telefono, fechaApertura, configuracion);
    }

    Estacion(Integer id, String codigoInterno, String nombreComercial, String direccion, String telefono,
             LocalDate fechaApertura, ConfiguracionFacturacion configuracion) {
        this.id = id;
        this.codigoInterno = obligatorio(codigoInterno, "El código interno de la estación");
        this.nombreComercial = obligatorio(nombreComercial, "El nombre comercial");
        this.direccion = obligatorio(direccion, "La dirección");
        this.telefono = obligatorio(telefono, "El teléfono");
        this.fechaApertura = Objects.requireNonNull(fechaApertura, "La fecha de apertura es obligatoria.");
        this.configuracion = Objects.requireNonNull(configuracion, "La ficha de facturación es obligatoria.");
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getCodigoInterno() { return codigoInterno; }
    public String getNombreComercial() { return nombreComercial; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public ConfiguracionFacturacion getConfiguracion() { return configuracion; }

    /** Vista de sólo lectura: así nadie puede saltarse las validaciones del alta. */
    public Set<Surtidor> getSurtidores() { return Collections.unmodifiableSet(surtidores); }

    public void añadirSurtidor(Surtidor surtidor) {
        Objects.requireNonNull(surtidor, "El surtidor es obligatorio.");
        boolean numeroRepetido = surtidores.stream()
                .anyMatch(s -> s.getNumeroVisible() == surtidor.getNumeroVisible());
        if (numeroRepetido) {
            throw new IllegalArgumentException("Ya existe en " + codigoInterno
                    + " un surtidor con el número visible " + surtidor.getNumeroVisible() + ".");
        }
        if (!surtidores.add(surtidor)) {
            throw new IllegalArgumentException("Ya existe un surtidor con el código de equipo "
                    + surtidor.getCodigoEquipo() + ".");
        }
    }

    public void darDeBajaSurtidor(Surtidor surtidor) {
        if (surtidores.size() <= 1) {
            throw new IllegalStateException("Una estación debe conservar al menos un surtidor registrado.");
        }
        if (!surtidores.remove(surtidor)) {
            throw new IllegalArgumentException("Ese surtidor no pertenece a " + codigoInterno + ".");
        }
    }

    public List<Surtidor> surtidoresOrdenados() {
        return surtidores.stream()
                .sorted(Comparator.comparingInt(Surtidor::getNumeroVisible))
                .collect(Collectors.toList());
    }

    public Optional<Surtidor> buscarSurtidor(String codigoEquipo) {
        return surtidores.stream()
                .filter(s -> s.getCodigoEquipo().equals(codigoEquipo))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Estacion otra && codigoInterno.equals(otra.codigoInterno);
    }

    @Override
    public int hashCode() { return Objects.hash(codigoInterno); }

    @Override
    public String toString() {
        return codigoInterno + " - " + nombreComercial;
    }
}
