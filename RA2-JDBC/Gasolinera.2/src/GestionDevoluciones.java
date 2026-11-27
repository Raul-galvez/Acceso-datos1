import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Control de devoluciones acumuladas.
 *
 * La regla "no devolver más de lo comprado" se comprueba aquí sumando todas las
 * devoluciones anteriores de la línea, y también se refleja en el diseño SQL.
 * La protección frente a operaciones simultáneas queda pendiente de la fase JDBC
 * (bloqueo de la línea o restricción en el motor).
 */
public final class GestionDevoluciones {

    private final Map<LineaVenta, List<Devolucion>> porLinea = new LinkedHashMap<>();

    public Devolucion registrar(LineaVenta linea, double cantidad, String motivo,
                                LocalDateTime fechaHora, Empleado empleado, boolean aptaParaReventa) {
        LineaVenta gestionada = Objects.requireNonNull(linea, "La línea es obligatoria.");
        if (gestionada.getVenta() == null || gestionada.getVenta().getEstadoCobro() != EstadoCobro.PAGADA) {
            throw new IllegalStateException("Sólo se pueden devolver líneas de ventas ya pagadas.");
        }
        Devolucion nueva = new Devolucion(gestionada, cantidad, motivo, fechaHora,
                empleado, aptaParaReventa);

        validarLimiteCajero(empleado, nueva);

        List<Devolucion> anteriores = porLinea.computeIfAbsent(gestionada, k -> new ArrayList<>());
        double cantidadTotal = anteriores.stream().mapToDouble(Devolucion::getCantidad).sum()
                + nueva.getCantidad();
        double importeTotal = anteriores.stream().mapToDouble(Devolucion::getImporteReintegrado).sum()
                + nueva.getImporteReintegrado();
        if (cantidadTotal > gestionada.getCantidad() + RedondeoMonetario.TOLERANCIA) {
            throw new IllegalStateException("Las devoluciones superan la cantidad comprada en la línea "
                    + gestionada.getNumeroOrden() + ".");
        }
        if (importeTotal > gestionada.getImporte() + RedondeoMonetario.TOLERANCIA) {
            throw new IllegalStateException("Las devoluciones superan el importe original de la línea.");
        }
        anteriores.add(nueva);
        return nueva;
    }

    public List<Devolucion> de(LineaVenta linea) {
        return porLinea.getOrDefault(linea, List.of()).stream().collect(Collectors.toUnmodifiableList());
    }

    public List<Devolucion> todas() {
        return porLinea.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<LineaVenta, List<Devolucion>> getPorLinea() {
        return Collections.unmodifiableMap(porLinea);
    }

    private void validarLimiteCajero(Empleado empleado, Devolucion devolucion) {
        LocalDate hoy = devolucion.getFechaHora().toLocalDate();
        empleado.funcionesVigentes(hoy).stream()
                .filter(f -> f instanceof Cajero)
                .map(f -> (Cajero) f)
                .findFirst()
                .ifPresent(cajero -> {
                    if (devolucion.getImporteReintegrado() > cajero.limiteDevoluciones()) {
                        throw new IllegalStateException("El reintegro supera el límite autorizado del cajero.");
                    }
                });
    }
}
