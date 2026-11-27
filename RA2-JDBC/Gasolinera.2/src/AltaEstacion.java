import java.time.LocalDate;
import java.util.Objects;

/**
 * Datos del alta conjunta de una estación. El caso dice que estación y ficha de
 * facturación son una única alta, así que se agrupan aquí y se construyen juntas.
 *
 * Es un record porque es un transporte inmutable de datos de entrada: no necesita
 * herencia ni comportamiento propio aparte de la validación y de {@link #construir()}.
 * La atomicidad real en MySQL se conseguirá en la fase JDBC con una transacción
 * (INSERT de la ficha + INSERT de la estación + Commit).
 */
public record AltaEstacion(
        String codigoInterno,
        String nombreComercial,
        String direccion,
        String telefono,
        LocalDate fechaApertura,
        ConfiguracionFacturacion configuracion,
        String codigoEquipo,
        int numeroVisible,
        TipoCombustible tipoCombustible,
        EstadoServicio estadoInicial) {

    public AltaEstacion {
        Objects.requireNonNull(fechaApertura, "La fecha de apertura es obligatoria.");
        Objects.requireNonNull(configuracion, "La ficha de facturación es obligatoria: el alta es conjunta.");
        Objects.requireNonNull(tipoCombustible, "El tipo de combustible del primer surtidor es obligatorio.");
        Objects.requireNonNull(estadoInicial, "El estado inicial del primer surtidor es obligatorio.");
        if (numeroVisible <= 0) {
            throw new IllegalArgumentException("El número visible debe ser mayor que cero.");
        }
    }

    /** Crea la ficha, la estación y su primer surtidor como un único conjunto válido. */
    public Estacion construir() {
        Estacion estacion = new Estacion(codigoInterno, nombreComercial, direccion, telefono,
                fechaApertura, configuracion);
        estacion.añadirSurtidor(new Surtidor(codigoEquipo, numeroVisible, tipoCombustible,
                estadoInicial, estacion));
        return estacion;
    }
}
