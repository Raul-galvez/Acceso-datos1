import java.util.Objects;

/**
 * Cliente registrado de la tienda. Se distingue del {@link Cliente} heredado
 * de CSV/JSON: aquí la referencia de negocio es el código de cliente y los
 * medios de contacto son opcionales, con la condición de que conste al menos uno.
 */
public final class ClienteTienda {

    private final Integer id;
    private final String codigoCliente;
    private final String nombre;
    private final String telefono;
    private final String email;

    public ClienteTienda(String codigoCliente, String nombre, String telefono, String email) {
        this(null, codigoCliente, nombre, telefono, email);
    }

    public ClienteTienda(Integer id, String codigoCliente, String nombre, String telefono, String email) {
        this.id = id;
        this.codigoCliente = obligatorio(codigoCliente, "El código de cliente");
        this.nombre = obligatorio(nombre, "El nombre del cliente");
        this.telefono = opcional(telefono);
        this.email = opcional(email);
        if (this.telefono == null && this.email == null) {
            throw new IllegalArgumentException("Debe constar teléfono o email del cliente registrado.");
        }
    }

    private static String obligatorio(String valor, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Integer getId() { return id; }
    public String getCodigoCliente() { return codigoCliente; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    @Override public boolean equals(Object o) {
        return o instanceof ClienteTienda otro && codigoCliente.equals(otro.codigoCliente);
    }

    @Override public int hashCode() { return Objects.hash(codigoCliente); }

    @Override public String toString() { return codigoCliente + " - " + nombre; }
}
