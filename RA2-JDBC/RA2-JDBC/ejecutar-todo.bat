@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat test runCrearBaseDatos runSqlite runSqliteMetadata runErrorConexion runCrearTabla runErrorSql runVariasSentencias --console=plain
echo.
echo runMysql necesita un servidor MySQL local y las variables JARDIN_MYSQL_URL,
echo JARDIN_MYSQL_USER y JARDIN_MYSQL_PASSWORD.
echo.
pause
