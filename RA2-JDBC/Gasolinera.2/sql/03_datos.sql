-- ============================================================================
-- 03_datos.sql: datos de ejemplo. Se ejecuta DESPUÉS de 01 y 02.
-- Todas las altas conjuntas (ficha+estación, empleado+funciones, etc.) van en
-- una sola transacción: si falla cualquier INSERT, no queda una alta a medias.
-- No se desactivan claves foráneas.
-- ============================================================================

USE gasolinera_fase1;

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- Estaciones: cinco fichas, cinco estaciones, nueve surtidores.
-- EST-001 tiene tres equipos; el nº visible 1 se repite en estaciones distintas.
-- SUR-003 está averiado y sigue perteneciendo a su estación.
-- ----------------------------------------------------------------------------
INSERT INTO configuracion_facturacion (id, codigo_configuracion, serie_tickets, siguiente_numero, texto_pie) VALUES
    (1, 'CF-001', 'A', 101,  'Gracias por visitarnos'),
    (2, 'CF-002', 'N', 204,  'Visite también nuestro taller'),
    (3, 'CF-003', 'P', 1,    'Puerto'),
    (4, 'CF-004', 'S', 50,   'Atención al cliente 900 000 004'),
    (5, 'CF-005', 'E', 3001, 'Gasolinera Este');

INSERT INTO estacion (id, codigo_interno, nombre_comercial, direccion, telefono, fecha_apertura, configuracion_id) VALUES
    (1, 'EST-001', 'Gasolinera Centro', 'Av. Principal 12', '910111213', '2020-03-15', 1),
    (2, 'EST-002', 'Gasolinera Norte',  'Calle Norte 8',    '910222222', '2021-06-01', 2),
    (3, 'EST-003', 'Gasolinera Puerto', 'Puerto 3',         '910333333', '2022-01-20', 3),
    (4, 'EST-004', 'Gasolinera Sur',    'Ctra. Sur 45',     '910444444', '2022-09-05', 4),
    (5, 'EST-005', 'Gasolinera Este',   'Av. Este 7',       '910555555', '2023-02-14', 5);

INSERT INTO catalogo_tipo_combustible (id, codigo, nombre) VALUES
    (1, 'DIESEL',      'Gasóleo A'),
    (2, 'GASOLINA_95', 'Gasolina 95'),
    (3, 'GASOLINA_98', 'Gasolina 98');

INSERT INTO catalogo_estado_servicio (id, codigo, nombre) VALUES
    (1, 'OPERATIVO',     'En servicio'),
    (2, 'AVERIADO',      'Averiado temporalmente'),
    (3, 'MANTENIMIENTO', 'En mantenimiento');

INSERT INTO surtidor (id, estacion_id, codigo_equipo, numero_visible, tipo_combustible_id, estado_servicio_id) VALUES
    (1, 1, 'SUR-001', 1, 1, 1),
    (2, 1, 'SUR-002', 2, 2, 1),
    (3, 1, 'SUR-003', 3, 3, 2),
    (4, 2, 'SUR-004', 1, 1, 1),
    (5, 2, 'SUR-005', 2, 1, 3),
    (6, 3, 'SUR-006', 1, 2, 1),
    (7, 4, 'SUR-007', 1, 2, 1),
    (8, 4, 'SUR-008', 2, 1, 1),
    (9, 5, 'SUR-009', 1, 3, 1);

-- ----------------------------------------------------------------------------
-- Personal: EMP-001 sin supervisor y con tres funciones; EMP-002 y EMP-003
-- supervisados por EMP-001; EMP-004 sin supervisor y sin funciones; EMP-005
-- jefa y cajera a la vez; EMP-003 recuperó CAJERO tras un periodo cerrado.
-- ----------------------------------------------------------------------------
INSERT INTO empleado (id, numero_interno, documento_identidad, nombre, telefono, email,
                      fecha_incorporacion, estacion_id, supervisor_id) VALUES
    (1, 'EMP-001', '11111111A', 'Ana García',  '910000001', NULL,                  '2019-02-01', 1, NULL),
    (2, 'EMP-002', '22222222B', 'Bruno López', NULL,        'bruno@example.com',   '2023-04-10', 1, 1),
    (3, 'EMP-003', '33333333C', 'Carla Ruiz',  '910000003', 'carla@example.com',   '2024-01-08', 1, 1),
    (4, 'EMP-004', '44444444D', 'Dani Soto',   '910000004', NULL,                  '2026-10-01', 2, NULL),
    (5, 'EMP-005', '55555555E', 'Elena Vidal', '910000005', 'elena@example.com',   '2021-07-12', 2, 1);

