# Contexto de RA2 — JDBC

Este archivo describe **cómo funciona este Resultado de Aprendizaje**. El trabajo de
RA2 se hace **solo en JDBC**: conexiones, metadatos, DDL/DML, transacciones y
excepciones contra SQLite y MySQL. El resto del repositorio (`RA1/`, gasolinera,
CSV/JSON) es contexto de otra unidad y **no se toca**.

> El `CONTEXT.md` de la raíz describe el repositorio completo. Este archivo es la
> guía práctica del bloque JDBC.

## Dónde está el proyecto

| Ruta | Para qué sirve |
| --- | --- |
| `RA2-JDBC/RA2-JDBC/` | **Proyecto real de trabajo**: Gradle + JDK 21, jardín botánico. |
| `RA2-JDBC/RA2-JDBC/build.gradle.kts` | Dependencias, toolchain y todas las tareas `run*`. |
| `RA2-JDBC/RA2-JDBC/src/main/java/.../jardin/bloque02/` | Conexión, metadatos, creación de la BD y errores de conexión. |
| `RA2-JDBC/RA2-JDBC/src/main/java/.../jardin/bloque03/` | Creación de tablas, errores SQL y ejecución de varias sentencias. |
| `RA2-JDBC/RA2-JDBC/src/test/java/.../bloque02/` | Prueba JUnit de SQLite (claves foráneas y cierre de conexión). |
| `RA2-JDBC/RA2-JDBC/sql/sqlite/` y `sql/mysql/` | Esquemas (`01-esquema.sql`) y datos (`02-datos.sql`). |
| `RA2-JDBC/RA2-JDBC/tools/` | Scripts Python de apoyo (generar/validar datos). No son ejemplos JDBC. |
| `RA2-JDBC/Gasolinera.2/` | **Fuera de alcance**: copia de RA1 (CSV/JSON), no JDBC. No modificar. |

Abre en IntelliJ **la carpeta interna** `RA2-JDBC/RA2-JDBC` (donde está
`build.gradle.kts`). El wrapper fija **Gradle 9.8.0** y el proyecto usa **JDK 21**.

## Dominio de datos: jardín botánico

Cinco tablas con claves foráneas y restricciones `CHECK`:
`botanico` (8000) → `credencial` (5000), `parcela` (10000) → `arbol` (40000, con
autorreferencia `arbol_origen_id`), y `estudio` (30000, PK compuesta).

- SQLite usa `STRICT`, `CHECK (length(trim(...)) > 0)` y fechas `AAAA-MM-DD` como texto.
- MySQL usa `AUTO_INCREMENT`, `InnoDB`, `utf8mb4` y dos `BEFORE INSERT/UPDATE`
  triggers para impedir que un árbol sea su propio origen.
- Los datos son deterministas (los genera `tools/generar_datos.py` con lotes de
  500 INSERT) y se versionan en los `.sql` para poder trabajar solo con SQL.

## Cómo ejecutar

Desde `RA2-JDBC/RA2-JDBC` en terminal:

```
gradlew.bat test                        # JUnit: PRAGMA foreign_keys y cierre de conexión
gradlew.bat runCrearBaseDatos           # regenera botanico.sqlite desde sql/sqlite
gradlew.bat runSqlite                   # PrimeraConexionSqlite
gradlew.bat runSqliteMetadata           # MetadatosSqlite
gradlew.bat runErrorConexion            # URL jdbc:desconocido:... (error esperado)
gradlew.bat runCrearTabla               # DDL en :memory: + DatabaseMetaData.getTables
gradlew.bat runErrorSql                 # INSRT deliberado + SQLState
gradlew.bat runVariasSentencias         # executeUpdate y filas afectadas en :memory:
gradlew.bat runMysql                    # requiere servidor MySQL local
```

En IntelliJ usa las configuraciones `RA2 - ...` del desplegable de Run; todas
apuntan a esas mismas tareas Gradle. Atajos: `run.bat` (pruebas + SQLite +
metadatos) y `ejecutar-todo.bat` (todo salvo MySQL).

