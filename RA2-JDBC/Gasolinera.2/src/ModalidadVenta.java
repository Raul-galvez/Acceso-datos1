/**
 * Estrategia de venta de un producto.
 *
 * Interfaz sellada con dos implementaciones: el código que prepara líneas de
 * ticket llama siempre a estos métodos y no necesita preguntar si el producto
 * es envasado o granel. Añadir una modalidad obliga a declararla aquí, por lo
 * que el compilador avisa de los lugares que deben revisarse.
 */
public sealed interface ModalidadVenta permits Envasado, Granel {

    String codigo();

    /** Valida la cantidad y la devuelve expresada en la unidad de la modalidad. */
    double normalizarCantidad(double cantidad);

    /** Importe ya redondeado según el criterio común del caso. */
    double calcularImporte(double cantidad, double precioUnitario);
}
