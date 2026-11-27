# AGENTS.md — Gasolinera.2 (Fase 1 MySQL)

## Regla de oro

Las decisiones importantes de diseño y arquitectura de clases las toma el
estudiante. Herencia o composición, estructuras de datos (lista, set, map...),
interfaces y responsabilidades son cuestiones críticas: identifícalas y
pregúntale antes de escribir código.

## Instrucciones de funcionamiento

- Ayudar a aprender diseño y arquitectura Java para Acceso a Datos.
- Antes de codificar, explicar brevemente las opciones razonables con ventajas,
  inconvenientes y cuándo conviene cada una, y dejar que elija.
- Justificar el código: qué hace y, sobre todo, por qué.
- Priorizar programación funcional (streams, `map`, `filter`, `collect`,
  `Optional`) frente a bucles iterativos.
- No ejecutar comandos de git sin permiso explícito.
- Explicaciones claras y en español; adaptar el nivel de detalle.
- Java 21, IntelliJ IDEA, Git ejecutado por el estudiante.

## Contexto de esta carpeta

- Es la entrega de la Fase 1 del caso `Caso_practico_Gasolinera_MySQL_Fase_1`.
- `src/` contiene la aplicación original (CSV/JSON) y el modelo ampliado.
- No modificar `Cliente`, `Pago`, `Almacenamiento`, `AlmacenamientoCSV`,
  `GestionJson` ni `MigracionCsvaJson`: CSV y JSON deben seguir funcionando.
- `sql/01_esquema.sql`, `02_triggers.sql`, `03_datos.sql` y
  `04_comprobaciones.sql` se ejecutan en ese orden sobre MySQL vacío.
- El trigger obligatorio está en `02_triggers.sql`; el programa Java no inserta
  historial de precios.
- Las clases de dominio no abren conexiones ni ejecutan SQL. Las consultas JDBC
  son de la fase siguiente.
- `out/`, `.idea/` y los ficheros generados por IntelliJ son artefactos locales.

## Decisiones ya aprobadas en esta entrega

- Trabajar en `RA2-JDBC/Gasolinera.2`.
- Funciones del personal: interfaz sellada + periodos `AsignacionFuncion`.
- Modalidades: interfaz sellada con `double` y redondeo centralizado.
- Cobros de tienda: clase `CobroTienda` separada del `Pago` heredado.

Cualquier cambio de estas decisiones debe volver a consultarse con el estudiante.
