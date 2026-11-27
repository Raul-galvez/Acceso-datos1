-- ============================================================================
-- 02_triggers.sql: se ejecuta DESPUÉS de 01_esquema.sql y ANTES de 03_datos.sql.
--
-- Trigger OBLIGATORIO: trg_producto_historial_precios.
-- Funciona también si se cambia el precio con una herramienta SQL, porque está
-- en MySQL y no en Java. Con InnoDB, la fila del historial se inserta en la misma
-- transacción: si se hace ROLLBACK, desaparece junto con el cambio de precio.
--
-- Los otros tres triggers son refuerzos opcionales del caso: modalidad estable
-- e historial no editable desde operaciones ordinarias. No sustituyen al
-- trigger de precios.
-- ============================================================================

USE gasolinera_fase1;

-- ----------------------------------------------------------------------------
-- OBLIGATORIO: registro automático de cambios REALES de precio.
-- INSERT no dispara este trigger (AFTER UPDATE): alta inicial sin historial.
-- UPDATE que no toca precio no inserta (IF <> OLD).
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_producto_historial_precios;
DELIMITER //
CREATE TRIGGER trg_producto_historial_precios
AFTER UPDATE ON producto
FOR EACH ROW
BEGIN
    IF NEW.precio_venta <> OLD.precio_venta THEN
        INSERT INTO historial_precios
            (producto_id, precio_anterior, precio_nuevo, fecha_cambio)
        VALUES
            (NEW.id, OLD.precio_venta, NEW.precio_venta, CURRENT_TIMESTAMP(6));
    END IF;
END//
DELIMITER ;

-- ----------------------------------------------------------------------------
-- REFUERZO: un producto mantiene su modalidad durante toda su comercialización.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_producto_modalidad_inmutable;
DELIMITER //
CREATE TRIGGER trg_producto_modalidad_inmutable
BEFORE UPDATE ON producto
FOR EACH ROW
BEGIN
    IF NEW.modalidad_id <> OLD.modalidad_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La modalidad de un producto no se puede cambiar.';
    END IF;
END//
DELIMITER ;

-- ----------------------------------------------------------------------------
-- REFUERZO: la aplicación no ofrecerá editar/borrar el historial; la BD lo impide.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_historial_no_editar;
DELIMITER //
CREATE TRIGGER trg_historial_no_editar
BEFORE UPDATE ON historial_precios
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El historial de precios no se puede editar.';
END//
DELIMITER ;

DROP TRIGGER IF EXISTS trg_historial_no_borrar;
DELIMITER //
CREATE TRIGGER trg_historial_no_borrar
BEFORE DELETE ON historial_precios
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El historial de precios no se puede borrar.';
END//
DELIMITER ;
