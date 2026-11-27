# Modelo Java de dominio — Fase 1

Todas las clases nuevas están en `src/` (paquete por defecto, igual que el
proyecto existente). Ninguna abre conexión ni ejecuta SQL: la persistencia MySQL
se incorporará en la fase siguiente.

## Decisiones de diseño y porqué

| Decisión | Elección | Por qué |
|---|---|---|
| Funciones del personal | `sealed interface Funcion` con `Cajero`, `Expendedor`, `ResponsableTienda`, `JefeEstacion`; una asignación es un `record AsignacionFuncion(funcion, inicio, fin)` | Composición, no herencia: un empleado *tiene* periodos, no *es* una función. Cada función lleva sus condiciones propias (límite, acreditación, presupuesto, nivel) sin columnas opcionales. El `record` hace explícito que un periodo histórico no se modifica; para retirarlo se crea uno nuevo con `fin`. Añadir una función futura es añadir una implementación permitida; no cambia `Empleado` ni los servicios. |
| Alternativa descartada | `enum TipoFuncion` con campos `null` | Obligaría a validar combinaciones imposibles y a modificar una clase central cada vez que aparezca una función con datos nuevos. |
| Modalidades de producto | `sealed interface ModalidadVenta` con `Envasado(formato)` y `Granel(precisionPesada)` | `LineaVenta` y `Devolucion` llaman a `normalizarCantidad` y `calcularImporte` sin preguntar el tipo. El compilador obliga a revisar cualquier uso exhaustivo si se añade una modalidad. |
| Tipo numérico | `double` (decisión del estudiante) | Se conserva el criterio pedido. Para compensar la imprecisión, **todo** importe pasa por `RedondeoMonetario.monetario()` (2 decimales) y las comparaciones por `iguales()` con tolerancia. Las cantidades usan `RedondeoMonetario.cantidad()` y la precisión de `Granel`. |
| Dinero alternativo descartado | `BigDecimal` | Más exacto, pero se descartó por decisión explícita; no se mezcla con `double` para evitar dos criterios de redondeo conviviendo. MySQL sí usa `DECIMAL`, precisión que se aplica al persistir. |
| Cobros de tienda | `CobroTienda` separado de `Pago` | `Cliente`, `Pago`, CSV y JSON se dejan intactos. Un cobro de tienda pertenece a `Venta`; el `Pago` de repostaje conserva `idCliente`, `fecha`, `importe`, `litros` y `combustible`. No se reinterpreta ningún registro antiguo. |
| Identificadores | `Integer id` que puede ser `null` | `null` significa "todavía no generado por MySQL", sin inventar 0 ni -1. Las igualdades de dominio usan referencias de negocio (`codigoEquipo`, `numeroInterno`, `referenciaComercial`, `referenciaTicket`, ...). |
| Colecciones | `Set` + vistas no modificables | `Estacion` usa `LinkedHashSet<Surtidor>` (orden de alta, sin duplicados por `codigoEquipo`); `GestionPersonal` y `GestionCatalogo` usan `LinkedHashSet`; `Venta` usa `List` porque el orden de línea es parte del negocio. Las lists de función/devoluciones se devuelven sin modificar para que nadie salte las validaciones. |
| Reglas compartidas | Clases de servicio `Gestion*` | `Main` sigue siendo sólo interfaz de consola. Los servicios concentran altas, bajas, solapes, ciclos, acuerdos, inscripciones, cobros y devoluciones. |
| Programación funcional | `stream`, `map`, `filter`, `anyMatch`, `Optional`, `collect` | Se usa en búsquedas, validaciones de duplicados, listados ordenados y cálculos de totales. Quedan bucles simples sólo donde se recorre una cadena de supervisión o se busca un índice a reemplazar. |

## Clases del dominio

### Persistentes con correspondencia directa

`Estacion`, `ConfiguracionFacturacion`, `Surtidor`, `TipoCombustible`,
`EstadoServicio`, `Empleado`, `AsignacionFuncion`, `Cajero`, `Expendedor`,
`ResponsableTienda`, `JefeEstacion`, `ClienteTienda`, `TarjetaFidelizacion`,
`Producto`, `Envasado`, `Granel`, `EstadoComercializacion`, `Proveedor`,
`AcuerdoSuministro`, `Campana`, `InscripcionCampana`, `Venta`, `LineaVenta`,
`CobroTienda`, `MedioPago`, `EstadoCobro`, `Devolucion`, `Incidencia`,
`EstadoIncidencia`.

### No persistentes

