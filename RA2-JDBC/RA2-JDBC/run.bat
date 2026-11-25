@echo off
setlocal
cd /d "%~dp0"

echo === Pruebas ===
call gradlew.bat test --console=plain
echo === Primera conexion SQLite ===
call gradlew.bat runSqlite --console=plain
echo === Metadatos SQLite ===
call gradlew.bat runSqliteMetadata --console=plain
echo.
pause
