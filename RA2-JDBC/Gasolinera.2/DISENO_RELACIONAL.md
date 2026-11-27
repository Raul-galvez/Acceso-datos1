# Modelo relacional y diccionario de datos — Fase 1

MySQL 8.x, `InnoDB`, `utf8mb4_unicode_ci`. No se presenta diagrama entidad-relación:
se usa notación textual y tablas, como pide el caso.

## Convenciones generales

- **Todas** las tablas, incluidas asociaciones, históricos y líneas, tienen una
  clave primaria formada por **un único `INT AUTO_INCREMENT`** llamado `id`.
- Las referencias de negocio (`codigo_interno`, `referencia_comercial`,
  `numero_tarjeta`, `referencia_ticket`, ...) son `UNIQUE`, pero **no** son PK.
- Los identificadores técnicos (`id`) se usan en las claves foráneas. Los códigos
  de negocio se conservan para búsquedas y trazabilidad.
- `ON UPDATE CASCADE` se usa cuando el ID técnico pudiera cambiar; en la práctica
  los ID no se editan, pero mantiene la coherencia.
- `ON DELETE RESTRICT` protege los históricos. Las tablas de detalle 1:1 usan
  `ON DELETE CASCADE` desde su asignación/producto porque no tienen vida propia.
- Salvo indicación expresa, las columnas son `NOT NULL` y no tienen valor
  predeterminado. Los únicos `DEFAULT` son `confirmada_pagada FALSE`,
  `fecha_cambio CURRENT_TIMESTAMP(6)` y `apta_para_reventa FALSE`.
- El redondeo monetario acordado es a **2 decimales** (`DECIMAL(12,2)` para
  precios e importes unitarios, `DECIMAL(14,2)` para importes de línea). Las
  cantidades son `DECIMAL(12,3)`, suficiente para la precisión de pesada.

---

## 1. Estaciones y su puesta en servicio

### `configuracion_facturacion`

Ficha de facturación propia de una estación. Identidad separada de la estación:
soporte la localiza por `codigo_configuracion`.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `codigo_configuracion` | `VARCHAR(30)` | sí | `UNIQUE` |
| `serie_tickets` | `VARCHAR(10)` | sí | — |
| `siguiente_numero` | `INT` | sí | `CHECK > 0` |
| `texto_pie` | `VARCHAR(300)` | sí | — |

### `estacion`

Estación de servicio. `configuracion_id` `NOT NULL` + `UNIQUE` + FK garantiza
**exactamente una ficha** y que no se comparta.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `codigo_interno` | `VARCHAR(20)` | sí | `UNIQUE` |
| `nombre_comercial` | `VARCHAR(120)` | sí | — |
| `direccion` | `VARCHAR(250)` | sí | — |
| `telefono` | `VARCHAR(30)` | sí | — |
| `fecha_apertura` | `DATE` | sí | — |
| `configuracion_id` | `INT` | sí | `UNIQUE`, FK → `configuracion_facturacion(id)`, `ON DELETE RESTRICT` |

### `catalogo_tipo_combustible`, `catalogo_estado_servicio`

Catálogos acotados. Columnas: `id INT AI PK`, `codigo VARCHAR(20) UNIQUE`,
`nombre VARCHAR(80)`. Evitan texto libre en el surtidor.

### `surtidor`

Equipo de una estación. El estado es un catálogo: un equipo averiado **no** se
da de baja.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `estacion_id` | `INT` | sí | FK → `estacion(id)`, `ON DELETE CASCADE` |
| `codigo_equipo` | `VARCHAR(30)` | sí | `UNIQUE` global |
| `numero_visible` | `INT` | sí | `CHECK > 0`, `UNIQUE (estacion_id, numero_visible)` |
| `tipo_combustible_id` | `INT` | sí | FK → catálogo, `RESTRICT` |
| `estado_servicio_id` | `INT` | sí | FK → catálogo, `RESTRICT` |

El `UNIQUE` compuesto permite que el número 1 exista en varias estaciones pero
no dos veces en la misma.

---

## 2. Personal y organización del trabajo

### `empleado`

Persona con identidad estable. La estación actual es un dato mutable; las ventas
guardan su propia estación, así que el cambio no reescribe ventas anteriores.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `numero_interno` | `VARCHAR(30)` | sí | `UNIQUE` |
| `documento_identidad` | `VARCHAR(30)` | sí | `UNIQUE` |
| `nombre` | `VARCHAR(120)` | sí | — |
| `telefono` | `VARCHAR(30)` | **no** | al menos uno de `telefono`/`email` |
| `email` | `VARCHAR(160)` | **no** | ídem (`CHECK`) |
| `fecha_incorporacion` | `DATE` | sí | — |
| `estacion_id` | `INT` | sí | FK → `estacion(id)`, `RESTRICT` |
| `supervisor_id` | `INT` | **no** | FK → `empleado(id)`, `RESTRICT`; `CHECK <> id` |

