@echo off
echo ========================================
echo REINICIO LIMPIO - AUTHENTICATION SERVICE
echo ========================================

echo.
echo 1. Parando todos los servicios...
podman-compose down

echo.
echo 2. Eliminando volumenes para limpiar BD...
podman-compose down -v

echo.
echo 3. Eliminando imagenes viejas...
podman rmi -f localhost/crediya-auth_authentication-service:latest 2>nul
podman rmi -f localhost/crediya/authentication-service:latest 2>nul

echo.
echo 4. Limpiando cache de imagenes...
podman system prune -f

echo.
echo 5. Reconstruyendo imagen...
call scripts\build.bat

echo.
echo 6. Iniciando servicios desde cero...
podman-compose up -d

echo.
echo 7. Esperando que los servicios esten listos...
timeout /t 30 /nobreak

echo.
echo 8. Verificando estado de los servicios...
podman-compose ps

echo.
echo ========================================
echo REINICIO COMPLETADO
echo ========================================
echo.
echo Para probar los endpoints:
echo curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@crediya.com\",\"password\":\"admin123456\"}"
echo.
