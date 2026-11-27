/** Condición específica de un cajero: límite autorizado para devoluciones. */
public record Cajero(double limiteDevoluciones) implements Funcion {

    public Cajero {
        if (!Double.isFinite(limiteDevoluciones) || limiteDevoluciones <= 0) {
            throw new IllegalArgumentException("El límite de devoluciones debe ser mayor que cero.");
        }
    }

    @Override public String codigo() { return "CAJERO"; }

    @Override public String descripcion() {
        return "Cajero (límite devoluciones " + RedondeoMonetario.formatear(limiteDevoluciones) + " €)";
    }
}
