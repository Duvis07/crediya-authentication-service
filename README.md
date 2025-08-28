# CrediYa - Microservicio de Autenticación

Microservicio de autenticación para la plataforma CrediYa, desarrollado con **Clean Architecture** y **Spring WebFlux** para gestión de usuarios y autenticación JWT.

## 📋 Tabla de Contenidos

- [Descripción del Proyecto](#descripción-del-proyecto)
- [Arquitectura](#arquitectura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Requerimientos Técnicos](#requerimientos-técnicos)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Documentación API](#documentación-api)
- [Usuarios Iniciales](#usuarios-iniciales)
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

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- PostgreSQL 12+ (o Docker)
- Gradle 8.x (o usar wrapper incluido)

### Base de Datos
1. **Crear base de datos PostgreSQL:**
```sql
CREATE DATABASE crediya_auth;
CREATE USER crediya_user WITH PASSWORD 'crediya_pass';
GRANT ALL PRIVILEGES ON DATABASE crediya_auth TO crediya_user;
```

2. **Configurar variables de entorno (.env):**
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=crediya_auth
DB_USER=crediya_user
DB_PASSWORD=crediya_pass
JWT_SECRET=your-super-secret-jwt-key-here
```

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

La aplicación estará disponible en: `http://localhost:8080`

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

## 👥 Usuarios Iniciales

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

### Notas de Seguridad
- Contraseñas encriptadas con BCrypt (strength 10)
- Cambiar contraseñas por defecto en producción
- Usuarios creados automáticamente al iniciar la aplicación

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
