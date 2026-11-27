-- ============================================================================
-- 04_comprobaciones.sql: se ejecuta DESPUÉS de 01, 02 y 03.
-- Cumplimentar con el cliente mysql (mysql -u ... -p < 04_comprobaciones.sql)
-- porque usa DELIMITER y GET DIAGNOSTICS.
--
-- Los intentos inválidos se agrupan en procedimientos con CONTINUE HANDLER:
-- un error esperado NO interrumpe el resto. Las reglas pendientes se marcan
-- explícitamente como ACEPTADO EN BD y se deshacen con ROLLBACK; no se finge
-- que la base de datos las rechaza.
-- ============================================================================

USE gasolinera_fase1;

-- ----------------------------------------------------------------------------
-- A) COMPROBACIONES CORRECTAS
-- ----------------------------------------------------------------------------

-- Esperado: 5 filas, cada estación con una ficha distinta.
SELECT e.codigo_interno, e.nombre_comercial, c.codigo_configuracion,
       c.serie_tickets, c.siguiente_numero
FROM estacion e
JOIN configuracion_facturacion c ON c.id = e.configuracion_id
ORDER BY e.codigo_interno;

-- Esperado: EST-001 tiene 3 equipos; el resto al menos 1; el nº 1 se repite.
SELECT e.codigo_interno, COUNT(s.id) AS num_surtidores
FROM estacion e
LEFT JOIN surtidor s ON s.estacion_id = e.id
GROUP BY e.id, e.codigo_interno
ORDER BY e.codigo_interno;

SELECT numero_visible, COUNT(DISTINCT estacion_id) AS estaciones
FROM surtidor
GROUP BY numero_visible
HAVING COUNT(DISTINCT estacion_id) > 1
ORDER BY numero_visible;

-- Esperado: SUR-003 averiado sigue en EST-001.
SELECT s.codigo_equipo, e.codigo_interno, ea.nombre AS estado
FROM surtidor s
JOIN estacion e ON e.id = s.estacion_id
JOIN catalogo_estado_servicio ea ON ea.id = s.estado_servicio_id
WHERE ea.codigo = 'AVERIADO';

-- Esperado: EMP-001 sin supervisor y supervisa a EMP-002, EMP-003 y EMP-005;
-- EMP-004 sin supervisor y sin funciones.
SELECT e.numero_interno, e.nombre, COALESCE(s.numero_interno, 'sin supervisor') AS supervisor,
       COUNT(af.id) AS periodos
FROM empleado e
LEFT JOIN empleado s ON s.id = e.supervisor_id
LEFT JOIN asignacion_funcion af ON af.empleado_id = e.id
GROUP BY e.id, e.numero_interno, e.nombre, s.numero_interno
ORDER BY e.numero_interno;

-- Esperado: EMP-001 con CAJERO + RESPONSABLE_TIENDA + JEFE; EMP-003 con dos
-- periodos de CAJERO (uno cerrado y otro vigente).
SELECT e.numero_interno, cf.codigo, af.fecha_inicio,
       COALESCE(CAST(af.fecha_fin AS CHAR), 'vigente') AS fecha_fin
FROM asignacion_funcion af
JOIN empleado e ON e.id = af.empleado_id
JOIN catalogo_funcion cf ON cf.id = af.funcion_id
ORDER BY e.numero_interno, af.fecha_inicio;

-- Esperado: dos tarjetas sin titular; CLI-002 y CLI-004 sin tarjeta.
SELECT t.numero_tarjeta, t.fecha_emision,
       COALESCE(CAST(t.fecha_asignacion AS CHAR), 'sin asignar') AS fecha_asignacion,
       COALESCE(c.codigo_cliente, 'sin titular') AS cliente
FROM tarjeta_fidelizacion t
LEFT JOIN cliente_tienda c ON c.id = t.cliente_id
ORDER BY t.numero_tarjeta;

-- Esperado: unidades completas y granel; importes redondeados a 2 decimales.
SELECT p.referencia_comercial, mp.codigo AS modalidad,
       l.cantidad, l.precio_unitario,
       ROUND(l.cantidad * l.precio_unitario, 2) AS importe_calculado, l.importe AS importe_guardado
FROM linea_venta l
JOIN producto p ON p.id = l.producto_id
JOIN catalogo_modalidad_producto mp ON mp.id = p.modalidad_id
ORDER BY l.id;

-- Esperado: PROV-001 con 2 productos; PROD-001 con 2 proveedores; sin aislados.
SELECT p.codigo_interno, COUNT(a.id) AS productos
FROM proveedor p LEFT JOIN acuerdo_suministro a ON a.proveedor_id = p.id
GROUP BY p.id, p.codigo_interno ORDER BY p.codigo_interno;

