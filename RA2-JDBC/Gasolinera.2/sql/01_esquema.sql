-- ============================================================================
-- Fase 1 - Caso práctico de la gasolinera con MySQL
-- 01_esquema.sql: base de datos, tablas, claves y restricciones.
-- Motor: MySQL 8.x / InnoDB / utf8mb4. Todas las PK son INT AUTO_INCREMENT.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS gasolinera_fase1
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gasolinera_fase1;

-- ----------------------------------------------------------------------------
-- ESTACIONES Y SU PUESTA EN SERVICIO
-- La ficha se crea antes: estacion.configuracion_id es NOT NULL y UNIQUE.
-- ----------------------------------------------------------------------------
CREATE TABLE configuracion_facturacion (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    codigo_configuracion  VARCHAR(30)  NOT NULL,
    serie_tickets         VARCHAR(10)  NOT NULL,
    siguiente_numero      INT          NOT NULL,
    texto_pie             VARCHAR(300) NOT NULL,
    CONSTRAINT uq_config_codigo UNIQUE (codigo_configuracion),
    CONSTRAINT chk_config_siguiente CHECK (siguiente_numero > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE estacion (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    codigo_interno   VARCHAR(20)  NOT NULL,
    nombre_comercial VARCHAR(120) NOT NULL,
    direccion        VARCHAR(250) NOT NULL,
    telefono         VARCHAR(30)  NOT NULL,
    fecha_apertura   DATE         NOT NULL,
    configuracion_id INT          NOT NULL,
    CONSTRAINT uq_estacion_codigo UNIQUE (codigo_interno),
    CONSTRAINT uq_estacion_config UNIQUE (configuracion_id),
    CONSTRAINT fk_estacion_config FOREIGN KEY (configuracion_id)
        REFERENCES configuracion_facturacion (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE catalogo_tipo_combustible (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT uq_tipo_combustible_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE catalogo_estado_servicio (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT uq_estado_servicio_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE surtidor (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    estacion_id         INT          NOT NULL,
    codigo_equipo       VARCHAR(30)  NOT NULL,
    numero_visible      INT          NOT NULL,
    tipo_combustible_id INT          NOT NULL,
    estado_servicio_id  INT          NOT NULL,
    CONSTRAINT uq_surtidor_equipo       UNIQUE (codigo_equipo),
    CONSTRAINT uq_surtidor_num_estacion UNIQUE (estacion_id, numero_visible),
    CONSTRAINT chk_surtidor_numero      CHECK (numero_visible > 0),
    CONSTRAINT fk_surtidor_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_surtidor_tipo FOREIGN KEY (tipo_combustible_id)
        REFERENCES catalogo_tipo_combustible (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_surtidor_estado FOREIGN KEY (estado_servicio_id)
        REFERENCES catalogo_estado_servicio (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- PERSONAL Y ORGANIZACIÓN DEL TRABAJO
-- La supervisión es autorreferencial; el CHECK impide la autorreferencia directa.
-- Los ciclos indirectos quedan pendientes de control en la fase JDBC.
-- ----------------------------------------------------------------------------
CREATE TABLE empleado (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    numero_interno       VARCHAR(30)  NOT NULL,
    documento_identidad  VARCHAR(30)  NOT NULL,
    nombre               VARCHAR(120) NOT NULL,
    telefono             VARCHAR(30)  NULL,
    email                VARCHAR(160) NULL,
    fecha_incorporacion  DATE         NOT NULL,
    estacion_id          INT          NOT NULL,
    supervisor_id        INT          NULL,
    CONSTRAINT uq_empleado_numero    UNIQUE (numero_interno),
    CONSTRAINT uq_empleado_documento UNIQUE (documento_identidad),
    CONSTRAINT chk_empleado_contacto CHECK (telefono IS NOT NULL OR email IS NOT NULL),
    CONSTRAINT chk_empleado_no_self  CHECK (supervisor_id IS NULL OR supervisor_id <> id),
    CONSTRAINT fk_empleado_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_empleado_supervisor FOREIGN KEY (supervisor_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE catalogo_funcion (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT uq_funcion_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE asignacion_funcion (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    empleado_id   INT  NOT NULL,
    funcion_id    INT  NOT NULL,
    fecha_inicio  DATE NOT NULL,
    fecha_fin     DATE NULL,
    CONSTRAINT chk_funcion_periodo CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT fk_asignacion_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_asignacion_funcion FOREIGN KEY (funcion_id)
        REFERENCES catalogo_funcion (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Condiciones específicas: tablas 1:1 con la asignación, con PK propia.
CREATE TABLE funcion_cajero (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    asignacion_funcion_id INT           NOT NULL,
    limite_devoluciones   DECIMAL(12,2) NOT NULL,
    CONSTRAINT uq_cajero_asignacion UNIQUE (asignacion_funcion_id),
    CONSTRAINT chk_cajero_limite CHECK (limite_devoluciones > 0),
    CONSTRAINT fk_cajero_asignacion FOREIGN KEY (asignacion_funcion_id)
        REFERENCES asignacion_funcion (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE funcion_expendedor (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    asignacion_funcion_id INT          NOT NULL,
    numero_acreditacion   VARCHAR(40)  NOT NULL,
    caducidad             DATE         NOT NULL,
    CONSTRAINT uq_expendedor_asignacion UNIQUE (asignacion_funcion_id),
    CONSTRAINT uq_expendedor_acreditacion UNIQUE (numero_acreditacion),
    CONSTRAINT fk_expendedor_asignacion FOREIGN KEY (asignacion_funcion_id)
        REFERENCES asignacion_funcion (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE funcion_responsable_tienda (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    asignacion_funcion_id INT           NOT NULL,
    presupuesto_compras   DECIMAL(12,2) NOT NULL,
    CONSTRAINT uq_responsable_asignacion UNIQUE (asignacion_funcion_id),
    CONSTRAINT chk_responsable_presupuesto CHECK (presupuesto_compras > 0),
    CONSTRAINT fk_responsable_asignacion FOREIGN KEY (asignacion_funcion_id)
        REFERENCES asignacion_funcion (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE funcion_jefe_estacion (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    asignacion_funcion_id INT NOT NULL,
    nivel_autorizacion    INT NOT NULL,
    CONSTRAINT uq_jefe_asignacion UNIQUE (asignacion_funcion_id),
    CONSTRAINT chk_jefe_nivel CHECK (nivel_autorizacion > 0),
    CONSTRAINT fk_jefe_asignacion FOREIGN KEY (asignacion_funcion_id)
        REFERENCES asignacion_funcion (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- CLIENTES Y TARJETAS DE FIDELIZACIÓN
-- Criterio de contacto: teléfono y email son opcionales por separado, pero
-- debe constar al menos uno. La tarjeta sin titular tiene cliente_id NULL y
-- entonces fecha_asignacion también debe ser NULL.
-- ----------------------------------------------------------------------------
CREATE TABLE cliente_tienda (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    codigo_cliente  VARCHAR(30)  NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    telefono        VARCHAR(30)  NULL,
    email           VARCHAR(160) NULL,
    CONSTRAINT uq_cliente_codigo UNIQUE (codigo_cliente),
    CONSTRAINT chk_cliente_contacto CHECK (telefono IS NOT NULL OR email IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tarjeta_fidelizacion (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    numero_tarjeta   VARCHAR(30) NOT NULL,
    fecha_emision    DATE        NOT NULL,
    fecha_asignacion DATE        NULL,
    cliente_id       INT         NULL,
    CONSTRAINT uq_tarjeta_numero   UNIQUE (numero_tarjeta),
    CONSTRAINT uq_tarjeta_cliente  UNIQUE (cliente_id),
    CONSTRAINT chk_tarjeta_estado  CHECK ((cliente_id IS NULL AND fecha_asignacion IS NULL)
                                       OR (cliente_id IS NOT NULL AND fecha_asignacion IS NOT NULL)),
    CONSTRAINT fk_tarjeta_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente_tienda (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- TIENDA: CATÁLOGO, MODALIDADES E HISTORIAL DE PRECIOS
-- La modalidad es inmutable en Java y se protege además con un trigger en 02.
-- Los detalles de envase/pesada son tablas separadas, no columnas opcionales.
-- ----------------------------------------------------------------------------
CREATE TABLE catalogo_estado_comercializacion (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    CONSTRAINT uq_estado_comercializacion_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE catalogo_modalidad_producto (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    CONSTRAINT uq_modalidad_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    referencia_comercial VARCHAR(40)   NOT NULL,
    descripcion          VARCHAR(200)  NOT NULL,
    precio_venta         DECIMAL(12,2) NOT NULL,
    estado_id            INT           NOT NULL,
    modalidad_id         INT           NOT NULL,
    CONSTRAINT uq_producto_referencia UNIQUE (referencia_comercial),
    CONSTRAINT chk_producto_precio CHECK (precio_venta > 0),
    CONSTRAINT fk_producto_estado FOREIGN KEY (estado_id)
        REFERENCES catalogo_estado_comercializacion (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_producto_modalidad FOREIGN KEY (modalidad_id)
        REFERENCES catalogo_modalidad_producto (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto_envasado (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    producto_id    INT          NOT NULL,
    formato_envase VARCHAR(100) NOT NULL,
    CONSTRAINT uq_envasado_producto UNIQUE (producto_id),
    CONSTRAINT fk_envasado_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE producto_granel (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    producto_id       INT           NOT NULL,
    precision_pesada  DECIMAL(10,3) NOT NULL,
    CONSTRAINT uq_granel_producto UNIQUE (producto_id),
    CONSTRAINT chk_granel_precision CHECK (precision_pesada > 0 AND precision_pesada <= 1),
    CONSTRAINT fk_granel_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE historial_precios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    producto_id     INT           NOT NULL,
    precio_anterior DECIMAL(12,2) NOT NULL,
    precio_nuevo    DECIMAL(12,2) NOT NULL,
    fecha_cambio    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_historial_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- PROVEEDORES DEL CATÁLOGO
-- UNIQUE (proveedor_id, producto_id): un acuerdo vigente por pareja.
-- "Nadie aislado" se comprueba al confirmar altas/bajas; queda documentado
-- como regla pendiente de transacción en la fase JDBC.
-- ----------------------------------------------------------------------------
CREATE TABLE proveedor (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    codigo_interno       VARCHAR(30)  NOT NULL,
    identificacion_fiscal VARCHAR(40) NOT NULL,
    razon_social         VARCHAR(160) NOT NULL,
    telefono             VARCHAR(30)  NULL,
    email                VARCHAR(160) NULL,
    CONSTRAINT uq_proveedor_codigo UNIQUE (codigo_interno),
    CONSTRAINT uq_proveedor_fiscal UNIQUE (identificacion_fiscal),
    CONSTRAINT chk_proveedor_contacto CHECK (telefono IS NOT NULL OR email IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE acuerdo_suministro (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    proveedor_id         INT           NOT NULL,
    producto_id          INT           NOT NULL,
    referencia_proveedor VARCHAR(60)   NOT NULL,
    precio_compra        DECIMAL(12,2) NOT NULL,
    plazo_entrega_dias   INT           NOT NULL,
    CONSTRAINT uq_acuerdo_proveedor_producto UNIQUE (proveedor_id, producto_id),
    CONSTRAINT chk_acuerdo_precio CHECK (precio_compra > 0),
    CONSTRAINT chk_acuerdo_plazo CHECK (plazo_entrega_dias >= 0),
    CONSTRAINT fk_acuerdo_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedor (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_acuerdo_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- CAMPAÑAS PROMOCIONALES
-- ----------------------------------------------------------------------------
CREATE TABLE campana (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    codigo       VARCHAR(30)  NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    descripcion  VARCHAR(300) NOT NULL,
    fecha_inicio DATE         NOT NULL,
    fecha_fin    DATE         NOT NULL,
    CONSTRAINT uq_campana_codigo UNIQUE (codigo),
    CONSTRAINT chk_campana_fechas CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inscripcion_campana (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    campana_id        INT  NOT NULL,
    cliente_id        INT  NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    CONSTRAINT uq_inscripcion UNIQUE (campana_id, cliente_id),
    CONSTRAINT fk_inscripcion_campana FOREIGN KEY (campana_id)
        REFERENCES campana (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_inscripcion_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente_tienda (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- VENTAS, LÍNEAS Y PAGOS DE TIENDA
-- confirmada_pagada distingue "cobro incompleto" de "venta pagada".
-- La línea guarda el precio cobrado: el historial de precios no la recalcula.
-- ----------------------------------------------------------------------------
CREATE TABLE catalogo_medio_pago (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    CONSTRAINT uq_medio_pago_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE venta (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    referencia_ticket VARCHAR(40)  NOT NULL,
    fecha_hora        DATETIME(6)  NOT NULL,
    estacion_id       INT          NOT NULL,
    empleado_id       INT          NOT NULL,
    cliente_id        INT          NULL,
    confirmada_pagada BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_venta_ticket UNIQUE (referencia_ticket),
    CONSTRAINT fk_venta_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_venta_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente_tienda (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE linea_venta (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    venta_id        INT           NOT NULL,
    producto_id     INT           NOT NULL,
    numero_orden    INT           NOT NULL,
    cantidad        DECIMAL(12,3) NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    importe         DECIMAL(14,2) NOT NULL,
    CONSTRAINT uq_linea_orden UNIQUE (venta_id, numero_orden),
    CONSTRAINT chk_linea_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_linea_precio CHECK (precio_unitario > 0),
    CONSTRAINT chk_linea_importe CHECK (importe > 0),
    CONSTRAINT fk_linea_venta FOREIGN KEY (venta_id)
        REFERENCES venta (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cobro_tienda (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    venta_id       INT           NOT NULL,
    importe        DECIMAL(12,2) NOT NULL,
    fecha_hora     DATETIME(6)   NOT NULL,
    medio_pago_id  INT           NOT NULL,
    CONSTRAINT chk_cobro_importe CHECK (importe > 0),
    CONSTRAINT fk_cobro_venta FOREIGN KEY (venta_id)
        REFERENCES venta (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cobro_medio FOREIGN KEY (medio_pago_id)
        REFERENCES catalogo_medio_pago (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- DEVOLUCIONES E INCIDENCIAS
-- La suma acumulada por línea no queda resuelta por CHECK/FK: se documenta
-- como pendiente de control transaccional en la siguiente fase.
-- ----------------------------------------------------------------------------
CREATE TABLE devolucion (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    linea_venta_id      INT           NOT NULL,
    cantidad            DECIMAL(12,3) NOT NULL,
    motivo              VARCHAR(250)  NOT NULL,
    fecha_hora          DATETIME(6)   NOT NULL,
    empleado_id         INT           NOT NULL,
    importe_reintegrado DECIMAL(12,2) NOT NULL,
    apta_para_reventa   BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_devolucion_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_devolucion_importe CHECK (importe_reintegrado > 0),
    CONSTRAINT fk_devolucion_linea FOREIGN KEY (linea_venta_id)
        REFERENCES linea_venta (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_devolucion_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE catalogo_estado_incidencia (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    CONSTRAINT uq_estado_incidencia_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incidencia (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    referencia              VARCHAR(30)  NOT NULL,
    descripcion             VARCHAR(300) NOT NULL,
    fecha_apertura          DATETIME(6)  NOT NULL,
    estacion_id             INT          NOT NULL,
    estado_id               INT          NOT NULL,
    empleado_responsable_id INT          NULL,
    CONSTRAINT uq_incidencia_referencia UNIQUE (referencia),
    CONSTRAINT fk_incidencia_estacion FOREIGN KEY (estacion_id)
        REFERENCES estacion (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidencia_estado FOREIGN KEY (estado_id)
        REFERENCES catalogo_estado_incidencia (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidencia_empleado FOREIGN KEY (empleado_responsable_id)
        REFERENCES empleado (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
