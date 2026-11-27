/** Producto a granel: cantidad decimal compatible con la precisión de pesada. */
public record Granel(double precisionPesada) implements ModalidadVenta {

    private static final double TOLERANCIA = 1e-7;

    public Granel {
        if (!Double.isFinite(precisionPesada) || precisionPesada <= 0 || precisionPesada > 1) {
            throw new IllegalArgumentException("La precisión de pesada debe estar entre 0 y 1.");
        }
    }

    @Override public String codigo() { return "GRANEL"; }

    /** Número de decimales necesarios para representar la precisión (0,05 -> 2). */
    public int decimales() {
        double escalado = precisionPesada;
        int decimales = 0;
        while (escalado < 1 && decimales < 6) {
            escalado *= 10;
            decimales++;
        }
        return decimales;
    }

    @Override public double normalizarCantidad(double cantidad) {
        if (!Double.isFinite(cantidad) || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un número positivo de kilogramos.");
        }
        double pasos = cantidad / precisionPesada;
        double pasosRedondeados = Math.rint(pasos);
        if (Math.abs(pasos - pasosRedondeados) > TOLERANCIA) {
            throw new IllegalArgumentException("La cantidad no respeta la precisión de pesada "
                    + precisionPesada + " kg.");
        }
        return RedondeoMonetario.cantidad(pasosRedondeados * precisionPesada, decimales());
    }

    @Override public double calcularImporte(double cantidad, double precioUnitario) {
        if (!Double.isFinite(precioUnitario) || precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero.");
        }
        return RedondeoMonetario.monetario(normalizarCantidad(cantidad) * precioUnitario);
    }
}