La supervisión actual es autorreferencial y opcional. El `CHECK` prohíbe la
autorreferencia directa; los ciclos indirectos quedan para la fase JDBC.

### `catalogo_funcion`

`id INT AI PK`, `codigo VARCHAR(40) UNIQUE`, `nombre VARCHAR(80)`. Valores:
`CAJERO`, `EXPENDEDOR`, `RESPONSABLE_TIENDA`, `JEFE_ESTACION`. Permite añadir
funciones futuras sin alterar `empleado`.

### `asignacion_funcion`

Periodo en que una persona desempeñó una función. El historial se conserva:
retirar y recuperar una función crea otra fila.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `empleado_id` | `INT` | sí | FK → `empleado(id)`, `CASCADE` |
| `funcion_id` | `INT` | sí | FK → `catalogo_funcion(id)`, `RESTRICT` |
| `fecha_inicio` | `DATE` | sí | — |
| `fecha_fin` | `DATE` | **no** | `NULL` = vigente; `CHECK >= fecha_inicio` |

No hay `UNIQUE` que prohíba solapes: se indican como pendientes. Sí pueden
coexistir funciones distintas para la misma persona.

### Tablas de condiciones específicas

Cada una es 1:1 con `asignacion_funcion` mediante `asignacion_funcion_id UNIQUE`
y su propia PK autoincremental. `ON DELETE CASCADE` porque la condición no existe
sin la asignación.

| Tabla | Columna específica | Tipo / restricción |
|---|---|---|
| `funcion_cajero` | `limite_devoluciones` | `DECIMAL(12,2) > 0` |
| `funcion_expendedor` | `numero_acreditacion`, `caducidad` | `VARCHAR(40) UNIQUE`, `DATE` |
| `funcion_responsable_tienda` | `presupuesto_compras` | `DECIMAL(12,2) > 0` |
| `funcion_jefe_estacion` | `nivel_autorizacion` | `INT > 0` |

---

## 3. Clientes y tarjetas de fidelización

### `cliente_tienda`

Cliente registrado de la tienda, distinto del `Cliente` heredado de CSV/JSON.

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `codigo_cliente` | `VARCHAR(30)` | sí | `UNIQUE` |
| `nombre` | `VARCHAR(120)` | sí | — |
| `telefono` | `VARCHAR(30)` | **no** | al menos uno de `telefono`/`email` |
| `email` | `VARCHAR(160)` | **no** | ídem (`CHECK`) |

**Criterio documentado**: ambos medios de contacto son opcionales por separado
(un cliente puede dar sólo email o sólo teléfono), pero al menos uno debe existir
para poder localizarle.

### `tarjeta_fidelizacion`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `numero_tarjeta` | `VARCHAR(30)` | sí | `UNIQUE` |
| `fecha_emision` | `DATE` | sí | — |
| `fecha_asignacion` | `DATE` | **no** | obligatoria si hay titular |
| `cliente_id` | `INT` | **no** | `UNIQUE`, FK → `cliente_tienda(id)`, `RESTRICT` |

`CHECK` garantiza que tarjeta sin titular tenga `fecha_asignacion NULL`, y que
una asignada tenga ambas. `UNIQUE(cliente_id)` impide dos tarjetas simultáneas;
varias filas con `NULL` están permitidas en MySQL.

---

## 4. Tienda y productos

### `catalogo_estado_comercializacion`, `catalogo_modalidad_producto`

`id INT AI PK`, `codigo VARCHAR(20) UNIQUE`, `nombre VARCHAR(60)`. Valores:
`ACTIVO`/`RETIRADO` y `ENVASADO`/`GRANEL`.

### `producto`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `referencia_comercial` | `VARCHAR(40)` | sí | `UNIQUE` |
| `descripcion` | `VARCHAR(200)` | sí | — |
| `precio_venta` | `DECIMAL(12,2)` | sí | `CHECK > 0` |
| `estado_id` | `INT` | sí | FK → catálogo de estados, `RESTRICT` |
| `modalidad_id` | `INT` | sí | FK → catálogo de modalidades, `RESTRICT`; inmutable por trigger |

### `producto_envasado`, `producto_granel`

Detalles 1:1 con `producto`:

