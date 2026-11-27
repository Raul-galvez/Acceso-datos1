/** Condición específica de un responsable de tienda: presupuesto de compras. */
public record ResponsableTienda(double presupuestoCompras) implements Funcion {

    public ResponsableTienda {
        if (!Double.isFinite(presupuestoCompras) || presupuestoCompras <= 0) {
            throw new IllegalArgumentException("El presupuesto de compras debe ser mayor que cero.");
        }
    }

    @Override public String codigo() { return "RESPONSABLE_TIENDA"; }

    @Override public String descripcion() {
        return "Responsable de tienda (presupuesto "
                + RedondeoMonetario.formatear(presupuestoCompras) + " €)";
    }
}
