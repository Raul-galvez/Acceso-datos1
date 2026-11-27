/**
 * Utilidad de redondeo compartida por catálogo, tickets y devoluciones.
 *
 * El dominio usa {@code double} porque es la decisión elegida para la ampliación,
 * pero todas las operaciones monetarias pasan por aquí: así el criterio de
 * redondeo (dos decimales, medio euro hacia arriba) no se repite por el código.
 * La tolerancia sólo sirve para comparar resultados ya redondeados; nunca para
 * aceptar una cantidad inválida.
 */
public final class RedondeoMonetario {

    public static final double TOLERANCIA = 1e-7;
    private static final int DECIMALES_MONETARIOS = 2;

    private RedondeoMonetario() { }

    public static double monetario(double valor) {
        comprobarValor(valor, "El importe");
        double factor = Math.pow(10, DECIMALES_MONETARIOS);
        return Math.round(valor * factor) / factor;
    }

    /** Redondea cantidades físicas a los decimales admitidos por la pesada. */
    public static double cantidad(double valor, int decimales) {
        comprobarValor(valor, "La cantidad");
        if (decimales < 0 || decimales > 6) {
            throw new IllegalArgumentException("El número de decimales debe estar entre 0 y 6.");
        }
        double factor = Math.pow(10, decimales);
        return Math.round(valor * factor) / factor;
    }

    public static boolean iguales(double a, double b) {
        return Math.abs(a - b) <= TOLERANCIA;
    }

    private static void comprobarValor(double valor, String etiqueta) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número finito.");
        }
    }

    /** Texto estable para mostrar importes sin depender de la configuración regional. */
    public static String formatear(double valor) {
        return String.format(java.util.Locale.ROOT, "%.2f", monetario(valor));
    }
}
