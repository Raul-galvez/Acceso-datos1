import java.util.Objects;

/**
 * Catálogo de estados de servicio de un surtidor. Como {@link TipoCombustible},
 * se guarda como tabla catálogo en MySQL y se referencia por clave foránea.
 */
public final class EstadoServicio {

    private final Integer id; // null hasta que lo genera la base de datos
    private final String codigo;
    private final String nombre;

    public EstadoServicio(String codigo, String nombre) {
        this(null, codigo, nombre);
    }

    public EstadoServicio(Integer id, String codigo, String nombre) {
        this.id = id;
        this.codigo = obligatorio(codigo, "El código de estado");
        this.nombre = obligatorio(nombre, "El nombre del estado");
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        return valor.trim();
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }

    @Override
    public boolean equals(Object o) {
        return o instanceof EstadoServicio otro && codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    @Override
    public String toString() { return nombre; }
}
