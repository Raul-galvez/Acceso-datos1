# Contexto del repositorio

Repositorio de prácticas de **Acceso a datos** (Java, IES Luis Braille). Se organiza en dos bloques de trabajo con dos dominios distintos: una **gasolinera** y un **jardín botánico**.

## Estructura

- **`RA1/Gasolinera.1/`** — aplicación de consola que gestiona clientes y pagos. Define la interfaz `Almacenamiento`, guarda los datos en CSV (`datos/`) y añade una primera conexión JDBC a SQLite (`gasolinera.sqlite`).
- **`RA1/Gasolinera.2/`** — evolución de la anterior: sustituye/añade persistencia en JSON (`GestionJson`), incluye migración de CSV a JSON y mantiene el menú y las validaciones en `Main`.
- **`RA2-JDBC/Gasolinera.2/`** — copia de la gasolinera adaptada al entorno del bloque JDBC (proyecto IntelliJ independiente).
- **`RA2-JDBC/RA2-JDBC/`** — proyecto **Gradle/JDK 21** del jardín botánico. Es el núcleo de JDBC:
  - `sql/sqlite/` y `sql/mysql/`: esquemas y datos de ejemplo.
  - `src/main/java/.../bloque02/`: conexiones SQLite/MySQL, metadatos y manejo de errores de conexión.
  - `src/main/java/.../bloque03/`: creación de tablas, errores SQL y ejecución de varias sentencias.
  - `src/test/java/.../bloque02/`: prueba JUnit de conexión SQLite.
  - `tools/`: scripts Python para generar los datos SQL y preparar/validar `botanico.sqlite`.
  - `build.gradle.kts`: dependencias (SQLite, MySQL, JUnit) y tareas `runSqlite`, `runMysql`, `runCrearTabla`, etc.
- **`.idea/` y `out/`** — configuración de IntelliJ y clases compiladas; son artefactos locales, no código fuente relevante.

## Idea principal

Practicar la separación entre modelo y persistencia (`Almacenamiento` → CSV/JSON) en RA1, y el uso directo de **JDBC** (conexiones, DDL, metadatos, excepciones, SQLite y MySQL) en RA2. Cada subproyecto tiene su propio `README` con instrucciones de ejecución.
