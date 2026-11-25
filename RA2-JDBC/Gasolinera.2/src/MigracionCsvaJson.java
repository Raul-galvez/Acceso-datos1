import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class MigracionCsvaJson {
    public static void migrarSiProcede(Scanner scanner) throws IOException {
        Almacenamiento csv = new AlmacenamientoCSV();
        Almacenamiento json = new GestionJson();

        List<Cliente> clientesCsv = csv.leerCliente();
        List<Pago> pagosCsv = csv.leerPago();

        boolean migrarClientes = json.leerCliente().isEmpty() && !clientesCsv.isEmpty();
        boolean migrarPagos = json.leerPago().isEmpty() && !pagosCsv.isEmpty();

        if (!migrarClientes && !migrarPagos) {
            return;
        }

        System.out.print("¿Quieres migrar de CSV a JSON? (s/n): ");
        String respuesta = scanner.nextLine();
        if (!respuesta.equalsIgnoreCase("s")) {
            return;
        }

        if (migrarClientes) {
            for (Cliente c : clientesCsv) {
                json.escribirCliente(c);
            }
        }
        if (migrarPagos) {
            for (Pago p : pagosCsv) {
                json.escribirPago(p);
            }
        }
        System.out.println("Migración completada.");
    }
}