| Tabla | Columna | Tipo / restricción |
|---|---|---|
| `producto_envasado` | `formato_envase` | `VARCHAR(100)` |
| `producto_granel` | `precision_pesada` | `DECIMAL(10,3)`, `CHECK > 0 AND <= 1` |

Ambas tienen `id INT AI PK`, `producto_id INT UNIQUE` con FK `ON DELETE CASCADE`.
Separarlas evita columnas opcionales y permite validar cada modalidad.

### `historial_precios`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `producto_id` | `INT` | sí | FK → `producto(id)`, `RESTRICT` |
| `precio_anterior` | `DECIMAL(12,2)` | sí | — |
| `precio_nuevo` | `DECIMAL(12,2)` | sí | — |
| `fecha_cambio` | `DATETIME(6)` | sí | `DEFAULT CURRENT_TIMESTAMP(6)` |

Sólo se inserta desde el trigger `trg_producto_historial_precios`. Dos triggers
adicionales impiden editar o borrar filas por operaciones ordinarias.

---

## 5. Proveedores del catálogo

### `proveedor`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `codigo_interno` | `VARCHAR(30)` | sí | `UNIQUE` |
| `identificacion_fiscal` | `VARCHAR(40)` | sí | `UNIQUE` |
| `razon_social` | `VARCHAR(160)` | sí | — |
| `telefono` | `VARCHAR(30)` | **no** | al menos uno de `telefono`/`email` |
| `email` | `VARCHAR(160)` | **no** | ídem (`CHECK`) |

Sólo se registran proveedores con acuerdo, pero la BD no puede exigir "al menos
un acuerdo" con una FK; se controla al confirmar altas/bajas.

### `acuerdo_suministro`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `proveedor_id` | `INT` | sí | FK → `proveedor(id)`, `RESTRICT` |
| `producto_id` | `INT` | sí | FK → `producto(id)`, `RESTRICT` |
| `referencia_proveedor` | `VARCHAR(60)` | sí | — |
| `precio_compra` | `DECIMAL(12,2)` | sí | `CHECK > 0` |
| `plazo_entrega_dias` | `INT` | sí | `CHECK >= 0` |

`UNIQUE (proveedor_id, producto_id)` prohíbe duplicar el acuerdo vigente.

---

## 6. Campañas promocionales

### `campana`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `codigo` | `VARCHAR(30)` | sí | `UNIQUE` |
| `nombre` | `VARCHAR(120)` | sí | — |
| `descripcion` | `VARCHAR(300)` | sí | — |
| `fecha_inicio` | `DATE` | sí | — |
| `fecha_fin` | `DATE` | sí | `CHECK >= fecha_inicio` |

### `inscripcion_campana`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `campana_id` | `INT` | sí | FK → `campana(id)`, `CASCADE` |
| `cliente_id` | `INT` | sí | FK → `cliente_tienda(id)`, `RESTRICT` |
| `fecha_inscripcion` | `DATE` | sí | — |

`UNIQUE (campana_id, cliente_id)`. La tarjeta no es requisito: basta el cliente
registrado.

---

## 7. Ventas, pagos y devoluciones

### `catalogo_medio_pago`

`id INT AI PK`, `codigo VARCHAR(20) UNIQUE`, `nombre VARCHAR(60)`. Valores:
`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`.

### `venta`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `referencia_ticket` | `VARCHAR(40)` | sí | `UNIQUE` en la empresa |
| `fecha_hora` | `DATETIME(6)` | sí | — |
| `estacion_id` | `INT` | sí | FK → `estacion(id)`, `RESTRICT` (foto del momento) |
| `empleado_id` | `INT` | sí | FK → `empleado(id)`, `RESTRICT` (autoría) |
| `cliente_id` | `INT` | **no** | FK → `cliente_tienda(id)`, `RESTRICT`; `NULL` = anónima |
| `confirmada_pagada` | `BOOLEAN` | sí | `DEFAULT FALSE` |

El estado `PENDIENTE`/`PARCIAL`/`PAGADA` se deriva de `confirmada_pagada` y de
la suma de `cobro_tienda`. La comprobación de que la suma coincide al confirmar
es control de aplicación/JDBC, no un `CHECK` de MySQL.

### `linea_venta`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `venta_id` | `INT` | sí | FK → `venta(id)`, `CASCADE` |
| `producto_id` | `INT` | sí | FK → `producto(id)`, `RESTRICT` |
| `numero_orden` | `INT` | sí | `UNIQUE (venta_id, numero_orden)` |
| `cantidad` | `DECIMAL(12,3)` | sí | `CHECK > 0` |
| `precio_unitario` | `DECIMAL(12,2)` | sí | `CHECK > 0`; precio cobrado, no vigente |
| `importe` | `DECIMAL(14,2)` | sí | `CHECK > 0` |