`runMysql` necesita `JARDIN_MYSQL_URL`, `JARDIN_MYSQL_USER` y
`JARDIN_MYSQL_PASSWORD`, o tres argumentos en ese orden. **No poner credenciales
en el código** (hay líneas comentadas con valores de ejemplo que no hay que
reactivar).

## Qué enseña cada clase (puntos clave)

- **`CrearBaseDatos`**: borra `botanico.sqlite`, activa FK, desactiva
  auto-commit, ejecuta los dos scripts en una transacción con `commit()` /
  `rollback()`, y verifica recuentos, `foreign_key_check` e `integrity_check`.
  Su método `dividirSentencias` parte por `;` respetando comillas y comentarios;
  ignora `PRAGMA`/`BEGIN`/`COMMIT` porque Java gestiona la conexión y la tx.
- **`PrimeraConexionSqlite`**: `DriverManager.getConnection("jdbc:sqlite:" + ruta)`;
  por defecto `botanico.sqlite`, o la ruta pasada como primer argumento.
  Muestra `isValid(2)`, `getAutoCommit()` y el cierre con try-with-resources.
- **`MetadatosSqlite`**: `DatabaseMetaData` (gestor, versión, driver). Ruta fija
  `botanico.sqlite` (no acepta argumentos, aunque el README lo sugiera).
- **`PrimeraConexionMysql`**: conexión con credenciales externas y metadatos del
  gestor; imprime `SQLState` si falla.
- **`ErrorDeConexion`** / **`ErrorSql`**: excepciones esperadas, `SQLException`,
  `getSQLState()`, y corrección de la sentencia.
- **`CrearTabla`** / **`VariasSentencias`**: usan `jdbc:sqlite::memory:` para no
  tocar el fichero; cada `getConnection` crea una BD distinta, así que todas las
  sentencias comparten **una sola** `Connection`. `execute()` indica si hay
  `ResultSet`; `executeUpdate()` devuelve filas afectadas (0 en DDL de SQLite).

## Reglas que se aplican siempre

1. En SQLite, **activar `PRAGMA foreign_keys = ON` en cada conexión nueva**, antes
   de cualquier transacción. La prueba JUnit lo demuestra.
2. Cerrar `Connection`, `Statement` y `ResultSet` con **try-with-resources**.
3. Usar `execute()` para SQL genérico, `executeQuery()` para consultas y
   `executeUpdate()` para DML/DDL; comprobar siempre el valor devuelto.
4. Con `autoCommit = false`, terminar con `commit()` o `rollback()`.
5. Los ejemplos de bloque 03 son efímeros: **no modificar** `botanico.sqlite`.
6. `botanico.sqlite` es un artefacto local generado; no forma parte de la entrega
   (está en `.gitignore`). Para recrearlo: `runCrearBaseDatos` (JDBC) o
   `python tools/preparar_sqlite.py` (Python, esquema del bloque 1).
7. Convención al añadir ejemplos: clase `final` con constructor privado y
   `main`, en el paquete `bloque02` o `bloque03`, más su tarea `JavaExec` en
   `build.gradle.kts` y su configuración `RA2 - ...` en `.idea/runConfigurations`.

## Utilidades Python (apoyo, no JDBC)

- `python tools/generar_datos.py` → reescribe ambos `02-datos.sql`.
- `python tools/validar_sqlite.py` → comprueba en memoria recuentos, JOIN, FK y
  restricciones; falla si se acepta una fila inválida.
- `python tools/preparar_sqlite.py [ruta]` → crea una BD nueva desde los scripts;
  por defecto `data/jardin-botanico.db` y **no sobrescribe** nada existente.

## Detalles a tener en cuenta

- El `README.md` habla de "bloques 01 y 02" y de "cuatro ejemplos" en
  `bloque02`, pero el código actual tiene `bloque02` y `bloque03`, y cinco
  clases en `bloque02` (incluida `CrearBaseDatos`). Manda el código.
- Hay configuraciones de IntelliJ tanto en `RA2-JDBC/.idea/` como en
  `RA2-JDBC/RA2-JDBC/.idea/`; las del proyecto interno son las que se usan al
  abrir la carpeta con `build.gradle.kts`.
- `out/` y las carpetas generadas por Gradle/IntelliJ son artefactos locales:
  no editar ni versionar.