INSERT INTO catalogo_funcion (id, codigo, nombre) VALUES
    (1, 'CAJERO',             'Cajero'),
    (2, 'EXPENDEDOR',         'Expendedor'),
    (3, 'RESPONSABLE_TIENDA', 'Responsable de tienda'),
    (4, 'JEFE_ESTACION',      'Jefe de estación');

INSERT INTO asignacion_funcion (id, empleado_id, funcion_id, fecha_inicio, fecha_fin) VALUES
    (1, 1, 1, '2020-01-01', NULL),
    (2, 1, 3, '2022-05-01', NULL),
    (3, 2, 2, '2023-04-10', NULL),
    (4, 3, 1, '2024-01-08', '2024-09-30'),
    (5, 3, 1, '2025-02-01', NULL),
    (6, 5, 4, '2025-01-01', NULL),
    (7, 5, 1, '2025-06-01', NULL),
    (8, 1, 4, '2026-10-01', NULL);

INSERT INTO funcion_cajero (id, asignacion_funcion_id, limite_devoluciones) VALUES
    (1, 1, 200.00),
    (2, 4, 100.00),
    (3, 5, 300.00),
    (4, 7, 150.00);

INSERT INTO funcion_expendedor (id, asignacion_funcion_id, numero_acreditacion, caducidad) VALUES
    (1, 3, 'ACR-7788', '2027-12-31');

INSERT INTO funcion_responsable_tienda (id, asignacion_funcion_id, presupuesto_compras) VALUES
    (1, 2, 1000.00);

INSERT INTO funcion_jefe_estacion (id, asignacion_funcion_id, nivel_autorizacion) VALUES
    (1, 6, 2),
    (2, 8, 3);

-- ----------------------------------------------------------------------------
-- Clientes y tarjetas. CLI-002 y CLI-004 no tienen tarjeta; TAR-002 y TAR-005
-- están registradas pero todavía sin titular.
-- ----------------------------------------------------------------------------
INSERT INTO cliente_tienda (id, codigo_cliente, nombre, telefono, email) VALUES
    (1, 'CLI-001', 'Lucía', '910111111', NULL),
    (2, 'CLI-002', 'Marcos', NULL,       'marcos@example.com'),
    (3, 'CLI-003', 'Nuria',  '910333333', 'nuria@example.com'),
    (4, 'CLI-004', 'Óscar',  '910444444', NULL),
    (5, 'CLI-005', 'Paula',  NULL,        'paula@example.com');

INSERT INTO tarjeta_fidelizacion (id, numero_tarjeta, fecha_emision, fecha_asignacion, cliente_id) VALUES
    (1, 'TAR-001', '2024-01-05', '2024-01-20', 1),
    (2, 'TAR-002', '2025-03-01', NULL,         NULL),
    (3, 'TAR-003', '2025-06-15', '2025-07-01', 3),
    (4, 'TAR-004', '2025-08-01', '2025-08-10', 5),
    (5, 'TAR-005', '2026-01-01', NULL,         NULL);

-- ----------------------------------------------------------------------------
-- Catálogo: tres productos envasados y dos a granel. PROD-005 está retirado
-- pero sigue identificable en la venta N000204.
-- El historial de precios se deja vacío: 04_comprobaciones.sql lo genera.
-- ----------------------------------------------------------------------------
INSERT INTO catalogo_estado_comercializacion (id, codigo, nombre) VALUES
    (1, 'ACTIVO',   'En comercialización'),
    (2, 'RETIRADO', 'Retirado');

INSERT INTO catalogo_modalidad_producto (id, codigo, nombre) VALUES
    (1, 'ENVASADO', 'Venta por unidades completas'),
    (2, 'GRANEL',   'Venta por kilogramos');

