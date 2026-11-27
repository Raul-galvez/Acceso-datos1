# Gasolinera.2 — Ampliación MySQL (Fase 1)

Proyecto de consola (Java 21, IntelliJ IDEA) que conserva la aplicación original
de clientes y pagos en CSV/JSON y añade el **modelo de dominio**, el **modelo
relacional** y los **scripts SQL** de la Fase 1 del caso práctico.

> Esta fase **no implementa consultas JDBC desde Java**: sólo prepara la
> conexión, las clases, los scripts y la demostración en memoria. La persistencia
> MySQL se añadirá en la fase siguiente.

## Requisitos

- JDK 21.
- MySQL 8.x con soporte para `CHECK`, `GET DIAGNOSTICS` y triggers
  (MySQL 8.0.16+ para que los `CHECK` se apliquen).
- Cliente `mysql` o herramienta SQL que acepte `DELIMITER` para
  `04_comprobaciones.sql`.
- IntelliJ IDEA opcional; también funciona con `javac`/`java`.

No hay credenciales en el código ni en los scripts. La futura conexión deberá
leer URL, usuario y contraseña de variables de entorno o argumentos, como se
hace en el bloque JDBC del jardín botánico (`JARDIN_MYSQL_URL`, etc.).

## Documentación

| ARCHIVO | CONTENIDO |
|---|---|
| `CASO_PRACTICO...md` (carpeta superior) | Enunciado del caso |
| `DISENO_RELACIONAL.md` | Tablas, columnas, tipos, claves, restricciones y dónde queda cada regla |
| `MODELO_JAVA.md` | Decisiones de diseño Java y correspondencia con MySQL |
| `REGLAS_Y_EVIDENCIAS.md` | Reglas garantizadas/pendientes, evidencias, intervenciones de IA y operaciones futuras |
| `ESTACIONES.md` | Documentación del primer apartado (estaciones) |
| `AUDITORIA_IA.odt` | Ejemplos concretos de aciertos y errores de la IA |
| `AGENTS.md` | Instrucciones para agentes que trabajen en esta carpeta |

## Preparación de MySQL

Desde esta carpeta, sobre un esquema vacío y **en este orden**:

```bat
mysql -u root -p < sql\01_esquema.sql
mysql -u root -p < sql\02_triggers.sql
mysql -u root -p < sql\03_datos.sql
mysql -u root -p < sql\04_comprobaciones.sql
```

- `01_esquema.sql` crea `gasolinera_fase1`, sus catálogos y tablas `InnoDB`.
- `02_triggers.sql` crea el trigger obligatorio de historial de precios y tres
  triggers de refuerzo (modalidad inmutable e historial no editable).
- `03_datos.sql` carga todo en una transacción: 5 estaciones con ficha, 9
  surtidores, 5 empleados, 5 clientes, 5 productos, 5 proveedores, 3 campañas,
  5 ventas, 12 líneas, 6 cobros, 3 devoluciones y 5 incidencias.
- `04_comprobaciones.sql` imprime resultados esperados, provoca 18 intentos
  inválidos (rechazados por MySQL), muestra 6 reglas **aceptadas en BD y
  pendientes** (se deshacen con `ROLLBACK`) y comprueba el trigger, incluido un
  `ROLLBACK` de un cambio de precio.

No se desactivan claves foráneas en ningún script.

Si la herramienta SQL no entiende `DELIMITER`, ejecuta los bloques de trigger y
procedimientos por separado cambiando el delimitador en la interfaz.

## Compilación y demostración Java

```bat
javac -encoding UTF-8 -d out src\*.java
java -Dfile.encoding=UTF-8 -cp out DemostracionEstaciones
java -Dfile.encoding=UTF-8 -cp out DemostracionAmpliacion
```

También puedes abrir el proyecto en IntelliJ y ejecutar las configuraciones
`Main`, `DemostracionEstaciones` y `DemostracionAmpliacion`. En el menú, las
opciones **6** y **7** lanzan respectivamente ambas demostraciones; las opciones
**1 a 5** son las originales de clientes/pagos CSV/JSON.

`DemostracionAmpliacion` muestra, entre otros casos:

- Productos envasados y a granel calculados por la misma interfaz.
- Funciones simultáneas, historial de funciones y supervisión sin ciclos.
- Clientes con/sin tarjeta y tarjetas sin titular.
- Ventas anónimas e identificadas, cobro fraccionado y precio histórico intacto.
- Devoluciones parciales sucesivas sin alterar la línea original.
- Reglas rechazadas con su motivo.

Las demostraciones **no escriben** en `datos/`, por lo que CSV y JSON siguen
funcionando con sus ficheros anteriores.

## Estado de verificación

- Java: compilado y ejecutado con JDK 21; demostraciones sin errores.
- MySQL: scripts preparados y revisados. En el entorno de preparación no había
  cliente/servidor MySQL, por lo que las comprobaciones SQL deben ejecutarse en
  el equipo de entrega; los resultados esperados están comentados en los scripts.
