import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class GestionJson implements Almacenamiento {

    private final Path archivoClientes;
    private final Path archivoPagos;

    public GestionJson() throws IOException {
        Path directorio = Path.of("datos");
        Files.createDirectories(directorio);
        archivoClientes = directorio.resolve("clientes.json");
        archivoPagos = directorio.resolve("pagos.json");

        if (Files.notExists(archivoClientes)) Files.createFile(archivoClientes);
        if (Files.notExists(archivoPagos)) Files.createFile(archivoPagos);
    }

    public static String clienteAJSON(Cliente c) {
        return "{\"id\": " + c.getId()
                + ", \"nombre\": \"" + c.getNombre() + "\""
                + ", \"telefono\": \"" + c.getTelefono() + "\""
                + ", \"matricula\": \"" + c.getMatricula() + "\"}";
    }

    public static String pagoAJSON(Pago p) {
        return "{\"id\": " + p.getId()
                + ", \"idCliente\": " + p.getIdCliente()
                + ", \"fecha\": \"" + p.getFecha() + "\""
                + ", \"importe\": " + p.getImporte()
                + ", \"litros\": " + p.getLitros()
                + ", \"combustible\": \"" + p.getCombustible() + "\"}";
    }

    public static Cliente jsonACliente(String json) {
        String[] campos = json.replace("{", "").replace("}", "").replace("\"", "").split(", ");
        int id = Integer.parseInt(campos[0].split(": ")[1]);
        String nombre = campos[1].split(": ")[1];
        String telefono = campos[2].split(": ")[1];
        String matricula = campos[3].split(": ")[1];
        return new Cliente(id, nombre, telefono, matricula);
    }

    public static Pago jsonAPago(String json) {
        String[] campos = json.replace("{", "").replace("}", "").replace("\"", "").split(", ");
        int id = Integer.parseInt(campos[0].split(": ")[1]);
        int idCliente = Integer.parseInt(campos[1].split(": ")[1]);
        LocalDate fecha = LocalDate.parse(campos[2].split(": ")[1]);
        double importe = Double.parseDouble(campos[3].split(": ")[1]);
        double litros = Double.parseDouble(campos[4].split(": ")[1]);
        String combustible = campos[5].split(": ")[1];
        return new Pago(id, idCliente, fecha, importe, litros, combustible);
    }

    // ---------- Almacenamiento ----------

    @Override
    public List<Cliente> leerCliente() {
        List<Cliente> clientes = new ArrayList<>();
        try (BufferedReader bf = Files.newBufferedReader(archivoClientes)) {
            String linea;
            while ((linea = bf.readLine()) != null) {
                if (linea.isBlank()) continue;
                try {
                    clientes.add(jsonACliente(linea));
                } catch (RuntimeException e) {
                    System.out.println("Línea de cliente ignorada: " + linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo clientes: " + e.getMessage());
        }
        return clientes;
    }

    @Override
    public boolean escribirCliente(Cliente cliente) {
        try (BufferedWriter bw = Files.newBufferedWriter(archivoClientes,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(clienteAJSON(cliente));
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error escribiendo cliente: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Pago> leerPago() {
        List<Pago> pagos = new ArrayList<>();
        try (BufferedReader bf = Files.newBufferedReader(archivoPagos)) {
            String linea;
            while ((linea = bf.readLine()) != null) {
                if (linea.isBlank()) continue;
                try {
                    pagos.add(jsonAPago(linea));
                } catch (RuntimeException e) {
                    System.out.println("Línea de pago ignorada: " + linea);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo pagos: " + e.getMessage());
        }
        return pagos;
    }

    @Override
    public boolean escribirPago(Pago pago) {
        try (BufferedWriter bw = Files.newBufferedWriter(archivoPagos,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(pagoAJSON(pago));
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error escribiendo pago: " + e.getMessage());
            return false;
        }
    }
}