INSERT INTO producto (id, referencia_comercial, descripcion, precio_venta, estado_id, modalidad_id) VALUES
    (1, 'PROD-001', 'Agua 1 L',              1.20, 1, 1),
    (2, 'PROD-002', 'Almendras a granel',   12.00, 1, 2),
    (3, 'PROD-003', 'Café en grano 1 kg',    8.50, 1, 1),
    (4, 'PROD-004', 'Anacardos a granel',   18.00, 1, 2),
    (5, 'PROD-005', 'Zumo retirado',         2.00, 2, 1);

INSERT INTO producto_envasado (id, producto_id, formato_envase) VALUES
    (1, 1, 'Botella 1 L'),
    (2, 3, 'Paquete 1 kg'),
    (3, 5, 'Brik 1 L');

INSERT INTO producto_granel (id, producto_id, precision_pesada) VALUES
    (1, 2, 0.050),
    (2, 4, 0.100);

-- ----------------------------------------------------------------------------
-- Proveedores: PROV-001 suministra dos productos; PROD-001 tiene dos
-- proveedores. Todos tienen al menos un acuerdo y ningún producto está aislado.
-- ----------------------------------------------------------------------------
INSERT INTO proveedor (id, codigo_interno, identificacion_fiscal, razon_social, telefono, email) VALUES
    (1, 'PROV-001', 'B12345678', 'Distribuciones Norte', '910100001', NULL),
    (2, 'PROV-002', 'B87654321', 'Alimentos Sur',        NULL,        'ventas@alsur.example'),
    (3, 'PROV-003', 'B11223344', 'Bebidas Este',         '910100003', NULL),
    (4, 'PROV-004', 'B44332211', 'Frutos Secos SL',      '910100004', NULL),
    (5, 'PROV-005', 'B55667788', 'Cafés Oeste',          '910100005', NULL);

INSERT INTO acuerdo_suministro (id, proveedor_id, producto_id, referencia_proveedor, precio_compra, plazo_entrega_dias) VALUES
    (1, 1, 1, 'AG-1L', 0.70, 2),
    (2, 1, 2, 'AL-GR', 9.00, 4),
    (3, 2, 1, 'H2O-N', 0.75, 3),
    (4, 3, 5, 'ZU-1L', 1.20, 5),
    (5, 4, 4, 'AN-GR', 14.00, 6),
    (6, 5, 3, 'CA-1K', 6.10, 7);

-- ----------------------------------------------------------------------------
-- Campañas: CAM-002 no tiene inscritos; CLI-004 y CLI-005 no están inscritos.
-- CLI-001, CLI-002 y CLI-003 participan en varias campañas (ambos sentidos).
-- ----------------------------------------------------------------------------
INSERT INTO campana (id, codigo, nombre, descripcion, fecha_inicio, fecha_fin) VALUES
    (1, 'CAM-001', 'Verano',              'Descuentos de verano',        '2026-06-01', '2026-08-31'),
    (2, 'CAM-002', 'Invierno',            'Sin inscritos todavía',       '2026-12-01', '2027-01-15'),
    (3, 'CAM-003', 'Clientes frecuentes', 'Puntos extra',                '2026-09-01', '2026-12-31');

INSERT INTO inscripcion_campana (id, campana_id, cliente_id, fecha_inscripcion) VALUES
    (1, 1, 1, '2026-06-03'),
    (2, 1, 3, '2026-06-05'),
    (3, 3, 1, '2026-09-02'),
    (4, 3, 2, '2026-09-04'),
    (5, 3, 3, '2026-09-06');

-- ----------------------------------------------------------------------------
-- Ventas: cinco tickets, doce líneas. A000101 se paga en dos cobros; A000104
-- queda PARCIAL (no confirmada) para mostrar el estado intermedio.
-- El precio de cada línea es el cobrado, no el vigente en el catálogo.
-- ----------------------------------------------------------------------------
INSERT INTO catalogo_medio_pago (id, codigo, nombre) VALUES
    (1, 'EFECTIVO',      'Efectivo'),
    (2, 'TARJETA',       'Tarjeta'),
    (3, 'TRANSFERENCIA', 'Transferencia');