SELECT p.referencia_comercial, COUNT(a.id) AS proveedores
FROM producto p LEFT JOIN acuerdo_suministro a ON a.producto_id = p.id
GROUP BY p.id, p.referencia_comercial ORDER BY p.referencia_comercial;

-- Esperado: CAM-002 sin inscritos; CLI-004 y CLI-005 sin campañas.
SELECT c.codigo, COUNT(i.id) AS inscritos
FROM campana c LEFT JOIN inscripcion_campana i ON i.campana_id = c.id
GROUP BY c.id, c.codigo ORDER BY c.codigo;

-- Esperado: INC-001 e INC-004 sin responsable; EMP-001 y EMP-005 con varias.
SELECT i.referencia, e.numero_interno AS responsable, ei.codigo AS estado
FROM incidencia i
LEFT JOIN empleado e ON e.id = i.empleado_responsable_id
JOIN catalogo_estado_incidencia ei ON ei.id = i.estado_id
ORDER BY i.referencia;

-- Esperado: A000101 tiene dos cobros; A000104 está PARCIAL (no confirmada).
SELECT v.referencia_ticket, v.confirmada_pagada,
       (SELECT SUM(l.importe) FROM linea_venta l WHERE l.venta_id = v.id) AS importe,
       (SELECT COALESCE(SUM(c.importe), 0) FROM cobro_tienda c WHERE c.venta_id = v.id) AS cobrado,
       CASE WHEN v.confirmada_pagada THEN 'PAGADA'
            WHEN (SELECT COALESCE(SUM(c.importe), 0) FROM cobro_tienda c WHERE c.venta_id = v.id) = 0
                 THEN 'PENDIENTE'
            ELSE 'PARCIAL' END AS estado
FROM venta v
ORDER BY v.referencia_ticket;

-- Esperado: línea 2 devuelta 0,750 de 1,500 kg y 9,00 de 18,00 €; línea intacta.
SELECT l.id, l.cantidad AS comprada, COALESCE(SUM(d.cantidad), 0) AS devuelta,
       l.importe AS importe_original, COALESCE(SUM(d.importe_reintegrado), 0) AS reintegrado
FROM linea_venta l LEFT JOIN devolucion d ON d.linea_venta_id = l.id
WHERE l.id IN (2, 5)
GROUP BY l.id, l.cantidad, l.importe
ORDER BY l.id;

-- Esperado: 0 filas. El trigger NO dispara en INSERT (alta inicial).
SELECT COUNT(*) AS historial_inicial FROM historial_precios;

