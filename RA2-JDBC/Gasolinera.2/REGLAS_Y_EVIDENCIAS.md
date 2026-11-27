# Reglas de negocio, evidencias y trabajo futuro

## 1. Reglas ya garantizadas

| Regla | Garantía | Evidencia |
|---|---|---|
| Código interno de estación y ficha no repetidos | `UNIQUE` + servicio de altas | `04_comprobaciones.sql`, intentos 1 y 2; `DemostracionEstaciones` |
| Exactamente una ficha por estación | `configuracion_id NOT NULL + UNIQUE + FK` | Consulta A de `04_comprobaciones.sql`: 5 estaciones / 5 fichas |
| Alta estación+ficha en una sola operación | Transacción en `03_datos.sql`; `AltaEstacion` | `03_datos.sql` termina con `COMMIT`; demo imprime las fichas |
| `codigo_equipo` único en toda la empresa | `UNIQUE` | Intento 4 |
| `numero_visible` único por estación, repetible entre estaciones | `UNIQUE (estacion_id, numero_visible)` | Intentos 3 y caso válido de la comprobación original; consulta de números repetidos |
| Surtidor averiado sigue en su estación | Estado del catálogo, no baja | Consulta de `AVERIADO` en `04_comprobaciones.sql` |
| Empleado con número y documento únicos | `UNIQUE` | Intento 6 usa estación inexistente para demostrar FK; `GestionPersonal` valida duplicados |
| Supervisor opcional, único y nunca uno mismo | columna nullable + FK + `CHECK <> id` | Consulta de empleados; demo `sinSupervisor` y `supervisoresDe` |
| Funciones simultáneas, independientes e historiales | `AsignacionFuncion` + tablas de condiciones | Consulta de periodos; demo muestra Ana con tres funciones y Carla con dos periodos de cajero |
| Cliente 0/1 tarjeta, tarjeta 0/1 titular | `UNIQUE(cliente_id)` + `CHECK` de fechas | Consulta de tarjetas; demo muestra TAR-002 sin titular y CLI-002 sin tarjeta |
| Precio > 0 y dos modalidades estables | `CHECK`, trigger de modalidad, interfaces selladas | Intento 16; demo calcula 3,60 € y 18,00 € |
| Cantidad de unidades completas / precisión de pesada | `Envasado` y `Granel` | Demo: rechaza 1,5 unidades y 0,52 kg; acepta 1,50 kg con precisión 0,05 |
| Cambio real de precio registrado por MySQL | `trg_producto_historial_precios` | Bloque D de `04_comprobaciones.sql`: 0 → 1 fila |
| Guardar el mismo precio y cambiar descripción no registran | `IF NEW <> OLD` | Bloque D: cuenta sigue en 1 |
| Crear producto no registra precio | Trigger es `AFTER UPDATE` | `historial_inicial = 0` tras `03_datos.sql` |
| Ticket anterior no se recalcula | `linea_venta` guarda precio e importe | Bloque D consulta A000101 a 1,20 € tras subir el catálogo; demo imprime lo mismo |
| Rollback revierte precio e historial | InnoDB + trigger en la misma transacción | Bloque D: rollback de PROD-002 deja 12,00 € y una sola fila de historial |
| Historial no editable por operaciones ordinarias | Triggers de update/delete | Procedimiento `probar_historial_no_editable`, intentos 17 y 18 |
| Referencia de ticket única, orden de línea único | `UNIQUE` | Intentos 12 y 13 |
| Cobro fraccionado | varias filas en `cobro_tienda` | Consulta A: A000101 con dos cobros; demo imprime efectivo + tarjeta |
| Devolución ligada a línea y con precio original | FK + `calcularImporteConPrecio` | Demo: 0,50 kg → 6,00 € y 0,25 kg → 3,00 €; línea sigue 1,50 kg a 12,00 € |
| `apta_para_reventa` se registra por separado | Columna booleana | Datos de ejemplo: primera devolución no apta, segunda apta |
| Incidencia sin o con un responsable actual | `empleado_responsable_id NULL` + FK | Consulta A: INC-001 e INC-004 sin responsable; EMP-004 sin incidencias |
| CSV/JSON siguen funcionando | Clases y ficheros sin cambios | Compilación y menú 1-5; demos no escriben en `datos/` |

## 2. Reglas pendientes y tratamiento propuesto

| Regla pendiente | Por qué no es automática en MySQL | Tratamiento en la fase siguiente |
|---|---|---|
| Al menos un surtidor por estación | No hay `CHECK` que pueda contar hijos | `SELECT COUNT(*) ... FOR UPDATE` en la transacción de baja; si es 1, rechazar |
| Ficha huérfana creada fuera del alta | La FK va de estación a ficha, no al revés | Sólo crear fichas desde el servicio de alta conjunta; transacción `commit/rollback` |
| Ciclos indirectos de supervisión | El `CHECK` sólo ve la fila actual | Recorrer la cadena de supervisores antes de asignar |
| Periodos solapados de una función | Un `CHECK` no compara filas | Consulta de solape por `funcion_id` dentro de la transacción |
| Proveedor/producto sin acuerdo | FK no puede exigir "al menos una fila en otra tabla" | Validar al confirmar altas/bajas con `GestionCatalogo` y bloqueo |
| Borrar el último acuerdo | Igual que el caso anterior | Contar acuerdos restantes antes de borrar |
| No devolver más de lo comprado (cantidad e importe) | Requiere agregar filas ya existentes y controlar concurrencia | Bloquear `linea_venta` (`FOR UPDATE`), sumar devoluciones, rechazar exceso |
| Confirmar cobro sólo si la suma coincide | Requiere agregar `cobro_tienda` | Sumar en la misma transacción y actualizar `confirmada_pagada` |
| Validar cantidad según modalidad en SQL | `DECIMAL` acepta 1,5 en un producto envasado | Trigger `BEFORE INSERT` en `linea_venta` y `devolucion` que consulte `producto_granel` |
| Carrito vacío / alta incompleta | No existe hasta que se inserta la venta | Validación en `Venta` y en la transacción JDBC |

