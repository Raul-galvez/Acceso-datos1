import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Reglas de alta y asignación 1:1 entre clientes registrados y tarjetas. */
public final class GestionFidelizacion {

    private final Set<ClienteTienda> clientes = new LinkedHashSet<>();
    private final Set<TarjetaFidelizacion> tarjetas = new LinkedHashSet<>();

    public GestionFidelizacion() { }

    public GestionFidelizacion(Collection<ClienteTienda> clientesIniciales) {
        Objects.requireNonNull(clientesIniciales, "Los clientes iniciales son obligatorios.")
                .forEach(this::registrarCliente);
    }

    public ClienteTienda registrarCliente(ClienteTienda cliente) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        if (clientes.contains(cliente)) {
            throw new IllegalArgumentException("El cliente " + cliente.getCodigoCliente() + " ya está registrado.");
        }
        clientes.add(cliente);
        return cliente;
    }

    public TarjetaFidelizacion registrarTarjeta(TarjetaFidelizacion tarjeta) {
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");
        if (tarjetas.contains(tarjeta)) {
            throw new IllegalArgumentException("La tarjeta " + tarjeta.getNumeroTarjeta() + " ya está registrada.");
        }
        tarjetas.add(tarjeta);
        return tarjeta;
    }

    public void asignar(TarjetaFidelizacion tarjeta, ClienteTienda cliente, LocalDate fecha) {
        TarjetaFidelizacion gestionada = obtener(tarjeta);
        if (!clientes.contains(cliente)) {
            throw new IllegalArgumentException("El cliente debe estar registrado antes de recibir la tarjeta.");
        }
        boolean clienteConTarjeta = tarjetas.stream()
                .filter(t -> !t.equals(gestionada))
                .anyMatch(t -> t.getTitular().filter(cliente::equals).isPresent());
        if (clienteConTarjeta) {
            throw new IllegalArgumentException("Ese cliente ya tiene otra tarjeta asignada.");
        }
        gestionada.asignar(cliente, fecha);
    }

    public void liberar(TarjetaFidelizacion tarjeta) {
        obtener(tarjeta).liberar();
    }

    public Optional<TarjetaFidelizacion> tarjetaDe(ClienteTienda cliente) {
        return tarjetas.stream()
                .filter(t -> t.getTitular().filter(cliente::equals).isPresent())
                .findFirst();
    }

    public List<TarjetaFidelizacion> sinTitular() {
        return tarjetas.stream()
                .filter(t -> !t.estaAsignada())
                .sorted(Comparator.comparing(TarjetaFidelizacion::getNumeroTarjeta))
                .collect(Collectors.toList());
    }

    public List<ClienteTienda> listarClientes() {
        return clientes.stream()
                .sorted(Comparator.comparing(ClienteTienda::getCodigoCliente))
                .collect(Collectors.toList());
    }

    public Set<TarjetaFidelizacion> getTarjetas() { return Collections.unmodifiableSet(tarjetas); }

    private TarjetaFidelizacion obtener(TarjetaFidelizacion tarjeta) {
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");
        return tarjetas.stream()
                .filter(t -> t.equals(tarjeta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Esa tarjeta no está registrada."));
    }
}