-- ----------------------------------------------------------------------------
-- B) INTENTOS DELIBERADAMENTE INVÁLIDOS: todos deben decir RECHAZADO
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS probar_restricciones_generales;
DELIMITER //
CREATE PROCEDURE probar_restricciones_generales()
BEGIN
    DECLARE v_config_temp INT;
    DECLARE v_mensaje VARCHAR(255);
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensaje = MESSAGE_TEXT;
        SELECT 'RECHAZADO (esperado)' AS resultado, v_mensaje AS detalle;
    END;

    INSERT INTO configuracion_facturacion
        (codigo_configuracion, serie_tickets, siguiente_numero, texto_pie)
        VALUES ('CF-TEMP', 'T', 1, 'temporal');
    SET v_config_temp = LAST_INSERT_ID();

    SELECT '1. código de estación duplicado' AS prueba;
    INSERT INTO estacion (codigo_interno, nombre_comercial, direccion, telefono, fecha_apertura, configuracion_id)
        VALUES ('EST-001', 'Duplicada', 'Calle 1', '900000000', '2024-01-01', v_config_temp);

    SELECT '2. código de ficha duplicado' AS prueba;
    INSERT INTO configuracion_facturacion (codigo_configuracion, serie_tickets, siguiente_numero, texto_pie)
        VALUES ('CF-001', 'X', 1, 'duplicada');

    SELECT '3. número visible repetido en EST-001' AS prueba;
    INSERT INTO surtidor (estacion_id, codigo_equipo, numero_visible, tipo_combustible_id, estado_servicio_id)
        VALUES (1, 'SUR-TEMP1', 1, 1, 1);

    SELECT '4. código de equipo duplicado en la empresa' AS prueba;
    INSERT INTO surtidor (estacion_id, codigo_equipo, numero_visible, tipo_combustible_id, estado_servicio_id)
        VALUES (2, 'SUR-001', 8, 1, 1);

    SELECT '5. surtidor con estación inexistente' AS prueba;
    INSERT INTO surtidor (estacion_id, codigo_equipo, numero_visible, tipo_combustible_id, estado_servicio_id)
        VALUES (999, 'SUR-TEMP2', 1, 1, 1);

    SELECT '6. empleado con estación inexistente' AS prueba;
    INSERT INTO empleado (numero_interno, documento_identidad, nombre, telefono, fecha_incorporacion, estacion_id)
        VALUES ('EMP-TEMP', '99999999Z', 'Temporal', '900000000', '2024-01-01', 999);

    SELECT '7. tarjeta con cliente inexistente' AS prueba;
    INSERT INTO tarjeta_fidelizacion (numero_tarjeta, fecha_emision, fecha_asignacion, cliente_id)
        VALUES ('TAR-TEMP', '2024-01-01', '2024-01-02', 999);

    SELECT '8. número de tarjeta duplicado' AS prueba;
    INSERT INTO tarjeta_fidelizacion (numero_tarjeta, fecha_emision)
        VALUES ('TAR-001', '2024-02-01');

    SELECT '9. código de cliente duplicado' AS prueba;
    INSERT INTO cliente_tienda (codigo_cliente, nombre, telefono)
        VALUES ('CLI-001', 'Duplicado', '900000001');

    SELECT '10. acuerdo proveedor-producto duplicado' AS prueba;
    INSERT INTO acuerdo_suministro (proveedor_id, producto_id, referencia_proveedor, precio_compra, plazo_entrega_dias)
        VALUES (1, 1, 'OTRA-REF', 0.80, 1);

    SELECT '11. inscripción duplicada' AS prueba;
    INSERT INTO inscripcion_campana (campana_id, cliente_id, fecha_inscripcion)
        VALUES (1, 1, '2026-07-01');

    SELECT '12. referencia de ticket duplicada' AS prueba;
    INSERT INTO venta (referencia_ticket, fecha_hora, estacion_id, empleado_id)
        VALUES ('A000101', '2026-11-25 12:00:00', 1, 1);

    SELECT '13. número de orden duplicado en un ticket' AS prueba;
    INSERT INTO linea_venta (venta_id, producto_id, numero_orden, cantidad, precio_unitario, importe)
        VALUES (1, 1, 1, 1.000, 1.20, 1.20);

    SELECT '14. línea con producto inexistente' AS prueba;
    INSERT INTO linea_venta (venta_id, producto_id, numero_orden, cantidad, precio_unitario, importe)
        VALUES (1, 999, 9, 1.000, 1.20, 1.20);

    SELECT '15. borrar producto con líneas de venta' AS prueba;
    DELETE FROM producto WHERE id = 1;

    SELECT '16. cambiar la modalidad de un producto' AS prueba;
    UPDATE producto SET modalidad_id = 2 WHERE id = 1;

    DELETE FROM configuracion_facturacion WHERE codigo_configuracion = 'CF-TEMP';
END//
DELIMITER ;

CALL probar_restricciones_generales();
DROP PROCEDURE probar_restricciones_generales;

-- ----------------------------------------------------------------------------
-- C) REGLAS PENDIENTES EN BD: se ACEPTAN y se deshacen con ROLLBACK.
-- La fase JDBC deberá comprobarlas con transacciones/bloqueos; aquí NO se
-- presentan como rechazos automáticos.
-- ----------------------------------------------------------------------------

START TRANSACTION;
DELETE FROM surtidor WHERE estacion_id = 2;
SELECT 'ACEPTADO EN BD (pendiente): dejar EST-002 sin surtidores' AS aviso;
ROLLBACK;

START TRANSACTION;
UPDATE empleado SET supervisor_id = 2 WHERE id = 1;
SELECT 'ACEPTADO EN BD (pendiente): ciclo indirecto EMP-001 -> EMP-002 -> EMP-001' AS aviso;
ROLLBACK;

START TRANSACTION;
INSERT INTO asignacion_funcion (empleado_id, funcion_id, fecha_inicio, fecha_fin)
    VALUES (1, 1, '2026-01-01', NULL);
SELECT 'ACEPTADO EN BD (pendiente): dos periodos CAJERO solapados para EMP-001' AS aviso;
ROLLBACK;

START TRANSACTION;
INSERT INTO devolucion (linea_venta_id, cantidad, motivo, fecha_hora, empleado_id,
                        importe_reintegrado, apta_para_reventa)
    VALUES (2, 2.000, 'Exceso deliberado', '2026-11-25 13:00:00', 1, 24.00, FALSE);
