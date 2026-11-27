import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Altas de ventas y cobros. No ejecuta SQL: prepara las operaciones JDBC futuras. */
public final class GestionVentas {

    private final Set<Venta> ventas = new LinkedHashSet<>();

    public GestionVentas() { }

    public GestionVentas(Collection<Venta> iniciales) {
        Objects.requireNonNull(iniciales, "Las ventas iniciales son obligatorias.").forEach(this::registrar);
    }

    public Venta registrar(Venta venta) {
        Objects.requireNonNull(venta, "La venta es obligatoria.");
        if (ventas.contains(venta)) {
            throw new IllegalArgumentException("La referencia de ticket " + venta.getReferenciaTicket()
                    + " ya existe en la empresa.");
        }
        ventas.add(venta);
        return venta;
    }

    public Venta registrar(String referenciaTicket, LocalDateTime fechaHora, Estacion estacion,
                           Empleado empleado, ClienteTienda cliente, List<LineaVenta> lineas) {
        return registrar(new Venta(referenciaTicket, fechaHora, estacion, empleado, cliente, lineas));
    }

    public void anadirCobro(Venta venta, double importe, LocalDateTime fechaHora, MedioPago medioPago) {
        Venta gestionada = obtener(venta);
        gestionada.anadirCobro(new CobroTienda(gestionada, importe, fechaHora, medioPago));
    }

    public void confirmarPagada(Venta venta) {
        obtener(venta).confirmarPagada();
    }

    public List<Venta> pendientesDeCobro() {
        return ventas.stream()
                .filter(v -> v.getEstadoCobro() != EstadoCobro.PAGADA)
                .collect(Collectors.toList());
    }

    public List<Venta> listar() { return List.copyOf(ventas); }

    public Set<Venta> getVentas() { return Collections.unmodifiableSet(ventas); }

    private Venta obtener(Venta venta) {
        Objects.requireNonNull(venta, "La venta es obligatoria.");
        return ventas.stream()
                .filter(v -> v.equals(venta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Esa venta no está registrada."));
    }
}
