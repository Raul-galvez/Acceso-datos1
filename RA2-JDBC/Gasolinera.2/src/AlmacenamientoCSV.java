import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AlmacenamientoCSV implements Almacenamiento {

    Path directorio;
    Path archivoClientes;
    Path archivoPagos;


    public AlmacenamientoCSV() throws IOException {
        directorio = Path.of("datos");

        Files.createDirectories(directorio);
        archivoClientes = directorio.resolve("clientes.csv");
        archivoPagos = directorio.resolve("pagos.csv");

        if (Files.notExists(archivoClientes)) { Files.createFile(archivoClientes);}
        if (Files.notExists(archivoPagos)) { Files.createFile(archivoPagos);}
    }

    @Override
    public List<Cliente> leerCliente() {
        List<Cliente> clientes = new ArrayList<>();
        try (BufferedReader bf = Files.newBufferedReader(archivoClientes)) {
            String linea;
            while ((linea = bf.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] campos = linea.split(";");
                int id = Integer.parseInt(campos[0]);
                String nombre = campos[1];
                String telefono = campos[2];
                String matricula = campos[3];
                clientes.add(new Cliente(id, nombre, telefono, matricula));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    @Override
    public boolean escribirCliente(Cliente cliente) {
        String linea = cliente.getId() + "; " + cliente.getNombre() + ";" + cliente.getTelefono() + ";" + cliente.getMatricula();
        try (BufferedWriter bw = Files.newBufferedWriter(archivoClientes, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(linea);
            bw.newLine();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Pago> leerPago() {
        List<Pago> pagos = new ArrayList<>();
        try (BufferedReader bf = Files.newBufferedReader(archivoPagos)){
            String linea;
            while ((linea = bf.readLine()) != null){
                if (linea.isBlank()) continue;
                String[] campos = linea.split(";");
                int id = Integer.parseInt(campos[0]);
                int idCliente = Integer.parseInt(campos[1]);
                LocalDate fecha = LocalDate.parse(campos[2], DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                double importe = Double.parseDouble(campos[3]);
                double litros = Double.parseDouble(campos[4]);
                String combustible = campos[5];
                pagos.add(new Pago(id, idCliente, fecha, importe, litros, combustible));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pagos;
    }

    @Override
    public boolean escribirPago(Pago pago) {
        String linea = pago.getId() + ";" + pago.getIdCliente() + ";" + pago.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/uuuu")) + ";"
                + pago.getImporte() + ";" + pago.getLitros() + ";" + pago.getCombustible();
        try (BufferedWriter bw = Files.newBufferedWriter(archivoPagos, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(linea);
            bw.newLine();
            return true;
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}