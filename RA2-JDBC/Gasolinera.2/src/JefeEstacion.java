/** Condición específica de un jefe de estación: nivel de autorización. */
public record JefeEstacion(int nivelAutorizacion) implements Funcion {

    public JefeEstacion {
        if (nivelAutorizacion <= 0) {
            throw new IllegalArgumentException("El nivel de autorización debe ser mayor que cero.");
        }
    }

    @Override public String codigo() { return "JEFE_ESTACION"; }

    @Override public String descripcion() {
        return "Jefe de estación (nivel " + nivelAutorizacion + ")";
    }
}