`AltaEstacion` (transporte de alta conjunta), `GestionEstaciones`,
`GestionPersonal`, `GestionFidelizacion`, `GestionCatalogo`, `GestionCampanas`,
`GestionVentas`, `GestionDevoluciones`, `GestionIncidencias`,
`RedondeoMonetario`, `DemostracionEstaciones`, `DemostracionAmpliacion`.

Se superan las 15 clases de referencia. El número es mayor porque el caso pide
historial de funciones, condiciones específicas, dos modalidades, acuerdos e
inscripciones con identidad propia; no se han creado clases vacías.

## Correspondencia Java ↔ MySQL

| Java | MySQL | Observación |
|---|---|---|
| `Estacion` | `estacion` | `configuracion_id NOT NULL UNIQUE` |
| `ConfiguracionFacturacion` | `configuracion_facturacion` | Se inserta antes que la estación |
| `Surtidor` | `surtidor` | `codigo_equipo` global; `(estacion_id, numero_visible)` |
| `TipoCombustible`, `EstadoServicio` | `catalogo_tipo_combustible`, `catalogo_estado_servicio` | Catálogos |
| `Empleado` | `empleado` | `supervisor_id` autorreferencial |
| `Funcion` + implementaciones | `catalogo_funcion` + `funcion_cajero`/`funcion_expendedor`/`funcion_responsable_tienda`/`funcion_jefe_estacion` | Una asignación + una fila de detalles |
| `AsignacionFuncion` | `asignacion_funcion` | `fecha_fin NULL` = vigente |
| `ClienteTienda` | `cliente_tienda` | No confundir con el `Cliente` de CSV/JSON |
| `TarjetaFidelizacion` | `tarjeta_fidelizacion` | `cliente_id UNIQUE` nullable |
| `Producto` | `producto` | `precio_venta` con trigger de historial |
| `ModalidadVenta` | `catalogo_modalidad_producto` | `Envasado` → `producto_envasado`; `Granel` → `producto_granel` |
| `Proveedor`, `AcuerdoSuministro` | `proveedor`, `acuerdo_suministro` | `UNIQUE(proveedor_id, producto_id)` |
| `Campana`, `InscripcionCampana` | `campana`, `inscripcion_campana` | `UNIQUE(campana_id, cliente_id)` |
| `Venta`, `LineaVenta` | `venta`, `linea_venta` | Precio cobrado congelado en la línea |
| `CobroTienda` | `cobro_tienda` | Varios por venta; `Pago` antiguo no se toca |
| `Devolucion` | `devolucion` | Importe calculado con precio de la línea |
| `Incidencia` | `incidencia` | Sólo responsable actual |
| `RedondeoMonetario` | `DECIMAL(12,2)` / `DECIMAL(14,2)` | Mismo criterio de 2 decimales |

## Conservación de CSV y JSON

`Almacenamiento`, `AlmacenamientoCSV`, `GestionJson`, `MigracionCsvaJson`,
`Cliente` y `Pago` no se han modificado. El menú mantiene las opciones 1-5 y
añade la 6 (estaciones) y la 7 (ampliación). Las demostraciones no escriben en
`datos/`, por lo que los ficheros `clientes.csv`, `pagos.csv`, `clientes.json` y
`pagos.json` siguen con su contenido y formato anteriores.

## Demostración

`DemostracionAmpliacion.ejecutar()` construye objetos relacionados de las dos
modalidades y muestra:

- 3 x agua envasada = 3,60 € y 1,50 kg de almendras = 18,00 €.
- Unidades completas y precisión 0,05 rechazando 1,5 unidades y 0,52 kg.
- Empleado sin supervisor, uno que supervisa a dos, alguien sin funciones,
  varias funciones simultáneas y una función recuperada tras un periodo cerrado.
- Tarjetas asignadas y sin titular; clientes con y sin tarjeta.
- Proveedor con dos productos y producto con dos proveedores.
- Campaña vacía, cliente sin campañas e inscripciones múltiples.
- Incidencia sin responsable y empleado con dos incidencias.
- Tickets anónimos e identificados, uno con dos cobros y otro con línea de
  producto retirado.
- Dos devoluciones parciales sucesivas; la línea original permanece intacta.
- Once reglas rechazadas con su motivo.

Ejecución:

```bat
javac -encoding UTF-8 -d out src\*.java
java -Dfile.encoding=UTF-8 -cp out DemostracionEstaciones
java -Dfile.encoding=UTF-8 -cp out DemostracionAmpliacion
```
