import java.util.Objects;

/**
 * Ficha propia de configuración de facturación de una estación.
 *
 * Se modela como clase independiente (y no como campos dentro de {@link Estacion})
 * porque el caso la describe como una ficha con identidad propia: soporte la
 * identifica por su código de configuración. En MySQL estacion.configuracion_id
 * es una FK NOT NULL y UNIQUE, lo que garantiza la relación 1:1.
 */
public final class ConfiguracionFacturacion {

    private final Integer id; // null hasta que lo genera la base de datos
    private final String codigoConfiguracion;
    private final String serieTickets;
    private int siguienteNumero;
    private String textoPie;

    public ConfiguracionFacturacion(String codigoConfiguracion, String serieTickets,
                                    int siguienteNumero, String textoPie) {
        this(null, codigoConfiguracion, serieTickets, siguienteNumero, textoPie);
    }

    public ConfiguracionFacturacion(Integer id, String codigoConfiguracion, String serieTickets,
                                    int siguienteNumero, String textoPie) {
        this.id = id;
        this.codigoConfiguracion = obligatorio(codigoConfiguracion, "El código de configuración");
        this.serieTickets = obligatorio(serieTickets, "La serie de tickets");
        if (siguienteNumero <= 0) {
            throw new IllegalArgumentException("El siguiente número de ticket debe ser mayor que cero.");
        }
        this.siguienteNumero = siguienteNumero;
        this.textoPie = obligatorio(textoPie, "El texto del pie de justificante");
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getCodigoConfiguracion() { return codigoConfiguracion; }
    public String getSerieTickets() { return serieTickets; }
    public int getSiguienteNumero() { return siguienteNumero; }
    public String getTextoPie() { return textoPie; }

    public void setTextoPie(String textoPie) {
        this.textoPie = obligatorio(textoPie, "El texto del pie de justificante");
    }

    /** Devuelve el número de ticket reservado y deja preparado el siguiente. */
    public String reservarNumeroTicket() {
        return serieTickets + String.format("%06d", siguienteNumero++);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ConfiguracionFacturacion otra
                && codigoConfiguracion.equals(otra.codigoConfiguracion);
    }

    @Override
    public int hashCode() { return Objects.hash(codigoConfiguracion); }

    @Override
    public String toString() {
        return codigoConfiguracion + " (serie " + serieTickets + ", siguiente " + siguienteNumero + ")";
    }
}
