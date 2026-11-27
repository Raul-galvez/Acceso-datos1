/** Producto envasado: unidades completas y formato del envase. */
public record Envasado(String formatoEnvase) implements ModalidadVenta {

    private static final double TOLERANCIA_UNIDAD = 1e-9;

    public Envasado {
        if (formatoEnvase == null || formatoEnvase.isBlank()) {
            throw new IllegalArgumentException("El formato del envase es obligatorio.");
        }
        formatoEnvase = formatoEnvase.trim();
    }

    @Override public String codigo() { return "ENVASADO"; }

    @Override public double normalizarCantidad(double cantidad) {
        if (!Double.isFinite(cantidad) || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un número positivo de unidades.");
        }
        double unidades = Math.rint(cantidad);
        if (Math.abs(cantidad - unidades) > TOLERANCIA_UNIDAD) {
            throw new IllegalArgumentException(
                    "Un producto envasado se vende por unidades completas; 1,5 no es válido.");
        }
        return unidades;
    }

    @Override public double calcularImporte(double cantidad, double precioUnitario) {
        validarPrecio(precioUnitario);
        return RedondeoMonetario.monetario(normalizarCantidad(cantidad) * precioUnitario);
    }

    private static void validarPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero.");
        }
    }
}
