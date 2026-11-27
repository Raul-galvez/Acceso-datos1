/**
 * Función que puede desempeñar un empleado.
 *
 * Interfaz sellada: el caso menciona cuatro funciones y prevé añadir otras.
 * Cada nueva función se declara aquí como una implementación permitida, con sus
 * propias condiciones, sin modificar las clases ya existentes ni las firmas de
 * {@link Empleado} y {@link GestionPersonal}. Es una composición, no herencia:
 * una asignación contiene una función, no "es" una función.
 */
public sealed interface Funcion permits Cajero, Expendedor, ResponsableTienda, JefeEstacion {

    /** Código estable que se guarda en MySQL y sirve para distinguir el tipo. */
    String codigo();

    /** Descripción legible para la demostración y los informes. */
    String descripcion();
}
