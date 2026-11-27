import java.util.Objects;

/**
 * Equipo de una estación. Siempre pertenece a una estación: no se permite crear
 * un surtidor "sin destino". El código de equipo es único en toda la empresa;
 * el número visible sólo debe ser único dentro de su estación.
 */
public final class Surtidor {

    private final Integer id; // null hasta que lo genera la base de datos
    private final String codigoEquipo;
    private final int numeroVisible;
    private final TipoCombustible tipoCombustible;
    private final Estacion estacion;
    private EstadoServicio estadoServicio;

    public Surtidor(String codigoEquipo, int numeroVisible, TipoCombustible tipoCombustible,
                    EstadoServicio estadoServicio, Estacion estacion) {
        this(null, codigoEquipo, numeroVisible, tipoCombustible, estadoServicio, estacion);
    }

    public Surtidor(Integer id, String codigoEquipo, int numeroVisible, TipoCombustible tipoCombustible,
                    EstadoServicio estadoServicio, Estacion estacion) {
        if (codigoEquipo == null || codigoEquipo.isBlank()) {
            throw new IllegalArgumentException("El código de equipo es obligatorio.");
        }
        if (numeroVisible <= 0) {
            throw new IllegalArgumentException("El número visible debe ser mayor que cero.");
        }
        this.id = id;
        this.codigoEquipo = codigoEquipo.trim();
        this.numeroVisible = numeroVisible;
        this.tipoCombustible = Objects.requireNonNull(tipoCombustible, "El tipo de combustible es obligatorio.");
        this.estadoServicio = Objects.requireNonNull(estadoServicio, "El estado de servicio es obligatorio.");
        this.estacion = Objects.requireNonNull(estacion, "Un surtidor debe pertenecer a una estación.");
    }

    public Integer getId() { return id; }
    public String getCodigoEquipo() { return codigoEquipo; }
    public int getNumeroVisible() { return numeroVisible; }
    public TipoCombustible getTipoCombustible() { return tipoCombustible; }
    public EstadoServicio getEstadoServicio() { return estadoServicio; }
    public Estacion getEstacion() { return estacion; }

    /** Un surtidor averiado sigue perteneciendo a su estación; sólo cambia de estado. */
    public void cambiarEstado(EstadoServicio nuevoEstado) {
        this.estadoServicio = Objects.requireNonNull(nuevoEstado, "El estado de servicio es obligatorio.");
    }

    public boolean perteneceA(Estacion otra) {
        return otra != null && estacion.getCodigoInterno().equals(otra.getCodigoInterno());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Surtidor otro && codigoEquipo.equals(otro.codigoEquipo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigoEquipo); }

    @Override
    public String toString() {
        return codigoEquipo + " (nº " + numeroVisible + ", " + tipoCombustible + ", " + estadoServicio + ")";
    }
}
