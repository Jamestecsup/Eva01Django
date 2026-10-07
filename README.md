# Evaluación 02 - Sistema de Gestión Hospitalaria

Sistema de gestión hospitalaria desarrollado con **Spring Boot 4.1.1** (compatible con Java 11/17) como evaluación del curso de Desarrollo de Aplicaciones Web Avanzado. La aplicación implementa autenticación basada en sesiones, control de acceso por roles y permisos (RBAC), gestión completa de usuarios, áreas, roles y permisos, trazabilidad mediante auditoría detallada y recuperación de contraseña.

---

## Características principales

### Evaluación 01 (Base)
- ✅ Autenticación de usuarios con contraseña cifrada con BCrypt
- ✅ Protección contra fuerza bruta: bloqueo temporal tras 3 intentos fallidos (30 minutos)
- ✅ Control de acceso por roles y permisos (RBAC) con permisos finos por módulo y acción
- ✅ Gestión de usuarios ligados a empleados y organizados por áreas
- ✅ Gestión de áreas, roles y permisos
- ✅ Registro de auditoría de las operaciones realizadas por cada usuario
- ✅ Recuperación de contraseña mediante tokens con expiración de 24 horas
- ✅ API REST para integración y aplicación web con Thymeleaf

### Evaluación 02 (Nuevas funcionalidades)
- ✅ **Relaciones JPA/Hibernate completas**: `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` entre entidades
- ✅ **Auditoría detallada**: Registro específico de cada cambio (usuario, fecha/hora, operación, entidad, ID, detalle campo a campo)
- ✅ **Gestión completa de usuarios**: CRUD, activar/desactivar, asignar rol, restablecer contraseña
- ✅ **Gestión completa de roles**: CRUD, activar/desactivar, asignación granular de permisos
- ✅ **Frontend web integrado**: Formularios, listados, edición, activación/desactivación con Thymeleaf + Bootstrap 5
- ✅ **Control de acceso por rol**: Menú dinámico según permisos, validación en backend, redirección post-login
- ✅ **Eliminación permanente (hard delete)** para áreas, además de soft delete (activar/desactivar)

---

## Stack tecnológico

| Tecnología | Versión |
|------------|---------|
| Java | 11/17 |
| Spring Boot | 4.1.1 |
| Spring Data JPA (Hibernate) | Incluido |
| Thymeleaf | Incluido |
| Spring Security Crypto (BCrypt) | Incluido |
| MySQL Connector/J | Incluido |
| Maven | Wrapper incluido |
| Lombok | Incluido |

---

## Estructura del proyecto

```
src/main/java/com/tecup/exam_1/
├── config/          Clases de configuración (seguridad web, cifrado, datos iniciales)
├── controller/
│   ├── api/         Controladores REST (/api/**)
│   └── web/         Controladores de la aplicación web (Thymeleaf)
├── dto/             Objetos de transferencia de datos y validaciones
├── exception/       Manejo global de excepciones de la API
├── model/           Entidades JPA y enumerados
├── repository/      Repositorios de persistencia (Spring Data JPA)
├── seguridad/       Servicios de sesión y permisos
└── service/         Lógica de negocio
src/main/resources/
├── static/          Recursos estáticos (CSS)
└── templates/       Vistas Thymeleaf
```

---

## Modelo de datos y relaciones JPA

El esquema se genera automáticamente mediante Hibernate (`ddl-auto=update`). Las tablas principales y sus relaciones:

| Entidad | Relaciones |
|---------|------------|
| `Usuario` | `@ManyToOne` → `Rol`, `@OneToOne` ↔ `Empleado`, `@OneToMany` → `Auditoria` |
| `Empleado` | `@ManyToOne` → `Area`, `@OneToOne` ↔ `Usuario` |
| `Area` | `@OneToMany` → `Empleado` |
| `Rol` | `@ManyToMany` → `Permiso` (tabla `rol_permiso`), `@OneToMany` → `Usuario` |
| `Permiso` | `@ManyToMany` → `Rol` |
| `Auditoria` | `@ManyToOne` → `Usuario` |
| `TokenRecuperacion` | `@OneToOne` → `Usuario` |

---

## Requisitos previos

- JDK 11 o 17.
- MySQL 8.x corriendo en `localhost:3306`.
- Base de datos `db_datos_1` creada.

### Opción rápida con Docker:
```bash
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD= \
  -e MYSQL_DATABASE=db_datos_1 \
  -p 3306:3306 mysql:8
```

---

## Configuración

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_datos_1?useSSL=false&serverTimezone=America/Lima&characterEncoding=utf8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
server.port=8080

# Java 11 compatibility
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

## Ejecución

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`. Al iniciar por primera vez, un `DataSeeder` carga datos iniciales si la base de datos está vacía.

---

## Credenciales iniciales

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | `Admin123` | SUPER ADMINISTRADOR |
| `medico` | `Medico123` | Médico |

---

## API REST

### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/recuperar` | Solicitar recuperación de contraseña |
| POST | `/api/auth/restablecer` | Restablecer contraseña con token |