SELECT 'ACEPTADO EN BD (pendiente): devolución acumulada superior a la compra' AS aviso;
ROLLBACK;

START TRANSACTION;
INSERT INTO proveedor (codigo_interno, identificacion_fiscal, razon_social, telefono)
    VALUES ('PROV-TEMP', 'B99999999', 'Sin acuerdo', '900000009');
SELECT 'ACEPTADO EN BD (pendiente): proveedor aislado' AS aviso;
ROLLBACK;

START TRANSACTION;
DELETE FROM acuerdo_suministro WHERE id = 2;
SELECT 'ACEPTADO EN BD (pendiente): quitar el último acuerdo de PROD-002' AS aviso;
ROLLBACK;

-- ----------------------------------------------------------------------------
-- D) TRIGGER OBLIGATORIO DE PRECIOS
-- ----------------------------------------------------------------------------

-- Esperado: cambio real -> 1 fila (anterior 1,20 / nuevo 1,35).
UPDATE producto SET precio_venta = 1.35 WHERE id = 1;
SELECT id, producto_id, precio_anterior, precio_nuevo FROM historial_precios ORDER BY id;

-- Esperado: guardar el mismo precio -> NO añade fila (sigue 1).
UPDATE producto SET precio_venta = 1.35 WHERE id = 1;
SELECT 'tras guardar el mismo precio' AS prueba, COUNT(*) AS filas FROM historial_precios;

-- Esperado: cambiar sólo la descripción -> NO añade fila (sigue 1).
UPDATE producto SET descripcion = 'Agua 1 L (etiqueta nueva)' WHERE id = 1;
SELECT 'tras cambiar descripción' AS prueba, COUNT(*) AS filas FROM historial_precios;

-- Esperado: el ticket antiguo sigue a 1,20 aunque el catálogo esté a 1,35.
SELECT referencia_ticket, numero_orden, precio_unitario AS precio_cobrado
FROM linea_venta l JOIN venta v ON v.id = l.venta_id
WHERE v.referencia_ticket = 'A000101' AND l.numero_orden = 1;

-- Esperado DENTRO de la transacción: precio 1,45 y una fila nueva de historial.
START TRANSACTION;
UPDATE producto SET precio_venta = 1.45 WHERE id = 2;
SELECT 'dentro de la transacción' AS prueba, precio_venta FROM producto WHERE id = 2;
SELECT COUNT(*) AS filas_dentro FROM historial_precios;
ROLLBACK;

-- Esperado DESPUÉS del rollback: precio 12,00 e historial sigue con 1 fila.
SELECT 'tras rollback' AS prueba, precio_venta FROM producto WHERE id = 2;
SELECT 'tras rollback' AS prueba, COUNT(*) AS filas FROM historial_precios;

-- Se devuelve PROD-001 a 1,20: es un cambio real, así que añade una segunda fila.
UPDATE producto SET precio_venta = 1.20 WHERE id = 1;
SELECT 'precio restaurado' AS prueba, COUNT(*) AS filas FROM historial_precios;

-- El historial no se puede editar ni borrar con operaciones ordinarias.
DROP PROCEDURE IF EXISTS probar_historial_no_editable;
DELIMITER //
CREATE PROCEDURE probar_historial_no_editable()
BEGIN
    DECLARE v_mensaje VARCHAR(255);
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 v_mensaje = MESSAGE_TEXT;
        SELECT 'RECHAZADO (esperado)' AS resultado, v_mensaje AS detalle;
    END;
    SELECT '17. editar una entrada del historial' AS prueba;
    UPDATE historial_precios SET precio_nuevo = 99.00 WHERE id = 1;
    SELECT '18. borrar una entrada del historial' AS prueba;
    DELETE FROM historial_precios WHERE id = 1;
END//
DELIMITER ;

CALL probar_historial_no_editable();
DROP PROCEDURE probar_historial_no_editable;

-- ----------------------------------------------------------------------------
-- E) ESTADO FINAL: datos originales intactos y 2 entradas de historial.
-- ----------------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM estacion)              AS estaciones,
       (SELECT COUNT(*) FROM configuracion_facturacion) AS fichas,
       (SELECT COUNT(*) FROM surtidor)              AS surtidores,
       (SELECT COUNT(*) FROM empleado)              AS empleados,
       (SELECT COUNT(*) FROM cliente_tienda)        AS clientes,
       (SELECT COUNT(*) FROM producto)              AS productos,
       (SELECT COUNT(*) FROM venta)                 AS ventas,
       (SELECT COUNT(*) FROM linea_venta)           AS lineas,
       (SELECT COUNT(*) FROM historial_precios)     AS historial_precios;
