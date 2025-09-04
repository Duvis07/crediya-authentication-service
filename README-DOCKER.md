# 🐳 Authentication Service - Docker/Podman

## 📋 Resumen General

Microservicio de autenticación y gestión de usuarios completamente dockerizado que proporciona JWT tokens y se comunica con PostgreSQL.

## 🏗️ Arquitectura

```
PostgreSQL Container ◄─── Authentication Service Container ◄─── HTTP Calls from Solicitudes Service
    (Port 5432)              (Port 8080)                          (Port 8081)
```

### 🌐 Comunicación entre Microservicios

**Red Docker Compartida (`crediya-network`):**
- Ambos microservicios (authentication-service y solicitudes-service) deben usar la **misma red Docker**
- Esto permite que se comuniquen usando nombres de contenedor en lugar de IPs
- La red se crea automáticamente cuando se ejecuta `docker-compose up`

**Flujo de Comunicación:**
1. **Authentication Service** recibe petición de login en puerto 8080
2. Valida credenciales contra **PostgreSQL** en puerto 5432
3. Genera JWT token y lo devuelve al cliente
4. **Solicitudes Service** valida tokens llamando a endpoints de validación

**Configuración de Red:**
- **Authentication Service:** `http://crediya-authentication-service:8080`
- **Solicitudes Service:** `http://crediya-solicitudes-service:8081`
- **PostgreSQL:** `crediya-postgres-authentication:5432`

**¿Por qué la misma red?**
- Sin red compartida: Los contenedores no pueden comunicarse entre sí
- Con red compartida: Resolución de nombres automática entre servicios
- Seguridad: Aislamiento del tráfico de otros contenedores del sistema

## 📁 Archivos Docker

### `deployment/Dockerfile`
**Propósito:** Crea la imagen del microservicio usando multi-stage build para optimizar el tamaño final.
**Contiene:** Instrucciones para construir imagen con OpenJDK 17, copiar JAR compilado, exponer puerto 8080 y definir comando de inicio.

### `docker-compose.yml`
**Propósito:** Orquesta todos los servicios necesarios (PostgreSQL + Authentication Service) en contenedores.
**Contiene:** Definición de servicios, puertos, volúmenes, variables de entorno, dependencias y red compartida.

### `deployment/init-db.sql`
**Propósito:** Inicializa automáticamente la base de datos PostgreSQL cuando se crea el contenedor.
**Contiene:** Scripts SQL para crear tablas de usuarios, roles y permisos e insertar datos iniciales de prueba.

### `.env`
**Propósito:** Define variables de entorno para ejecución local del microservicio (sin Docker).
**Contiene:** Configuración de base de datos local, secretos JWT, configuración de seguridad y perfil de Spring.

## 📜 Scripts de Automatización

### `scripts/build.bat`
**Propósito:** Compila la aplicación Spring Boot y prepara el JAR para Docker.
**Contiene:** Comandos para limpiar, compilar con Gradle y copiar JAR al directorio deployment.

### `scripts/clean-restart.bat`
**Propósito:** Reinicio completo del entorno Docker eliminando todo rastro anterior.
**Contiene:** Comandos para parar servicios, eliminar imágenes/volúmenes, reconstruir desde cero e iniciar servicios.

### `scripts/deploy.bat`
**Propósito:** Script de despliegue rápido sin limpieza completa.
**Contiene:** Comandos para construir y desplegar servicios manteniendo datos existentes.

## 🚀 Ejecución con Podman

### Comando Principal
```bash
scripts\clean-restart.bat
```

**Este script:**
- Para servicios existentes y limpia volúmenes
- Elimina imágenes anteriores del proyecto
- Reconstruye desde cero con `gradlew bootJar`
- Inicia PostgreSQL + Authentication Service
- Verifica estado final

### Comandos Alternativos
```bash
# Solo iniciar
podman-compose up -d

# Reconstruir
podman-compose build --no-cache

# Ver estado
podman-compose ps
```

## 🔐 Endpoints Principales

### Autenticación
- `POST /api/v1/auth/login` - Login de usuario
- `POST /api/v1/auth/register` - Registro de usuario
- `GET /api/v1/auth/validate` - Validación de token JWT

### Gestión de Usuarios
- `GET /api/v1/usuarios/{documentId}` - Obtener usuario por documento
- `PUT /api/v1/usuarios/{id}` - Actualizar usuario
- `DELETE /api/v1/usuarios/{id}` - Eliminar usuario

### Monitoreo
- `GET /actuator/health` - Estado del servicio
- `GET /actuator/info` - Información del servicio

## 🔧 Variables de Entorno

**Base de Datos:**
- `R2DBC_HOST` - Host de PostgreSQL
- `R2DBC_PORT` - Puerto de PostgreSQL  
- `R2DBC_DATABASE` - Nombre de la base de datos
- `R2DBC_USERNAME` - Usuario de BD
- `R2DBC_PASSWORD` - Contraseña de BD

**JWT:**
- `JWT_SECRET` - Secreto para firmar tokens
- `JWT_EXPIRATION` - Tiempo de expiración de tokens

**Aplicación:**
- `SPRING_PROFILES_ACTIVE` - Perfil de Spring activo

## 📋 Requisitos

- **Podman** o Docker instalado
- **Java 17** (para compilación local)
- **Gradle** (incluido con wrapper)
- **Puerto 8080** disponible para el servicio
- **Puerto 5433** disponible para PostgreSQL

## 🚨 Notas Importantes

1. **Orden de Inicio:** Authentication Service debe iniciarse ANTES que Solicitudes Service
2. **Red Compartida:** Ambos microservicios deben usar `crediya-network`
3. **Base de Datos:** Cada microservicio tiene su propia instancia de PostgreSQL
4. **JWT:** El secreto JWT debe ser el mismo en ambos microservicios
5. **Puertos:** Authentication Service usa 8080, Solicitudes Service usa 8081
