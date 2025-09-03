# CrediYa - Microservicio de Autenticación

Microservicio de autenticación para la plataforma CrediYa, desarrollado con **Clean Architecture** y **Spring WebFlux** para gestión de usuarios y autenticación JWT.

## Tabla de Contenidos

- [Descripción del Proyecto](#descripción-del-proyecto)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requerimientos Técnicos](#requerimientos-técnicos)
- [Deployment con Docker/Podman](#deployment-con-dockerpodman)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Documentación API](#documentación-api)
- [Usuarios Iniciales](#usuarios-iniciales)
- [Troubleshooting](#troubleshooting)
- [Testing](#testing)

## 🚀 Descripción del Proyecto

Este microservicio implementa las **HU1** y **HU3** del sistema CrediYa:

- **HU1**: Registro de usuarios en el sistema
- **HU3**: Autenticación y autorización con JWT

### Funcionalidades Principales

- ✅ Registro de usuarios (Admin/Asesor/Solicitante)
- ✅ Autenticación con JWT
- ✅ Validaciones robustas de datos
- ✅ Manejo de excepciones personalizado
- ✅ Logs de traza completos
- ✅ Arquitectura reactiva con WebFlux

## 🏗️ Arquitectura

Este proyecto implementa **Clean Architecture** con **Spring WebFlux** para programación reactiva.

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

### Capas de la Arquitectura

#### 🎯 Domain (Núcleo del Negocio)
- **Entities**: Modelos de dominio (`User`, `Role`, `UserType`)
- **Business Rules**: Lógica de negocio pura
- **Exceptions**: Excepciones de dominio (`UserAlreadyExistsException`)

#### 🔧 Use Cases (Casos de Uso)
- **UserUseCase**: Orquesta la lógica de registro y consulta de usuarios
- **AuthUseCase**: Maneja autenticación y generación de tokens JWT
- **Reactive**: Implementación completamente reactiva con `Mono` y `Flux`

#### 🌐 Infrastructure (Infraestructura)

##### Entry Points (Puntos de Entrada)
- **Reactive Web**: Controllers reactivos con WebFlux
- **Handlers**: Manejo de requests HTTP
- **DTOs**: Objetos de transferencia de datos
- **Mappers**: Conversión entre capas con MapStruct

##### Driven Adapters (Adaptadores Externos)
- **R2DBC PostgreSQL**: Persistencia reactiva
- **JWT Helper**: Generación y validación de tokens

##### Helpers
- **JWT**: Utilidades para manejo de tokens JWT
- **Security**: Configuración de seguridad WebFlux

#### 🚀 Application (Configuración)
- **Main Application**: Punto de entrada de la aplicación
- **Dependency Injection**: Configuración de beans
- **Component Scan**: Auto-discovery de componentes

## 📁 Estructura del Proyecto

```
authentication-service/
├── applications/
│   └── app-service/                    # Configuración principal de la aplicación
├── domain/
│   ├── model/                          # Entidades de dominio (User, Role, UserType)
│   └── usecase/                        # Casos de uso (UserUseCase, AuthUseCase)
├── infrastructure/
│   ├── driven-adapters/
│   │   └── r2dbc-postgresql/           # Persistencia reactiva con R2DBC
│   ├── entry-points/
│   │   └── reactive-web/               # API REST reactiva con WebFlux
│   └── helpers/
│       └── jwt/                        # Utilidades JWT
├── deployment/
│   └── Dockerfile                      # Configuración Docker
├── gradle/                             # Configuración Gradle Wrapper
├── build.gradle                        # Configuración principal del proyecto
├── settings.gradle                     # Configuración de módulos
└── README.md                           # Este archivo
```

## 🛠️ Requerimientos Técnicos

### Tecnologías Principales
- **Java 17+**
- **Spring Boot 3.5.4**
- **Spring WebFlux** (Programación reactiva)
- **R2DBC PostgreSQL** (Base de datos reactiva)
- **JWT** (Autenticación)
- **MapStruct** (Mapeo de objetos)
- **Gradle 8.x** (Build tool)

### Dependencias Clave
```gradle
// Reactive Web
implementation 'org.springframework.boot:spring-boot-starter-webflux'

// R2DBC PostgreSQL
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
implementation 'org.postgresql:r2dbc-postgresql'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'

// MapStruct
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'

// Validation
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Testing
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'io.projectreactor:reactor-test'
```

## Deployment con Docker/Podman

### Prerrequisitos
- **Podman** o Docker instalado
- **podman-compose** (`pip install podman-compose`)
- Java 17+ (para build local)

### Opción 1: Deployment Rápido (Recomendado)

**Reinicio completo desde cero:**
```bash
# Windows
scripts\clean-restart.bat

# Linux/Mac
./scripts/clean-restart.sh
```

Este script ejecuta automáticamente:
1. Para todos los servicios
2. Elimina volúmenes (limpia BD)
3. Elimina imágenes viejas
4. Reconstruye la imagen
5. Inicia servicios desde cero
6. Verifica estado

### Opción 2: Comandos Manuales

**1. Construir imagen:**
```bash
# Windows
scripts\build.bat

# Linux/Mac  
./scripts/build.sh
```

**2. Iniciar servicios:**
```bash
podman-compose up -d
```

**3. Verificar estado:**
```bash
podman-compose ps
podman-compose logs -f authentication-service
```

**4. Detener servicios:**
```bash
podman-compose down
```

**5. Reinicio limpio (eliminar volúmenes):**
```bash
podman-compose down -v
```

### Opción 3: Scripts de Deployment

**Windows:**
```bash
# Iniciar servicios
scripts\deploy.bat up

# Detener servicios
scripts\deploy.bat down

# Reiniciar servicios
scripts\deploy.bat restart

# Ver logs
scripts\deploy.bat logs

# Ver estado
scripts\deploy.bat status

# Construir e iniciar
scripts\deploy.bat build
```

**Linux/Mac:**
```bash
# Iniciar servicios
./scripts/deploy.sh up

# Detener servicios
./scripts/deploy.sh down

# Reiniciar servicios
./scripts/deploy.sh restart

# Ver logs
./scripts/deploy.sh logs

# Ver estado
./scripts/deploy.sh status

# Construir e iniciar
./scripts/deploy.sh build
```

### Configuración de Servicios

El `docker-compose.yml` incluye:

**PostgreSQL:**
- Puerto: `5433:5432` (externo:interno)
- Base de datos: `crediya_authentication_db`
- Usuario: `postgres`
- Contraseña: `nueva_password`
- Volumen persistente: `postgres_auth_data`

**Authentication Service:**
- Puerto: `8080:8080`
- Imagen: `crediya/authentication-service:latest`
- Dependencias: PostgreSQL (healthcheck)
- Variables de entorno configuradas automáticamente

### URLs de Acceso

Una vez iniciados los servicios:
- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Instalación y Ejecución (Desarrollo Local)

### Prerrequisitos
- Java 17 o superior
- PostgreSQL 12+ (o usar Docker)
- Gradle 8.x (o usar wrapper incluido)

### Ejecutar la Aplicación
```bash
# Clonar el repositorio
git clone <repository-url>
cd authentication-service

# Ejecutar con Gradle
./gradlew bootRun

# O compilar y ejecutar JAR
./gradlew build
java -jar applications/app-service/build/libs/authentication-service.jar
```

## 📚 Documentación API

### Swagger UI
Una vez ejecutada la aplicación, la documentación interactiva estará disponible en:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Endpoints Principales

#### Autenticación
```http
POST /api/v1/login
Content-Type: application/json

{
  "email": "admin@crediya.com",
  "password": "admin123456"
}
```

#### Registro de Usuarios
```http
POST /api/v1/usuarios
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "password": "password123",
  "birthDate": "1990-01-01",
  "phone": "3001234567",
  "address": "Calle 123",
  "baseSalary": 3000000,
  "userType": "APPLICANT"
}
```

#### Consultar Usuarios
```http
GET /api/v1/usuarios
Authorization: Bearer <jwt-token>
```

## Usuarios Iniciales

El sistema incluye usuarios predefinidos para facilitar el bootstrap inicial:

### Usuario Administrador
- **Email:** `admin@crediya.com`
- **Contraseña:** `admin123456`
- **Rol:** ADMIN
- **Propósito:** Gestión del sistema y registro de usuarios

### Usuario Asesor
- **Email:** `asesor@crediya.com`
- **Contraseña:** `asesor123456`
- **Rol:** ASESOR
- **Propósito:** Funcionalidades de asesoría de crédito

### Usuario Cliente Premium
- **Email:** `cliente@crediya.com`
- **Contraseña:** `cliente123`
- **Rol:** APPLICANT
- **Propósito:** Usuario de prueba para solicitudes

### Usuario Juan Pérez
- **Email:** `juan.perez@example.com`
- **Contraseña:** `admin123456`
- **Rol:** APPLICANT
- **Propósito:** Usuario de prueba adicional

### Notas de Seguridad
- Contraseñas encriptadas con BCrypt (strength 12)
- Cambiar contraseñas por defecto en producción
- Usuarios creados automáticamente por PostgreSQL init script

## Troubleshooting

### Problemas Comunes y Soluciones

#### 1. Error de Conexión a Base de Datos
```
Failed to obtain R2DBC Connection
```
**Solución:**
- Verificar que PostgreSQL esté corriendo: `podman-compose ps`
- Reiniciar servicios: `scripts\clean-restart.bat`
- Verificar logs: `podman-compose logs postgres-auth`

#### 2. Error de Autenticación "Invalid credentials"
```
401 Unauthorized - Invalid credentials
```
**Solución:**
- Verificar que uses las credenciales correctas (ver [Usuarios Iniciales](#usuarios-iniciales))
- Verificar que la BD tenga los usuarios: 
  ```bash
  podman exec crediya-postgres-auth psql -U postgres -d crediya_authentication_db -c "SELECT email, first_name FROM users;"
  ```

#### 3. Error de Esquema de BD
```
column users.birth_date does not exist
```
**Solución:**
- Ejecutar reinicio limpio para recrear BD: `scripts\clean-restart.bat`
- Esto eliminará volúmenes y recreará tablas con esquema correcto

#### 4. Problemas con Caracteres Especiales
```
value contains character 'ñ' which is non US-ASCII
```
**Solución:**
- Ya corregido en configuración actual
- Usar solo caracteres ASCII en contraseñas de BD

#### 5. Puerto en Uso
```
Error starting userland proxy: listen tcp 0.0.0.0:8080: bind: address already in use
```
**Solución:**
- Detener servicios existentes: `podman-compose down`
- Verificar puertos: `netstat -an | findstr :8080`
- Cambiar puerto en `docker-compose.yml` si es necesario

#### 6. Imagen Docker Corrupta
```
Error response from daemon: No such image
```
**Solución:**
- Limpiar imágenes: `podman system prune -f`
- Reconstruir: `scripts\build.bat`

### Comandos de Diagnóstico

**Verificar estado de servicios:**
```bash
podman-compose ps
```

**Ver logs en tiempo real:**
```bash
podman-compose logs -f authentication-service
podman-compose logs -f postgres-auth
```

**Conectar a PostgreSQL:**
```bash
podman exec -it crediya-postgres-auth psql -U postgres -d crediya_authentication_db
```

**Verificar conectividad:**
```bash
curl http://localhost:8080/actuator/health
```

**Probar autenticación:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@crediya.com","password":"admin123456"}'
```

## 🧪 Testing

### Ejecutar Tests
```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con reporte de cobertura
./gradlew test jacocoTestReport

# Ver reporte de cobertura
open build/reports/jacoco/test/html/index.html
```

### Tipos de Tests
- **Unit Tests**: Tests unitarios para cada capa
- **Integration Tests**: Tests de integración con base de datos
- **WebFlux Tests**: Tests reactivos con `WebTestClient`
- **Test Coverage**: Cobertura de código con JaCoCo

### Estructura de Tests
```
src/test/java/
├── api/
│   ├── handler/           # Tests de handlers
│   ├── mapper/            # Tests de mappers
│   └── RouterRestTest     # Tests de rutas
├── usecase/               # Tests de casos de uso
└── r2dbc/                 # Tests de repositorios
```

## 🔧 Desarrollo

### Herramientas de Calidad
- **SonarLint**: Análisis estático de código
- **JaCoCo**: Cobertura de código
- **Checkstyle**: Estilo de código

### Comandos Útiles
```bash
# Limpiar y compilar
./gradlew clean build

# Ejecutar análisis de calidad
./gradlew sonarqube

# Generar reporte de dependencias
./gradlew dependencies

# Ejecutar con perfil de desarrollo
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📋 Funcionalidades Implementadas

### HU1 - Registro de Usuarios ✅
- [x] Endpoint POST /api/v1/usuarios
- [x] Validaciones de datos obligatorios
- [x] Validación de email único
- [x] Validación de formato de datos
- [x] Persistencia transaccional
- [x] Logs de traza
- [x] Manejo de excepciones

### HU3 - Autenticación ✅
- [x] Endpoint POST /api/v1/login
- [x] Validación de credenciales
- [x] Generación de tokens JWT
- [x] Autorización por roles
- [x] Logs de traza
- [x] Manejo de excepciones
