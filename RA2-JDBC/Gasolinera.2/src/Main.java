//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    public static void main(String[] args) {
        Almacenamiento almacenamiento;

        try {
            almacenamiento = new GestionJson();
        } catch (IOException e) {
            System.out.println("No se crea el directorio");
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);

        try {
            MigracionCsvaJson.migrarSiProcede(scanner);
        } catch (IOException e) {
            System.out.println("Error en la migración: " + e.getMessage());
        }

        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println("=== GESTIÓN DE GASOLINERA ===");
            System.out.println("1. Dar de alta un cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar clientes");
            System.out.println("4. Procesar un pago de repostaje");
            System.out.println("5. Consultar pagos");
            System.out.println("6. Demostración de estaciones (Fase 1)");
            System.out.println("7. Demostración de la ampliación (Fase 1)");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    darDeAltaCliente(scanner, almacenamiento);
                    break;
                case "2":
                    listarClientes(almacenamiento);
                    break;
                case "3":
                    buscarClientes(scanner, almacenamiento);
                    break;
                case "4":
                    procesarPago(scanner, almacenamiento);
                    break;
                case "5":
                    consultarPagos(almacenamiento);
                    break;
                case "6":
                    DemostracionEstaciones.ejecutar();
                    break;
                case "7":
                    DemostracionAmpliacion.ejecutar();
                    break;
                case "0":
                    salir = true;
                    System.out.println("has cerrado el menu");
                    break;
                default:
                    System.out.println("no es ninguna de als opciones.");
            }
        }
    }
    static String leerTextoObligatorio(Scanner scanner, String etiqueta) {
        String texto;
        do {
            System.out.print(etiqueta);
            texto = scanner.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("Este campo es obligatorio.");
            }
        } while (texto.isEmpty());
        return texto;
    }

    static void darDeAltaCliente(Scanner scanner, Almacenamiento almacenamiento){
    String nombre = leerTextoObligatorio(scanner, "nombre: ");
    String telefono = leerTextoObligatorio(scanner, "telefono: ");
    String matricula = leerTextoObligatorio(scanner, "matricula: ").toUpperCase();
        List<Cliente> clientes = almacenamiento.leerCliente();
        boolean matriculaDuplicada = false;
        for (Cliente c : clientes) {
            if (c.getMatricula().equalsIgnoreCase(matricula)) {
                matriculaDuplicada = true;
                break;
            }
        }
        if (matriculaDuplicada) {
            System.out.println("Esa matrícula ya esta");
            return;
        }

        int siguienteId = 1;
        for (Cliente c : clientes) {
            if (c.getId() >= siguienteId) {
                siguienteId = c.getId() + 1;
            }
        }

        Cliente nuevoCliente = new Cliente(siguienteId, nombre, telefono, matricula);
        almacenamiento.escribirCliente(nuevoCliente);
        System.out.println("Cliente registrado con ID " + siguienteId + ".");
    }
        static void listarClientes(Almacenamiento almacenamiento) {
            List<Cliente> clientes = almacenamiento.leerCliente();

            if (clientes.isEmpty()) {
                System.out.println("No hay clientes registrados.");
                return;
            }

            clientes.sort(Comparator.comparing((Cliente c) -> c.getNombre().toLowerCase())
                    .thenComparingInt(Cliente::getId));

            System.out.println("ID\tNOMBRE\tTELÉFONO\tMATRÍCULA");
            for (Cliente c : clientes) {
                System.out.println(c.getId() + "\t" + c.getNombre() + "\t" + c.getTelefono() + "\t" + c.getMatricula());
            }
        }
        static void buscarClientes(Scanner scanner, Almacenamiento almacenamiento) {
            String texto = leerTextoObligatorio(scanner, "Texto que buscar: ").toLowerCase();

            List<Cliente> clientes = almacenamiento.leerCliente();

            List<Cliente> encontrados = new ArrayList<>();
            for (Cliente c : clientes) {
                if (c.getNombre().toLowerCase().contains(texto)
                        || c.getTelefono().toLowerCase().contains(texto)
                        || c.getMatricula().toLowerCase().contains(texto)) {
                    encontrados.add(c);
                }
            }

            if (encontrados.isEmpty()) {
                System.out.println("No se han encontrado clientes.");
                return;
            }

            System.out.println("ID\tNOMBRE\tTELÉFONO\tMATRÍCULA");
            for (Cliente c : encontrados) {
                System.out.println(c.getId() + "\t" + c.getNombre() + "\t" + c.getTelefono() + "\t" + c.getMatricula());
            }
        }
    static void procesarPago(Scanner scanner, Almacenamiento almacenamiento) {
        List<Cliente> clientes = almacenamiento.leerCliente();

        if (clientes.isEmpty()) {
            System.out.println("Primero debes dar de alta un cliente.");
            return;
        }

        System.out.println("ID\tNOMBRE\tTELÉFONO\tMATRÍCULA");
        for (Cliente c : clientes) {
            System.out.println(c.getId() + "\t" + c.getNombre() + "\t" + c.getTelefono() + "\t" + c.getMatricula());
        }

        int idCliente = leerEnteroPositivo(scanner, "ID del cliente: ");

        Cliente clienteEncontrado = null;
        for (Cliente c : clientes) {
            if (c.getId() == idCliente) clienteEncontrado = c;
        }

        if (clienteEncontrado == null) {
            System.out.println("No existe un cliente con ese identificador. No se ha registrado el pago.");
            return;
        }

        LocalDate fecha = leerFecha(scanner, "Fecha (dd/MM/aaaa; vacío para hoy): ");
        double importe = leerCantidadPositiva(scanner, "Importe (€): ");
        double litros = leerCantidadPositiva(scanner, "Litros: ");
        String combustible = leerTextoObligatorio(scanner, "Combustible: ");

        List<Pago> pagos = almacenamiento.leerPago();
        int siguienteId = 1;
        for (Pago p : pagos) {
            if (p.getId() >= siguienteId) siguienteId = p.getId() + 1;
        }

        almacenamiento.escribirPago(new Pago(siguienteId, idCliente, fecha, importe, litros, combustible));

        System.out.println("Pago " + siguienteId + " registrado para " + clienteEncontrado.getNombre() + ": " + importe + " €.");
    }

    static int leerEnteroPositivo(Scanner scanner, String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor > 0) return valor;
            } catch (NumberFormatException ignorado) { }
            System.out.println("Introduce un número entero positivo.");
        }
    }

    static LocalDate leerFecha(Scanner scanner, String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String texto = scanner.nextLine().trim();
            if (texto.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(texto, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("La fecha no es válida.");
            }
        }
    }

    static double leerCantidadPositiva(Scanner scanner, String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            try {
                double valor = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
                if (valor > 0) return valor;
            } catch (NumberFormatException ignorado) { }
            System.out.println("Introduce una cantidad mayor que cero.");
        }
    }

    static void consultarPagos(Almacenamiento almacenamiento) {
        List<Pago> pagos = almacenamiento.leerPago();

        if (pagos.isEmpty()) {
            System.out.println("No hay pagos registrados.");
            return;
        }
        List<Cliente> clientes = almacenamiento.leerCliente();

        System.out.println("ID\tCLIENTE\tFECHA\tIMPORTE\tLITROS\tCOMBUSTIBLE");
        for (Pago p : pagos) {
            String nombreCliente = "Cliente desconocido";
            for (Cliente c : clientes) {
                if (c.getId() == p.getIdCliente()) nombreCliente = c.getNombre();
            }
            System.out.println(p.getId() + "\t" + nombreCliente + "\t" + p.getFecha().format(FORMATO_FECHA)
                    + "\t" + p.getImporte() + " €\t" + p.getLitros() + "\t" + p.getCombustible());
        }
    }
}

