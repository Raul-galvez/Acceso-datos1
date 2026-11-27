import java.util.Objects;

/** Proveedor con el que ya existe un acuerdo de suministro. */
public final class Proveedor {

    private final Integer id;
    private final String codigoInterno;
    private final String identificacionFiscal;
    private final String razonSocial;
    private final String telefono;
    private final String email;

    public Proveedor(String codigoInterno, String identificacionFiscal, String razonSocial,
                     String telefono, String email) {
        this(null, codigoInterno, identificacionFiscal, razonSocial, telefono, email);
    }

    public Proveedor(Integer id, String codigoInterno, String identificacionFiscal, String razonSocial,
                     String telefono, String email) {
        this.id = id;
        this.codigoInterno = obligatorio(codigoInterno, "El código interno del proveedor");
        this.identificacionFiscal = obligatorio(identificacionFiscal, "La identificación fiscal");
        this.razonSocial = obligatorio(razonSocial, "La razón social");
        this.telefono = opcional(telefono);
        this.email = opcional(email);
        if (this.telefono == null && this.email == null) {
            throw new IllegalArgumentException("Debe constar teléfono o email del proveedor.");
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
    public String getCodigoInterno() { return codigoInterno; }
    public String getIdentificacionFiscal() { return identificacionFiscal; }
    public String getRazonSocial() { return razonSocial; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    @Override public boolean equals(Object o) {
        return o instanceof Proveedor otro && codigoInterno.equals(otro.codigoInterno);
    }

    @Override public int hashCode() { return Objects.hash(codigoInterno); }

    @Override public String toString() { return codigoInterno + " - " + razonSocial; }
}
