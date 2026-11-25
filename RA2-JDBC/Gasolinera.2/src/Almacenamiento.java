import java.util.List;

public interface Almacenamiento {
    List<Cliente> leerCliente();
    boolean escribirCliente(Cliente cliente);
    List<Pago> leerPago();
    boolean escribirPago(Pago pago);
}