import java.time.LocalDate;

/**
 * Demostración en memoria del apartado "Las estaciones y su puesta en servicio".
 * No guarda ni lee MySQL: sólo construye objetos relacionados y muestra que las
 * reglas de este apartado se respetan.
 */
public final class DemostracionEstaciones {

    private DemostracionEstaciones() { }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        TipoCombustible diesel = new TipoCombustible("DIESEL", "Gasóleo A");
        TipoCombustible gasolina95 = new TipoCombustible("GASOLINA_95", "Gasolina 95");
        TipoCombustible gasolina98 = new TipoCombustible("GASOLINA_98", "Gasolina 98");

        EstadoServicio operativo = new EstadoServicio("OPERATIVO", "En servicio");
        EstadoServicio averiado = new EstadoServicio("AVERIADO", "Averiado temporalmente");

        GestionEstaciones gestion = new GestionEstaciones();

        Estacion centro = gestion.darDeAlta(new AltaEstacion(
                "EST-001", "Gasolinera Centro", "Av. Principal 12", "910111213",
                LocalDate.of(2020, 3, 15),
                new ConfiguracionFacturacion("CF-001", "A", 101, "Gracias por visitarnos"),
                "SUR-001", 1, diesel, operativo));
        gestion.añadirSurtidor(centro, new Surtidor("SUR-002", 2, gasolina95, operativo, centro));
        gestion.añadirSurtidor(centro, new Surtidor("SUR-003", 3, gasolina98, averiado, centro));

        Estacion norte = gestion.darDeAlta(new AltaEstacion(
                "EST-002", "Gasolinera Norte", "Calle Norte 8", "910222222",
                LocalDate.of(2021, 6, 1),
                new ConfiguracionFacturacion("CF-002", "N", 204, "Visite también nuestro taller"),
                "SUR-004", 1, diesel, operativo));

        gestion.darDeAlta(new AltaEstacion(
                "EST-003", "Gasolinera Puerto", "Puerto 3", "910333333",
                LocalDate.of(2022, 1, 20),
                new ConfiguracionFacturacion("CF-003", "P", 1, "Puerto"),
                "SUR-005", 1, gasolina95, operativo));

        System.out.println("=== ESTACIONES Y SU FICHA (alta conjunta) ===");
        gestion.listar().forEach(e -> System.out.printf("%s | %s | apertura %s | ficha %s | %d surtidores%n",
                e.getCodigoInterno(), e.getNombreComercial(), e.getFechaApertura(),
                e.getConfiguracion().getCodigoConfiguracion(), e.getSurtidores().size()));

        System.out.println();
        System.out.println("=== SURTIDORES (el nº visible se repite en estaciones distintas) ===");
        gestion.todosLosSurtidores().stream()
                .sorted(java.util.Comparator.comparing(Surtidor::getCodigoEquipo))
                .forEach(s -> System.out.printf("%s | estación %s | nº %d | %s | %s%n",
                        s.getCodigoEquipo(), s.getEstacion().getCodigoInterno(), s.getNumeroVisible(),
                        s.getTipoCombustible(), s.getEstadoServicio()));

        System.out.println();
        System.out.println("=== COMPORTAMIENTO CONJUNTO ===");
        System.out.println("El equipo SUR-003 está averiado pero sigue en su estación: "
                + centro.buscarSurtidor("SUR-003")
                        .map(s -> s.perteneceA(centro) && s.getEstadoServicio().equals(averiado))
                        .orElse(false));
        System.out.println("Próximos tickets de EST-001: "
                + centro.getConfiguracion().reservarNumeroTicket() + " y "
                + centro.getConfiguracion().reservarNumeroTicket());

        System.out.println();
        System.out.println("=== REGLAS RECHAZADAS (esperado) ===");
        esperarRechazo("código de estación duplicado", () -> gestion.darDeAlta(new AltaEstacion(
                "EST-001", "Otra", "Otra dirección", "900000000", LocalDate.now(),
                new ConfiguracionFacturacion("CF-TEMP", "Z", 1, "pie"),
                "SUR-TEMP", 1, diesel, operativo)));

        esperarRechazo("número visible repetido en la misma estación", () ->
                gestion.añadirSurtidor(centro, new Surtidor("SUR-TEMP", 1, diesel, operativo, centro)));

        esperarRechazo("código de equipo repetido en la empresa", () ->
                gestion.añadirSurtidor(norte, new Surtidor("SUR-001", 9, diesel, operativo, norte)));

        esperarRechazo("baja del último surtidor de una estación", () ->
                gestion.darDeBajaSurtidor(norte, norte.getSurtidores().iterator().next()));
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
