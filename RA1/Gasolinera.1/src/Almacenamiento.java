import java.util.List;
public interface Almacenamiento {

    public List<Cliente> leerCliente();

    public boolean escribirCliente(Cliente cliente);

    public List<Pago> leerPago();

    public boolean escribirPago(Pago pago);

}