"Al menos una línea" no se puede imponer con FK; se garantiza en la creación de
la venta y se abordará en la transacción JDBC.

### `cobro_tienda`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `venta_id` | `INT` | sí | FK → `venta(id)`, `RESTRICT` |
| `importe` | `DECIMAL(12,2)` | sí | `CHECK > 0` |
| `fecha_hora` | `DATETIME(6)` | sí | — |
| `medio_pago_id` | `INT` | sí | FK → catálogo, `RESTRICT` |

Varias filas por venta permiten efectivo + tarjeta. `Pago` de CSV/JSON no se
tocó: su información sigue en `clientes.csv`/`pagos.csv` y en `clientes.json`/
`pagos.json`; la correspondencia futura se documenta en `MODELO_JAVA.md`.

### `devolucion`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `linea_venta_id` | `INT` | sí | FK → `linea_venta(id)`, `RESTRICT` |
| `cantidad` | `DECIMAL(12,3)` | sí | `CHECK > 0` |
| `motivo` | `VARCHAR(250)` | sí | — |
| `fecha_hora` | `DATETIME(6)` | sí | — |
| `empleado_id` | `INT` | sí | FK → `empleado(id)`, `RESTRICT` |
| `importe_reintegrado` | `DECIMAL(12,2)` | sí | `CHECK > 0`; calculado con precio original |
| `apta_para_reventa` | `BOOLEAN` | sí | `DEFAULT FALSE` |

Varias devoluciones sucesivas por línea. La suma acumulada no supera cantidad ni
importe: regla recogida aquí y controlada en Java; su implementación concurrente
queda pendiente.

---

## 8. Incidencias

### `catalogo_estado_incidencia`

`id INT AI PK`, `codigo VARCHAR(20) UNIQUE`, `nombre VARCHAR(60)`:
`ABIERTA`, `EN_CURSO`, `RESUELTA`.

### `incidencia`

| Columna | Tipo | Obligatoria | Restricción |
|---|---|---|---|
| `id` | `INT AI PK` | sí | clave técnica |
| `referencia` | `VARCHAR(30)` | sí | `UNIQUE` |
| `descripcion` | `VARCHAR(300)` | sí | — |
| `fecha_apertura` | `DATETIME(6)` | sí | — |
| `estacion_id` | `INT` | sí | FK → `estacion(id)`, `RESTRICT` |
| `estado_id` | `INT` | sí | FK → catálogo, `RESTRICT` |
| `empleado_responsable_id` | `INT` | **no** | FK → `empleado(id)`, `RESTRICT` |

Sólo se guarda el responsable actual. No se confunde con supervisor ni funciones.

---

## 9. Dónde queda cada regla del relato