`04_comprobaciones.sql` no las presenta como rechazos: las intenta dentro de una
transacción, imprime `ACEPTADO EN BD (pendiente)` y hace `ROLLBACK`.

## 3. Evidencias de comprobación

### Java

```bat
javac -encoding UTF-8 -d out src\*.java
java -Dfile.encoding=UTF-8 -cp out DemostracionEstaciones
java -Dfile.encoding=UTF-8 -cp out DemostracionAmpliacion
```

Resultado observado: compilación sin errores; `DemostracionEstaciones` imprime
las altas conjuntas, los surtidores y cuatro líneas `Rechazado como se esperaba`.
`DemostracionAmpliacion` imprime las secciones `PERSONAL`, `CLIENTES Y TARJETAS`,
`CATÁLOGO`, `CAMPAÑAS`, `INCIDENCIAS`, `VENTAS`, `DEVOLUCIONES` y once líneas
`Rechazado como se esperaba`. No se ha detectado ningún `REVISAR: se aceptó ...`.

### MySQL

Scripts, en este orden, sobre un esquema vacío:

```bat
mysql -u root -p < sql\01_esquema.sql
mysql -u root -p < sql\02_triggers.sql
mysql -u root -p < sql\03_datos.sql
mysql -u root -p < sql\04_comprobaciones.sql
```

**Limitación honesta del entorno de preparación**: en la máquina donde se
generó la entrega no había cliente ni servidor MySQL (`mysql`, `mysqld`,
`docker` y `podman` no están disponibles). Por tanto, las comprobaciones SQL
están escritas y revisadas, pero **no se han ejecutado contra MySQL**; quien
entregue debe ejecutarlas y adjuntar la salida o capturas. Los resultados
esperados están comentados junto a cada consulta y procedimiento. Sí se han
verificado en Java el cálculo, las validaciones, la conservación de precios y
las reglas rechazadas.

## 4. Registro de tres intervenciones relevantes de IA

| # | Qué se pidió | Qué propuso | Qué se aceptó o modificó | Verificación |
|---|---|---|---|---|
| 1 | "Termina el caso práctico" revisando el repositorio | Analizar el estado: sólo estaba hecho el apartado de estaciones; proponer las decisiones de diseño antes de escribir código y preguntarlas | Se aceptó trabajar en `RA2-JDBC/Gasolinera.2` y dejar CSV/JSON intactos. Se modificó el alcance para incluir todos los apartados del caso y sus scripts | Lectura de `CONTEXT.md`, `AGENTS.MD`, caso, clases y SQL existentes |
| 2 | Decisión de diseño: funciones del personal y modalidades | Interfaz sellada + `AsignacionFuncion` para las funciones; interfaz sellada + `BigDecimal` para importes; `CobroTienda` separado | Se aceptó la interfaz sellada para funciones ("la manera que mejor te parezca") y `CobroTienda`; se **modificó** el tipo numérico a `double` por decisión del estudiante, concentrando el redondeo en `RedondeoMonetario` | Compilación con JDK 21 y demostración con productos de las dos modalidades |
| 3 | Comprobación de comportamiento de las reglas | Añadir una demostración que mostrara casos válidos y rechazos, y un procedimiento SQL con `CONTINUE HANDLER` para errores esperados | Se aceptó. Se **modificó** `GestionCampanas.inscribir` porque la igualdad del `record` incluía la fecha y permitía inscribir dos veces con fechas distintas; ahora se compara campaña + cliente | Se ejecutó `DemostracionAmpliacion`: la inscripción repetida aparece como rechazada; se revisó que el procedimiento SQL separe rechazos de reglas pendientes |

## 5. Tres operaciones futuras mediante JDBC

1. **Alta conjunta de estación**
   - Entrada: código/nombre/dirección/teléfono/fecha de apertura, ficha
     (código, serie, siguiente número, pie) y primer surtidor (equipo, número,
     tipo, estado).
   - Reglas: `autoCommit=false`, INSERT de ficha → estación → surtidor,
     unicidad de códigos, al menos un surtidor, `commit()` sólo si todo pasa;
     `rollback()` ante cualquier error.

2. **Registrar venta y confirmar su cobro**
   - Entrada: referencia de ticket, estación, empleado, cliente opcional,
     líneas (producto, cantidad) y cobros (importe, fecha/hora, medio).
   - Reglas: carrito no vacío, orden de línea único, cantidad válida según
     modalidad, precio cobrado congelado en la línea, suma exacta al confirmar,
     ticket único en la empresa.

3. **Registrar devolución parcial**
   - Entrada: línea de venta, cantidad, motivo, fecha/hora, empleado y flag
     `apta_para_reventa`.
   - Reglas: venta ya pagada, cantidad compatible con la modalidad, cantidad
     acumulada ≤ comprada, importe acumulado ≤ importe original, precio de la
     línea (no el vigente), sin modificar la línea; bloquear la línea contra
     operaciones simultáneas.