INSERT INTO venta (id, referencia_ticket, fecha_hora, estacion_id, empleado_id, cliente_id, confirmada_pagada) VALUES
    (1, 'A000101', '2026-11-25 10:30:00', 1, 2, NULL, TRUE),
    (2, 'A000102', '2026-11-25 10:40:00', 2, 1, 1,   TRUE),
    (3, 'N000204', '2026-11-25 10:50:00', 3, 3, NULL, TRUE),
    (4, 'A000103', '2026-11-25 11:05:00', 1, 5, 3,   TRUE),
    (5, 'A000104', '2026-11-25 11:20:00', 2, 4, NULL, FALSE);

INSERT INTO linea_venta (id, venta_id, producto_id, numero_orden, cantidad, precio_unitario, importe) VALUES
    (1,  1, 1, 1, 3.000, 1.20,  3.60),
    (2,  1, 2, 2, 1.500, 12.00, 18.00),
    (3,  2, 3, 1, 2.000, 8.50,  17.00),
    (4,  2, 4, 2, 0.300, 18.00,  5.40),
    (5,  2, 1, 3, 1.000, 1.20,   1.20),
    (6,  3, 5, 1, 2.000, 2.00,   4.00),
    (7,  3, 1, 2, 4.000, 1.20,   4.80),
    (8,  4, 2, 1, 0.500, 12.00,  6.00),
    (9,  4, 3, 2, 1.000, 8.50,   8.50),
    (10, 4, 4, 3, 0.200, 18.00,  3.60),
    (11, 5, 1, 1, 2.000, 1.20,   2.40),
    (12, 5, 4, 2, 0.100, 18.00,  1.80);

INSERT INTO cobro_tienda (id, venta_id, importe, fecha_hora, medio_pago_id) VALUES
    (1, 1, 10.00, '2026-11-25 10:32:00', 1),
    (2, 1, 11.60, '2026-11-25 10:33:00', 2),
    (3, 2, 23.60, '2026-11-25 10:42:00', 2),
    (4, 3,  8.80, '2026-11-25 10:51:00', 1),
    (5, 4, 18.10, '2026-11-25 11:07:00', 2),
    (6, 5,  2.00, '2026-11-25 11:22:00', 1);

-- ----------------------------------------------------------------------------
-- Devoluciones: dos parciales sucesivas de la línea 2 (0,50 + 0,25 de 1,50 kg)
-- y una tercera de otra línea. Importes calculados con el precio original.
-- ----------------------------------------------------------------------------
INSERT INTO devolucion (id, linea_venta_id, cantidad, motivo, fecha_hora, empleado_id,
                        importe_reintegrado, apta_para_reventa) VALUES
    (1, 2, 0.500, 'Producto dañado',       '2026-11-25 11:40:00', 1, 6.00, FALSE),
    (2, 2, 0.250, 'Error de pesada',       '2026-11-25 12:00:00', 1, 3.00, TRUE),
    (3, 5, 0.100, 'Cliente se arrepintió', '2026-11-25 11:00:00', 1, 1.80, TRUE);

-- ----------------------------------------------------------------------------
-- Incidencias: INC-001 e INC-004 sin responsable; EMP-004 no responsable de
-- ninguna; EMP-001 y EMP-005 tienen varias.
-- ----------------------------------------------------------------------------
INSERT INTO catalogo_estado_incidencia (id, codigo, nombre) VALUES
    (1, 'ABIERTA',   'Abierta'),
    (2, 'EN_CURSO',  'En curso'),
    (3, 'RESUELTA',  'Resuelta');

INSERT INTO incidencia (id, referencia, descripcion, fecha_apertura, estacion_id, estado_id,
                        empleado_responsable_id) VALUES
    (1, 'INC-001', 'Surtidor averiado',   '2026-11-25 08:00:00', 1, 1, NULL),
    (2, 'INC-002', 'Caja sin cambio',     '2026-11-25 09:00:00', 1, 2, 1),
    (3, 'INC-003', 'Tienda sin etiquetas','2026-11-25 09:30:00', 1, 2, 1),
    (4, 'INC-004', 'Puerta atascada',     '2026-11-25 10:00:00', 2, 1, NULL),
    (5, 'INC-005', 'Limpieza pendiente',  '2026-11-25 10:15:00', 3, 2, 5);

COMMIT;
