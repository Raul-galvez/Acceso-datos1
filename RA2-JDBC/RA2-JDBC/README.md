# RA2 — JDBC · entrega para alumnado (bloques 01 y 02)

Abre **esta carpeta** en IntelliJ IDEA como proyecto Gradle. Usa **JDK 21**
para el proyecto y para Gradle. El Wrapper incluido descarga la versión de
Gradle fijada por el proyecto si aún no está instalada.


## Material

| Ruta | Contenido |
| --- | --- |
| `sql/sqlite/` y `sql/mysql/` | Esquemas y datos del Jardín Botánico |
| `src/main/java/.../bloque02/` | Cuatro ejemplos de conexión |
| `src/test/java/.../bloque02/` | Prueba de conexión SQLite |

Los ejemplos SQLite usan `botanico.sqlite` (en la raíz del proyecto) por
defecto, igual que `MetadatosSqlite` y `CrearBaseDatos`. También puedes pasar
otra ruta como argumento.

## Ejecutar

Desde IntelliJ abre **esta carpeta** (`RA2-JDBC/RA2-JDBC`) y usa las
configuraciones `RA2 - ...` del desplegable de Run. Desde la terminal:

```
gradlew.bat test
gradlew.bat runSqlite
gradlew.bat runSqliteMetadata
gradlew.bat runCrearTabla
gradlew.bat runErrorSql
gradlew.bat runVariasSentencias
```

`runMysql` necesita un servidor MySQL local y las variables
`JARDIN_MYSQL_URL`, `JARDIN_MYSQL_USER` y `JARDIN_MYSQL_PASSWORD` (o tres
argumentos con la URL, usuario y contraseña).

Si abres la carpeta superior (`RA2-JDBC`), las configuraciones siguen
apuntando a este proyecto; la carpeta con el `build.gradle.kts` es
`RA2-JDBC/RA2-JDBC`.

El fichero de base de datos y las carpetas de IntelliJ y Gradle generadas
localmente no forman parte de la entrega.