| Regla | Dónde se garantiza |
|---|---|
| Código interno de estación no repetido | `UNIQUE (codigo_interno)` + `GestionEstaciones` |
| Exactamente una ficha por estación | `configuracion_id NOT NULL + UNIQUE + FK` |
| No borrar ficha en uso | `ON DELETE RESTRICT` |
| Alta atómica estación + ficha + primer surtidor | Transacción en `03_datos.sql`; en Java `AltaEstacion`; JDBC deberá `commit/rollback` |
| Ficha huérfana creada fuera del alta | **Pendiente**: control de aplicación en la fase JDBC |
| Código de equipo único en la empresa | `UNIQUE (codigo_equipo)` |
| Número visible único por estación, repetible entre estaciones | `UNIQUE (estacion_id, numero_visible)` |
| Al menos un surtidor por estación | **Pendiente**: `Estacion.darDeBajaSurtidor` y transacción futura |
| Surtidor averiado sigue perteneciendo | `estado_servicio_id` es un catálogo, no una baja |
| Funciones independientes, simultáneas y con historial | `asignacion_funcion` + tablas de condiciones; Java `AsignacionFuncion` |
| Periodos solapados de una misma función | **Pendiente en BD**: validado en `Empleado.asignarFuncion` |
| Empleados sin funciones temporalmente | `asignacion_funcion` opcional por fecha/ausencia; permitido |
| Supervisor único, opcional, no uno mismo | columna nullable + FK + `CHECK supervisor_id <> id` |
| Ciclos indirectos de supervisión | **Pendiente**: `GestionPersonal.validarSupervisor` |
| Cliente con 0/1 tarjeta, tarjeta con 0/1 titular | `UNIQUE(cliente_id)`, `CHECK` de fechas, `GestionFidelizacion` |
| Referencia de negocio de tarjeta/cliente distinta del ID | columnas `UNIQUE` + `id` técnico |
| Precio de venta siempre informado y > 0 | `NOT NULL + CHECK > 0` + `Producto.cambiarPrecio` |
| Modalidad estable | Trigger `trg_producto_modalidad_inmutable` + campo `final` en Java |
| Cantidad válida según modalidad y redondeo | Java `Envasado`/`Granel` + `RedondeoMonetario`; **pendiente** de `CHECK`/trigger SQL |
| Cambio real de precio registrado en MySQL | Trigger obligatorio `trg_producto_historial_precios` |
| Mismo precio / cambiar descripción no generan historial | `IF NEW.precio_venta <> OLD.precio_venta` |
| Alta inicial no genera historial | Trigger es `AFTER UPDATE` |
| Historial no editable | Triggers `trg_historial_no_editar` y `trg_historial_no_borrar` |
| Rollback elimina historial del cambio | InnoDB: trigger inserta en la misma transacción |
| Proveedor con al menos un producto y viceversa | **Pendiente**: `GestionCatalogo.validarSinAislados` en altas/bajas |
| No duplicar acuerdo vigente | `UNIQUE (proveedor_id, producto_id)` |
| No eliminar último acuerdo | **Pendiente**: `GestionCatalogo.eliminarAcuerdo` |
| Campaña con fin >= inicio | `CHECK (fecha_fin >= fecha_inicio)` |
| No duplicar inscripción | `UNIQUE (campana_id, cliente_id)` |
| Participar sin tarjeta | `cliente_id` obligatorio en inscripción; la tarjeta no interviene |
| Ticket único y con al menos una línea | `UNIQUE(referencia_ticket)`; "al menos una línea" **pendiente** en BD |
| Orden de línea no repetido | `UNIQUE (venta_id, numero_orden)` |
| Precio antiguo no se recalcula | `linea_venta.precio_unitario` e `importe` guardados |
| Venta anónima | `venta.cliente_id NULL` |
| Uno o varios cobros por venta | tabla `cobro_tienda` con varias filas |
| Confirmar pago exige suma exacta | **Pendiente en BD**: `Venta.confirmarPagada` |
| Devolución ligada a línea pagada | FK + `GestionDevoluciones` |
| No devolver más de lo comprado (cantidad e importe) | **Pendiente** con concurrencia: validada en `GestionDevoluciones` |
| Importe de devolución con precio original | `calcularImporteConPrecio` en Java; columna guardada |
| Mercancía apta o no para reventa | `apta_para_reventa` |
| Incidencia sin/asignada a un empleado | `empleado_responsable_id NULL` + FK |

## 10. Tratamiento propuesto para las reglas pendientes en la fase JDBC

1. **Altas conjuntas** (estación+ficha+surtidor, proveedor+acuerdo,
   producto+acuerdo): una sola `Connection` con `autoCommit=false`, INSERTs en
   orden de dependencia, `commit()` sólo si todas las validaciones pasan,
   `rollback()` ante cualquier `SQLException`.
2. **Bajas del último elemento** (surtidor, acuerdo): `SELECT COUNT(*)` dentro
   de la misma transacción con bloqueo (`SELECT ... FOR UPDATE` en MySQL) antes
   de borrar; si el conteo es 1, se rechaza.
3. **Ciclos de supervisión**: recorrer la cadena de `supervisor_id` del candidato
   antes de asignar; comprobar también que no se alcanza el propio empleado.
4. **Periodos solapados**: `SELECT` de la misma `funcion_id` donde el rango
   `[inicio, fin]` se cruza con el nuevo, dentro de la transacción.
5. **Devoluciones acumuladas**: bloquear la línea (`FOR UPDATE`), sumar
   cantidades e importes ya registrados y rechazar si exceden los guardados en
   `linea_venta`. El redondeo de cada devolución se calcula con el precio de la
   línea, nunca con el vigente.
6. **Confirmación de cobro**: sumar `cobro_tienda` en la misma transacción y
   actualizar `confirmada_pagada` sólo si la suma equivale al importe de la
   venta; si no, `rollback()`.
7. **Cantidades y modalidades en SQL**: añadir `CHECK` por modalidad sería
   engañoso con `DECIMAL`; se propone un trigger `BEFORE INSERT` en `linea_venta`
   y `devolucion` que consulte `producto_granel` y valide enteros/precisión antes
   de aceptar la fila.
