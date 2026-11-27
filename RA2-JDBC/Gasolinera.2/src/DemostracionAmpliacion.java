import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Demostración en memoria del resto de la Fase 1.
 *
 * No abre conexiones y no ejecuta SQL: construye objetos relacionados y muestra
 * juntos productos de las dos modalidades, el cálculo de importes, el historial
 * de funciones, los cobros y las devoluciones parciales.
 */
public final class DemostracionAmpliacion {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 25, 10, 30);

    private DemostracionAmpliacion() { }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        GestionEstaciones estaciones = crearEstaciones();
        Estacion centro = estaciones.buscarPorCodigo("EST-001").orElseThrow();
        Estacion norte = estaciones.buscarPorCodigo("EST-002").orElseThrow();
        Estacion puerto = estaciones.buscarPorCodigo("EST-003").orElseThrow();

        GestionPersonal personal = crearPersonal(centro, norte);
        Empleado ana = personal.buscarPorNumero("EMP-001").orElseThrow();
        Empleado bruno = personal.buscarPorNumero("EMP-002").orElseThrow();
        Empleado carla = personal.buscarPorNumero("EMP-003").orElseThrow();
        Empleado dani = personal.buscarPorNumero("EMP-004").orElseThrow();

        GestionFidelizacion fidelizacion = crearFidelizacion();
        ClienteTienda cliente1 = fidelizacion.listarClientes().stream()
                .filter(c -> c.getCodigoCliente().equals("CLI-001")).findFirst().orElseThrow();

        GestionCatalogo catalogo = crearCatalogo();
        Producto agua = buscarProducto(catalogo, "PROD-001");
        Producto almendras = buscarProducto(catalogo, "PROD-002");
        Producto cafe = buscarProducto(catalogo, "PROD-003");
        Producto anacardos = buscarProducto(catalogo, "PROD-004");
        Producto zumo = buscarProducto(catalogo, "PROD-005");

        GestionCampanas campanas = crearCampanas(fidelizacion);
        GestionIncidencias incidencias = crearIncidencias(centro, ana);

        imprimirPersonal(personal, ana, bruno, carla, dani);
        imprimirFidelizacion(fidelizacion);
        imprimirCatalogo(catalogo);
        imprimirCampanas(campanas, fidelizacion);
        imprimirIncidencias(incidencias, dani);

        GestionVentas ventas = crearVentas(centro, norte, puerto, ana, bruno, carla, cliente1,
                agua, almendras, cafe, anacardos, zumo);
        imprimirVentas(ventas, agua);

        GestionDevoluciones devoluciones = crearDevoluciones(ventas, ana);
        imprimirDevoluciones(devoluciones, ventas);

        imprimirRechazos(estaciones, personal, fidelizacion, campanas, catalogo, ventas,
                devoluciones, agua, almendras, bruno, cliente1);
    }

    private static GestionEstaciones crearEstaciones() {
        TipoCombustible diesel = new TipoCombustible("DIESEL", "Gasóleo A");
        EstadoServicio operativo = new EstadoServicio("OPERATIVO", "En servicio");
        GestionEstaciones gestion = new GestionEstaciones();
        gestion.darDeAlta(new AltaEstacion("EST-001", "Centro", "Av. Principal 12", "910111213",
                LocalDate.of(2020, 3, 15), new ConfiguracionFacturacion("CF-001", "A", 101, "Gracias"),
                "SUR-001", 1, diesel, operativo));
        gestion.darDeAlta(new AltaEstacion("EST-002", "Norte", "Calle Norte 8", "910222222",
                LocalDate.of(2021, 6, 1), new ConfiguracionFacturacion("CF-002", "N", 204, "Pie norte"),
                "SUR-002", 1, diesel, operativo));
        gestion.darDeAlta(new AltaEstacion("EST-003", "Puerto", "Puerto 3", "910333333",
                LocalDate.of(2022, 1, 20), new ConfiguracionFacturacion("CF-003", "P", 1, "Puerto"),
                "SUR-003", 1, diesel, operativo));
        return gestion;
    }

    private static GestionPersonal crearPersonal(Estacion centro, Estacion norte) {
        GestionPersonal gestion = new GestionPersonal();
        Empleado ana = gestion.darDeAlta(new Empleado("EMP-001", "11111111A", "Ana García",
                "910000001", null, LocalDate.of(2019, 2, 1), centro));
        Empleado bruno = gestion.darDeAlta(new Empleado("EMP-002", "22222222B", "Bruno López",
                null, "bruno@example.com", LocalDate.of(2023, 4, 10), centro));
        Empleado carla = gestion.darDeAlta(new Empleado("EMP-003", "33333333C", "Carla Ruiz",
                "910000003", "carla@example.com", LocalDate.of(2024, 1, 8), centro));
        gestion.darDeAlta(new Empleado("EMP-004", "44444444D", "Dani Soto",
                "910000004", null, LocalDate.of(2026, 10, 1), norte));

        gestion.cambiarSupervisor(bruno, ana);
        gestion.cambiarSupervisor(carla, ana);

        gestion.asignarFuncion(ana, new Cajero(200.0), LocalDate.of(2020, 1, 1));
        gestion.asignarFuncion(ana, new ResponsableTienda(1000.0), LocalDate.of(2022, 5, 1));
        gestion.asignarFuncion(bruno, new Expendedor("ACR-7788", LocalDate.of(2027, 12, 31)),
                LocalDate.of(2023, 4, 10));
        gestion.asignarFuncion(carla, new Cajero(100.0), LocalDate.of(2024, 1, 8));
        gestion.finalizarFuncion(carla, "CAJERO", LocalDate.of(2024, 9, 30));
        gestion.asignarFuncion(carla, new Cajero(300.0), LocalDate.of(2025, 2, 1));
        return gestion;
    }

    private static GestionFidelizacion crearFidelizacion() {
        GestionFidelizacion gestion = new GestionFidelizacion();
        ClienteTienda c1 = gestion.registrarCliente(new ClienteTienda("CLI-001", "Lucía", "910111111", null));
        ClienteTienda c2 = gestion.registrarCliente(new ClienteTienda("CLI-002", "Marcos", null, "marcos@example.com"));
        ClienteTienda c3 = gestion.registrarCliente(new ClienteTienda("CLI-003", "Nuria", "910333333", "nuria@example.com"));
        gestion.registrarCliente(new ClienteTienda("CLI-004", "Óscar", "910444444", null));

        TarjetaFidelizacion t1 = gestion.registrarTarjeta(new TarjetaFidelizacion("TAR-001", LocalDate.of(2024, 1, 5)));
        gestion.registrarTarjeta(new TarjetaFidelizacion("TAR-002", LocalDate.of(2025, 3, 1)));
        TarjetaFidelizacion t3 = gestion.registrarTarjeta(new TarjetaFidelizacion("TAR-003", LocalDate.of(2025, 6, 15)));
        gestion.asignar(t1, c1, LocalDate.of(2024, 1, 20));
        gestion.asignar(t3, c3, LocalDate.of(2025, 7, 1));
        return gestion;
    }

    private static GestionCatalogo crearCatalogo() {
        Producto agua = new Producto("PROD-001", "Agua 1 L", 1.20,
                EstadoComercializacion.ACTIVO, new Envasado("Botella 1 L"));
        Producto almendras = new Producto("PROD-002", "Almendras a granel", 12.00,
                EstadoComercializacion.ACTIVO, new Granel(0.05));
        Producto cafe = new Producto("PROD-003", "Café en grano 1 kg", 8.50,
                EstadoComercializacion.ACTIVO, new Envasado("Paquete 1 kg"));
        Producto anacardos = new Producto("PROD-004", "Anacardos a granel", 18.00,
                EstadoComercializacion.ACTIVO, new Granel(0.1));
        Producto zumo = new Producto("PROD-005", "Zumo retirado", 2.00,
                EstadoComercializacion.RETIRADO, new Envasado("Brik 1 L"));

        Proveedor p1 = new Proveedor("PROV-001", "B12345678", "Distribuciones Norte", "910100001", null);
        Proveedor p2 = new Proveedor("PROV-002", "B87654321", "Alimentos Sur", null, "ventas@alsur.example");
        Proveedor p3 = new Proveedor("PROV-003", "B11223344", "Bebidas Este", "910100003", null);
        Proveedor p4 = new Proveedor("PROV-004", "B44332211", "Frutos Secos SL", "910100004", null);
        Proveedor p5 = new Proveedor("PROV-005", "B55667788", "Cafés Oeste", "910100005", null);

        List<Producto> productos = List.of(agua, almendras, cafe, anacardos, zumo);
        List<Proveedor> proveedores = List.of(p1, p2, p3, p4, p5);
        List<AcuerdoSuministro> acuerdos = List.of(
                new AcuerdoSuministro(p1, agua, "AG-1L", 0.70, 2),
                new AcuerdoSuministro(p1, almendras, "AL-GR", 9.00, 4),
                new AcuerdoSuministro(p2, agua, "H2O-N", 0.75, 3),
                new AcuerdoSuministro(p3, zumo, "ZU-1L", 1.20, 5),
                new AcuerdoSuministro(p4, anacardos, "AN-GR", 14.00, 6),
                new AcuerdoSuministro(p5, cafe, "CA-1K", 6.10, 7));
        return new GestionCatalogo(productos, proveedores, acuerdos);
    }

    private static GestionCampanas crearCampanas(GestionFidelizacion fidelizacion) {
        GestionCampanas gestion = new GestionCampanas(List.of(
                new Campana("CAM-001", "Verano", "Descuentos de verano",
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31)),
                new Campana("CAM-002", "Invierno", "Sin inscritos todavía",
                        LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 15)),
                new Campana("CAM-003", "Clientes frecuentes", "Puntos extra",
                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31))));
        List<ClienteTienda> clientes = fidelizacion.listarClientes();
        ClienteTienda c1 = clientes.get(0);
        ClienteTienda c2 = clientes.get(1);
        ClienteTienda c3 = clientes.get(2);
        gestion.inscribir(gestion.getCampanas().stream()
                .filter(c -> c.getCodigo().equals("CAM-001")).findFirst().orElseThrow(),
                c1, LocalDate.of(2026, 6, 3));
        gestion.inscribir(gestion.getCampanas().stream()
                .filter(c -> c.getCodigo().equals("CAM-001")).findFirst().orElseThrow(),
                c3, LocalDate.of(2026, 6, 5));
        gestion.inscribir(gestion.getCampanas().stream()
                .filter(c -> c.getCodigo().equals("CAM-003")).findFirst().orElseThrow(),
                c1, LocalDate.of(2026, 9, 2));
        gestion.inscribir(gestion.getCampanas().stream()
                .filter(c -> c.getCodigo().equals("CAM-003")).findFirst().orElseThrow(),
                c2, LocalDate.of(2026, 9, 4));
        gestion.inscribir(gestion.getCampanas().stream()
                .filter(c -> c.getCodigo().equals("CAM-003")).findFirst().orElseThrow(),
                c3, LocalDate.of(2026, 9, 6));
        return gestion;
    }

    private static GestionIncidencias crearIncidencias(Estacion centro, Empleado ana) {
        GestionIncidencias gestion = new GestionIncidencias();
        gestion.registrar(new Incidencia("INC-001", "Surtidor averiado", AHORA.minusHours(3),
                centro, EstadoIncidencia.ABIERTA, null));
        gestion.registrar(new Incidencia("INC-002", "Caja sin cambio", AHORA.minusHours(2),
                centro, EstadoIncidencia.EN_CURSO, ana));
        gestion.registrar(new Incidencia("INC-003", "Tienda sin etiquetas", AHORA.minusHours(1),
                centro, EstadoIncidencia.EN_CURSO, ana));
        return gestion;
    }

    private static GestionVentas crearVentas(Estacion centro, Estacion norte, Estacion puerto,
                                             Empleado ana, Empleado bruno, Empleado carla,
                                             ClienteTienda cliente1, Producto agua, Producto almendras,
                                             Producto cafe, Producto anacardos, Producto zumo) {
        GestionVentas gestion = new GestionVentas();

        // Unidades completas + granel con precisión 0,05: 1,50 kg es válido.
        Venta v1 = gestion.registrar("A000101", AHORA, centro, bruno, null, List.of(
                new LineaVenta(1, agua, 3),
                new LineaVenta(2, almendras, 1.50)));
        gestion.anadirCobro(v1, 10.00, AHORA.plusMinutes(2), MedioPago.EFECTIVO);
        gestion.anadirCobro(v1, v1.getImporteTotal() - 10.00, AHORA.plusMinutes(3), MedioPago.TARJETA);
        gestion.confirmarPagada(v1);

        Venta v2 = gestion.registrar("A000102", AHORA.plusMinutes(10), norte, ana, cliente1, List.of(
                new LineaVenta(1, cafe, 2),
                new LineaVenta(2, anacardos, 0.30),
                new LineaVenta(3, agua, 1)));
        gestion.anadirCobro(v2, v2.getImporteTotal(), AHORA.plusMinutes(12), MedioPago.TARJETA);
        gestion.confirmarPagada(v2);

        // Producto retirado que sigue siendo identificable en una operación antigua.
        Venta v3 = gestion.registrar("N000204", AHORA.plusMinutes(20), puerto, carla, null, List.of(
                new LineaVenta(1, zumo, 2),
                new LineaVenta(2, agua, 4)));
        gestion.anadirCobro(v3, v3.getImporteTotal(), AHORA.plusMinutes(21), MedioPago.EFECTIVO);
        gestion.confirmarPagada(v3);
        return gestion;
    }

    private static GestionDevoluciones crearDevoluciones(GestionVentas ventas, Empleado cajero) {
        GestionDevoluciones gestion = new GestionDevoluciones();
        Venta v1 = ventas.listar().stream()
                .filter(v -> v.getReferenciaTicket().equals("A000101")).findFirst().orElseThrow();
        LineaVenta granel = v1.getLineas().get(1);
        gestion.registrar(granel, 0.50, "Producto dañado", AHORA.plusHours(1), cajero, false);
        gestion.registrar(granel, 0.25, "Error de pesada", AHORA.plusHours(2), cajero, true);
        return gestion;
    }

    private static void imprimirPersonal(GestionPersonal personal, Empleado ana, Empleado bruno,
                                         Empleado carla, Empleado dani) {
        System.out.println("=== PERSONAL: SUPERVISIÓN Y FUNCIONES ===");
        System.out.println("Sin supervisor: " + personal.sinSupervisor().stream()
                .map(Empleado::getNumeroInterno).toList());
        System.out.println("Ana supervisa a: " + personal.supervisoresDe(ana).stream()
                .map(Empleado::getNumeroInterno).toList());
        System.out.println("Bruno hoy: " + personal.funcionesVigentes(bruno, AHORA.toLocalDate()));
        System.out.println("Dani hoy (sin funciones): " + personal.funcionesVigentes(dani, AHORA.toLocalDate()));
        carla.getHistorialFunciones().forEach(a ->
                System.out.println("Carla: " + a));
    }

    private static void imprimirFidelizacion(GestionFidelizacion fidelizacion) {
        System.out.println();
        System.out.println("=== CLIENTES Y TARJETAS ===");
        System.out.println("Tarjetas sin titular: " + fidelizacion.sinTitular().stream()
                .map(TarjetaFidelizacion::getNumeroTarjeta).toList());
        System.out.println("Cliente sin tarjeta: CLI-002 -> "
                + fidelizacion.tarjetaDe(new ClienteTienda("CLI-002", "x", "x", null)).isEmpty());
    }

    private static void imprimirCatalogo(GestionCatalogo catalogo) {
        System.out.println();
        System.out.println("=== CATÁLOGO Y CÁLCULO POR MODALIDAD ===");
        Producto agua = buscarProducto(catalogo, "PROD-001");
        Producto almendras = buscarProducto(catalogo, "PROD-002");
        System.out.println("Envasado: 3 x " + RedondeoMonetario.formatear(agua.getPrecioVenta())
                + " € = " + RedondeoMonetario.formatear(agua.calcularImporte(3)) + " €");
        System.out.println("Granel: 1,50 kg x " + RedondeoMonetario.formatear(almendras.getPrecioVenta())
                + " € = " + RedondeoMonetario.formatear(almendras.calcularImporte(1.5)) + " €");
        System.out.println("Acuerdos: " + catalogo.getAcuerdos().size()
                + "; proveedor PROV-001 suministra "
                + catalogo.acuerdosDe(new Proveedor("PROV-001", "x", "x", "x", null)).size()
                + " productos; PROD-001 tiene "
                + catalogo.acuerdosDe(agua).size() + " proveedores.");
    }

    private static void imprimirCampanas(GestionCampanas campanas, GestionFidelizacion fidelizacion) {
        System.out.println();
        System.out.println("=== CAMPAÑAS ===");
        System.out.println("Sin inscritos: " + campanas.sinInscritos().stream()
                .map(Campana::getCodigo).toList());
        System.out.println("Cliente sin campañas: CLI-004 -> "
                + campanas.campanasDe(new ClienteTienda("CLI-004", "x", "x", null)).isEmpty());
    }

    private static void imprimirIncidencias(GestionIncidencias incidencias, Empleado dani) {
        System.out.println();
        System.out.println("=== INCIDENCIAS ===");
        System.out.println("Sin responsable: " + incidencias.sinResponsable().stream()
                .map(Incidencia::getReferencia).toList());
        System.out.println("Dani no tiene incidencias: " + incidencias.de(dani).isEmpty());
        System.out.println("Incidencias de Ana: " + incidencias.de(
                incidencias.getIncidencias().stream()
                        .filter(i -> i.getReferencia().equals("INC-002"))
                        .findFirst().orElseThrow().getResponsable()).size());
    }

    private static void imprimirVentas(GestionVentas ventas, Producto agua) {
        System.out.println();
        System.out.println("=== VENTAS, PAGOS Y PRECIO HISTÓRICO ===");
        ventas.listar().forEach(v -> {
            System.out.println(v + " | cliente " + v.getCliente().map(ClienteTienda::getCodigoCliente).orElse("anónimo")
                    + " | cobros " + v.getCobros());
            v.getLineas().forEach(l -> System.out.println("   " + l));
        });
        double precioAntes = ventas.listar().stream()
                .filter(v -> v.getReferenciaTicket().equals("A000101")).findFirst().orElseThrow()
                .getLineas().get(0).getPrecioUnitario();
        agua.cambiarPrecio(1.50);
        System.out.println("PROD-001 pasa a 1,50 € en catálogo; línea A000101 conserva "
                + RedondeoMonetario.formatear(precioAntes) + " €.");
    }

    private static void imprimirDevoluciones(GestionDevoluciones devoluciones, GestionVentas ventas) {
        System.out.println();
        System.out.println("=== DEVOLUCIONES PARCIALES SUCESIVAS ===");
        devoluciones.todas().forEach(d -> System.out.println("   " + d));
        Venta v1 = ventas.listar().stream()
                .filter(v -> v.getReferenciaTicket().equals("A000101")).findFirst().orElseThrow();
        LineaVenta granel = v1.getLineas().get(1);
        System.out.println("Línea original intacta: " + RedondeoMonetario.formatear(granel.getCantidad())
                + " kg por " + RedondeoMonetario.formatear(granel.getPrecioUnitario()) + " €/kg.");
    }

    private static void imprimirRechazos(GestionEstaciones estaciones, GestionPersonal personal,
                                         GestionFidelizacion fidelizacion, GestionCampanas campanas,
                                         GestionCatalogo catalogo, GestionVentas ventas,
                                         GestionDevoluciones devoluciones, Producto agua,
                                         Producto almendras, Empleado bruno, ClienteTienda cliente1) {
        System.out.println();
        System.out.println("=== REGLAS RECHAZADAS (esperado) ===");
        esperarRechazo("carrito vacío", () -> new Venta("TEMP", AHORA, estaciones.listar().get(0),
                bruno, null, List.of()));
        esperarRechazo("cantidad 1,5 en producto envasado", () -> agua.calcularImporte(1.5));
        esperarRechazo("cantidad fuera de la precisión 0,05", () -> almendras.calcularImporte(0.52));
        esperarRechazo("periodo solapado de la misma función", () ->
                personal.asignarFuncion(bruno, new Expendedor("ACR-9999", LocalDate.of(2028, 1, 1)),
                        LocalDate.of(2025, 1, 1)));
        esperarRechazo("ciclo de supervisión", () -> personal.cambiarSupervisor(
                personal.buscarPorNumero("EMP-001").orElseThrow(), bruno));
        esperarRechazo("segunda tarjeta para el mismo cliente", () -> {
            TarjetaFidelizacion temporal = fidelizacion.registrarTarjeta(
                    new TarjetaFidelizacion("TAR-TEMP", LocalDate.of(2026, 1, 1)));
            fidelizacion.asignar(temporal, cliente1, LocalDate.of(2026, 2, 1));
        });
        esperarRechazo("inscripción repetida", () -> campanas.inscribir(
                campanas.getCampanas().stream().filter(c -> c.getCodigo().equals("CAM-001"))
                        .findFirst().orElseThrow(), cliente1, LocalDate.of(2026, 7, 1)));
        esperarRechazo("acuerdo duplicado", () -> catalogo.registrarAcuerdo(new AcuerdoSuministro(
                catalogo.getProveedores().stream().findFirst().orElseThrow(), agua, "OTRA", 0.80, 1)));
        esperarRechazo("ticket duplicado", () -> ventas.registrar("A000101", AHORA,
                estaciones.listar().get(0), bruno, null,
                List.of(new LineaVenta(1, agua, 1))));
        Venta pendiente = new Venta("TEMP-PEND", AHORA, estaciones.listar().get(0), bruno, null,
                List.of(new LineaVenta(1, agua, 1)));
        esperarRechazo("confirmar cobro sin importe suficiente", pendiente::confirmarPagada);
        Venta pagada = ventas.listar().stream()
                .filter(v -> v.getReferenciaTicket().equals("A000101")).findFirst().orElseThrow();
        LineaVenta granel = pagada.getLineas().get(1);
        esperarRechazo("devolución que supera lo comprado", () ->
                devoluciones.registrar(granel, 1.00, "Exceso", AHORA.plusHours(3), bruno, false));
    }

    private static Producto buscarProducto(GestionCatalogo catalogo, String referencia) {
        return catalogo.getProductos().stream()
                .filter(p -> p.getReferenciaComercial().equals(referencia))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta el producto " + referencia + "."));
    }

    private static void esperarRechazo(String descripcion, Runnable accion) {
        try {
            accion.run();
            System.out.println("REVISAR: se aceptó " + descripcion);
        } catch (RuntimeException e) {
            System.out.println("Rechazado como se esperaba -> " + descripcion + " | " + e.getMessage());
        }
    }
}
