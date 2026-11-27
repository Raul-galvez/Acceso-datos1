import java.util.Objects;

/**
 * Catálogo de tipos de combustible. Se persiste en su propia tabla (catálogo en
 * MySQL), de modo que un surtidor referencia una fila existente en lugar de
 * guardar texto libre.
 */
public final class TipoCombustible {

    private final Integer id; // null hasta que lo genera la base de datos
    private final String codigo;
    private final String nombre;

    public TipoCombustible(String codigo, String nombre) {
        this(null, codigo, nombre);
    }

    public TipoCombustible(Integer id, String codigo, String nombre) {
        this.id = id;
        this.codigo = obligatorio(codigo, "El código de combustible");
        this.nombre = obligatorio(nombre, "El nombre del combustible");
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
        return o instanceof TipoCombustible otro && codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    @Override
    public String toString() { return nombre; }
}
