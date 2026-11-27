# Fase 1 — Las estaciones y su puesta en servicio

Este documento cubre **solo** el apartado `Las estaciones y su puesta en servicio` del
`Caso_practico_Gasolinera_MySQL_Fase_1`. No cambia el comportamiento de clientes,
pagos, CSV ni JSON. La clase `Main` sigue igual salvo por una opción nueva (6) que
lanza la demostración.

## Decisiones de diseño Java

| Decisión | Elección | Por qué |
| --- | --- | --- |
| Ficha de facturación | Clase separada `ConfiguracionFacturacion` | Soporte la identifica por su propio código; es una responsabilidad distinta de los datos de la estación. |
| Alta conjunta | `record AltaEstacion` + `construir()` | Estación, ficha y primer surtidor se entregan juntos o no se entregan. La transacción JDBC se añadirá en la fase siguiente. |
| Catálogos | Clases `TipoCombustible` y `EstadoServicio` con tabla propia | Evitan texto libre, permiten ampliar valores y hacen que el surtidor referencie una fila real. |
| Colección de surtidores | `LinkedHashSet<Surtidor>` en `Estacion` | `equals/hashCode` por `codigoEquipo`, así que el conjunto impide duplicados; el `numeroVisible` se valida dentro de la estación. Se devuelve una vista no modificable. |
| Lógica de altas/bajas | Servicio `GestionEstaciones` | `Main` no debe conocer las reglas de negocio. El servicio no ejecuta SQL. |
| Identificadores | `Integer id` que puede ser `null` | Representa "todavía no generado por MySQL" sin inventarse un 0 o un -1. |

`Estacion` tiene constructor de paquete: la vía normal es `AltaEstacion.construir()`.
Los mecanismos de persistencia futuros necesitarán una factoría de carga que pase el
`id` existente, pero no deben poder construir una estación sin ficha.

## Correspondencia Java ↔ MySQL

| Java | MySQL | Relación |
| --- | --- | --- |
| `Estacion` | `estacion` | `configuracion_id` FK NOT NULL y UNIQUE → exactamente una ficha. |
| `ConfiguracionFacturacion` | `configuracion_facturacion` | Se inserta **antes** que la estación. |
| `Surtidor` | `surtidor` | `estacion_id` NOT NULL; `codigo_equipo` UNIQUE global; `UNIQUE (estacion_id, numero_visible)`. |
| `TipoCombustible` | `catalogo_tipo_combustible` | FK `ON DELETE RESTRICT`. |
| `EstadoServicio` | `catalogo_estado_servicio` | FK `ON DELETE RESTRICT`. |
| `AltaEstacion` | (no es tabla) | Traduce una alta conjunta a dos INSERT en una transacción. |
| `GestionEstaciones` | (no es tabla) | Reglas que todavía no son restricciones de BD. |

Todas las tablas usan `INT AUTO_INCREMENT` como clave primaria simple, `InnoDB` y
`utf8mb4`. Las referencias de negocio (`codigo_interno`, `codigo_configuracion`,
`codigo_equipo`) son `UNIQUE`, no claves primarias.

## Reglas: dónde quedan garantizadas

| Regla | Garantía actual |
| --- | --- |
| Código interno de estación no repetido | `UNIQUE (codigo_interno)` + validación en `GestionEstaciones.darDeAlta`. |
| Una estación, exactamente una ficha | `estacion.configuracion_id` NOT NULL + UNIQUE + FK. |
| No borrar la ficha en uso | `ON DELETE RESTRICT` en esa FK. |
| Alta atómica estación + ficha | Transacción en `03_datos.sql`; en Java, `AltaEstacion`. La fase JDBC deberá envolver ambos INSERT en `commit/rollback`. |
| `codigo_equipo` único en la empresa | `UNIQUE (codigo_equipo)` + `GestionEstaciones`. |
| `numeroVisible` único por estación, repetible entre estaciones | `UNIQUE (estacion_id, numero_visible)` + `Estacion.añadirSurtidor`. |
| Surtidor siempre con estación | `estacion_id` NOT NULL + FK; el constructor de `Surtidor` exige estación. |
| Surtidor averiado sigue perteneciendo | Es un valor del catálogo de estados, no una baja. |
| **Al menos un surtidor por estación** | **Pendiente en BD**: se valida en `Estacion.darDeBajaSurtidor`; en la siguiente fase se implementará con control de aplicación/trigger que compruebe el último equipo. |
| **Ficha huérfana creada fuera del alta** | **Pendiente**: no se puede impedir con FK no diferible en MySQL; la fase JDBC controlará que toda ficha se cree dentro del alta conjunta. |

## Scripts y orden de ejecución

Desde `Gasolinera.2/sql`, sobre MySQL vacío y en este orden:

1. `01_esquema.sql` — crea `gasolinera_fase1` y las cinco tablas.
2. `02_triggers.sql` — sin triggers en este apartado (el de precios llega después).
3. `03_datos.sql` — 5 estaciones con su ficha, 9 surtidores y catálogos; una transacción.
4. `04_comprobaciones.sql` — consultas correctas y un procedimiento con
   `CONTINUE HANDLER` para provocar ocho intentos, siete rechazados y uno válido.

## Compilación y demostración Java

```bat
javac -encoding UTF-8 -d out src\*.java
java -Dfile.encoding=UTF-8 -cp out DemostracionEstaciones
```

También está la opción `6` del menú `Main`. La demostración imprime tres
estaciones, los surtidores (con el nº 1 repetido en estaciones distintas), el
reservado de tickets `A000101` / `A000102` y cuatro reglas rechazadas: estación
duplicada, número visible repetido, equipo duplicado y baja del último surtidor.