### Usuarios
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/usuarios` | Listar usuarios (filtros: `busqueda`, `estado`, `rolId`) |
| GET | `/api/usuarios/{id}` | Obtener usuario por id |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Editar usuario |
| DELETE | `/api/usuarios/{id}` | Desactivar usuario (soft delete) |
| POST | `/api/usuarios/{id}/activar` | Activar usuario |
| POST | `/api/usuarios/{id}/desactivar` | Desactivar usuario |
| PUT | `/api/usuarios/{id}/password` | Cambiar contraseña (usuario autenticado) |
| PUT | `/api/usuarios/{id}/password-admin` | Restablecer contraseña (admin) |

### Roles
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/roles` | Listar roles (filtro: `estado`) |
| GET | `/api/roles/{id}` | Obtener rol por id |
| GET | `/api/roles/{id}/permisos` | Permisos del rol |
| POST | `/api/roles` | Crear rol |
| PUT | `/api/roles/{id}` | Editar rol |
| PUT | `/api/roles/{id}/permisos` | Asignar permisos al rol |
| DELETE | `/api/roles/{id}` | Desactivar rol |
| POST | `/api/roles/{id}/activar` | Activar rol |

### Permisos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/permisos` | Listar permisos |
| GET | `/api/permisos/modulos` | Permisos agrupados por módulo |
| GET | `/api/permisos/{id}` | Obtener permiso por id |
| POST | `/api/permisos` | Crear permiso |
| PUT | `/api/permisos/{id}` | Editar permiso |
| DELETE | `/api/permisos/{id}` | Eliminar permiso |

### Áreas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/areas` | Listar áreas (filtro: `estado`) |
| GET | `/api/areas/{id}` | Obtener área por id |
| POST | `/api/areas` | Crear área |
| PUT | `/api/areas/{id}` | Editar área |
| DELETE | `/api/areas/{id}` | **Eliminar permanentemente** (hard delete) |
| POST | `/api/areas/{id}/desactivar` | Desactivar área (soft delete) |
| POST | `/api/areas/{id}/activar` | Activar área |

### Auditoría
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/auditorias` | Últimas 100 auditorías |
| GET | `/api/auditorias/usuario/{username}` | Auditorías por usuario |
| GET | `/api/auditorias/{id}` | Obtener auditoría por id |

---

## Aplicación Web (Thymeleaf)

| Ruta | Descripción | Permisos requeridos |
|------|-------------|---------------------|
| `/login` | Inicio de sesión | - |
| `/dashboard` | Panel principal | `DASHBOARD:VER` |
| `/usuarios/**` | Gestión de usuarios | `USUARIOS:VER/CREAR/EDITAR/ELIMINAR` |
| `/roles/**` | Gestión de roles y permisos | `ROLES:VER/CREAR/EDITAR/ELIMINAR` |
| `/areas/**` | Gestión de áreas | `AREAS:VER/CREAR/EDITAR/ELIMINAR` |
| `/auditoria` | Consulta de auditoría | `AUDITORIA:VER` |
| `/recuperar` | Solicitud de recuperación | - |
| `/restablecer` | Restablecimiento de contraseña | - |
| `/usuarios/perfil` | Mi perfil / cambio de contraseña | Autenticado |

---

## Auditoría - Detalle de cambios

La auditoría registra **cada campo modificado** de forma individual:

**Ejemplo edición de usuario:**
```
Se actualizó el usuario juan.perez.
Cargo: Enfermero → Jefe de Enfermería
Área: Consulta → Enfermería
Rol: ENFERMERÍA → SUPERVISOR
Estado: ACTIVO → INACTIVO
```

**Ejemplo asignación de permisos a rol:**
```
Permisos actualizados en rol Administrador.
Agregados: USUARIOS:CREAR, USUARIOS:EDITAR, AREAS:VER
Removidos: AUDITORIA:EXPORTAR
```

**Ejemplo edición de rol (cambio de estado):**
```
Se actualizó el rol: Administrador → Administrador.
Estado: ACTIVO → INACTIVO
```

---

## Permisos del sistema (Módulos)

| Módulo | Acciones |
|--------|----------|
| `DASHBOARD` | VER |
| `USUARIOS` | VER, CREAR, EDITAR, ELIMINAR, EXPORTAR |
| `ROLES` | VER, CREAR, EDITAR, ELIMINAR, EXPORTAR |
| `AREAS` | VER, CREAR, EDITAR, ELIMINAR, EXPORTAR |
| `AUDITORIA` | VER, EXPORTAR |

---

## Roles predefinidos (DataSeeder)

| Rol | Permisos |
|-----|----------|
| `SUPER ADMINISTRADOR` | Todos los permisos (acceso total) |
| `Administrador` | Todos los permisos |
| `Director` | Dashboard, Usuarios:VER, Auditoría:VER |
| `Médico` | Dashboard:VER |
| `Enfermería` | Dashboard:VER |
| `Recepcionista` | Dashboard:VER |
| `Farmacia` | Dashboard:VER |
| `Laboratorio` | Dashboard:VER |
| `Contabilidad` | Dashboard:VER |
| `Almacén` | Dashboard:VER |
| `Consulta` | Dashboard:VER |

---

## Autor

**Ricardo Coello Palomino**  
Desarrollo de Aplicaciones Web - Sección 4 - C24 - A/B  
Evaluación 02 - Sistema de Gestión de Pedidos (Hospitalario)