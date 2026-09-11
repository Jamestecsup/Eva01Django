# Evaluacion_01_Web_A

Sistema de gestión hospitalaria desarrollado con **Spring Boot 4.1.1** como evaluación del curso de Desarrollo de Aplicaciones Web. La aplicación implementa autenticacion basada en sesiones, control de acceso por roles y permisos (RBAC), gestion de usuarios, areas, roles y permisos, trazabilidad mediante auditoria y recuperacion de contrasena.

## Caracteristicas

- Autenticacion de usuarios con contrasena cifrada con BCrypt.
- Proteccion contra fuerza bruta: bloqueo temporal tras 3 intentos fallidos (30 minutos).
- Control de acceso por roles y permisos (RBAC) con permisos finos por modulo y accion.
- Gestion de usuarios ligados a empleados y organizados por areas.
- Gestion de areas, roles y permisos.
- Registro de auditoria de las operaciones realizadas por cada usuario.
- Recuperacion de contrasena mediante tokens con expiracion de 24 horas.
- API REST para integracion y aplicacion web con Thymeleaf.

## Stack tecnologico

| Tecnologia | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Data JPA (Hibernate) | Incluido |
| Thymeleaf | Incluido |
| Spring Security Crypto (BCrypt) | Incluido |
| MySQL Connector/J | Incluido |
| Maven | Wrapper incluido |
| Lombok | Incluido |

## Estructura del proyecto

```
src/main/java/com/tecup/exam_1/
├── config/          Clases de configuracion (seguridad web, cifrado, datos iniciales)
├── controller/
│   ├── api/         Controladores REST (/api/**)
│   └── web/         Controladores de la aplicacion web (Thymeleaf)
├── dto/             Objetos de transferencia de datos y validaciones
├── exception/       Manejo global de excepciones de la API
├── model/           Entidades JPA y enumerados
├── repository/      Repositorios de persistencia (Spring Data JPA)
├── seguridad/       Servicios de sesion y permisos
└── service/         Logica de negocio
src/main/resources/
├── static/          Recursos estaticos (CSS)
└── templates/       Vistas Thymeleaf
```

## Modelo de datos

El esquema se genera automaticamente mediante Hibernate (`ddl-auto=update`). Las tablas principales son:

- `areas`
- `empleados`
- `usuarios`
- `roles`
- `permisos`
- `rol_permiso` (tabla pivote roles-permisos)
- `auditorias`
- `tokens_recuperacion`

## Requisitos previos

- JDK 17 o superior.
- MySQL 8.x corriendo en `localhost:3306`.
- Base de datos `db_datos_1` creada.

## Configuracion

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_datos_1?useSSL=false&serverTimezone=America/Lima&characterEncoding=utf8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
server.port=8080
```

## Ejecucion

```bash
./mvnw spring-boot:run
```

La aplicacion levanta en `http://localhost:8080`. Al iniciar por primera vez, un `DataSeeder` carga datos iniciales si la base de datos esta vacia.

## Credenciales iniciales

| Usuario | Contrasena | Rol |
|---------|------------|-----|
| `admin` | `Admin123` | SUPER ADMINISTRADOR |
| `medico` | `Medico123` | Medico |

## API REST

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesion |
| POST | `/api/auth/recuperar` | Solicitar recuperacion de contrasena |
| POST | `/api/auth/restablecer` | Restablecer contrasena con token |
| GET | `/api/usuarios` | Listar usuarios (filtros: `busqueda`, `estado`, `rolId`) |
| GET | `/api/usuarios/{id}` | Obtener usuario por id |
| POST | `/api/usuarios` | Crear usuario |
| PUT | `/api/usuarios/{id}` | Editar usuario |
| DELETE | `/api/usuarios/{id}` | Desactivar usuario |
| POST | `/api/usuarios/{id}/activar` | Activar usuario |
| POST | `/api/usuarios/{id}/desactivar` | Desactivar usuario |
| PUT | `/api/usuarios/{id}/password` | Cambiar contrasena |
| PUT | `/api/usuarios/{id}/password-admin` | Restablecer contrasena (admin) |
| GET | `/api/roles` | Listar roles (filtro: `estado`) |
| GET | `/api/roles/{id}` | Obtener rol por id |
| GET | `/api/roles/{id}/permisos` | Permisos del rol |
| POST | `/api/roles` | Crear rol |
| PUT | `/api/roles/{id}` | Editar rol |
| PUT | `/api/roles/{id}/permisos` | Asignar permisos al rol |
| DELETE | `/api/roles/{id}` | Desactivar rol |
| POST | `/api/roles/{id}/activar` | Activar rol |
| GET | `/api/permisos` | Listar permisos |
| GET | `/api/permisos/modulos` | Permisos agrupados por modulo |
| GET | `/api/permisos/{id}` | Obtener permiso por id |
| POST | `/api/permisos` | Crear permiso |
| PUT | `/api/permisos/{id}` | Editar permiso |
| DELETE | `/api/permisos/{id}` | Eliminar permiso |
| GET | `/api/areas` | Listar areas (filtro: `estado`) |
| GET | `/api/areas/{id}` | Obtener area por id |
| POST | `/api/areas` | Crear area |
| PUT | `/api/areas/{id}` | Editar area |
| DELETE | `/api/areas/{id}` | Desactivar area |
| POST | `/api/areas/{id}/activar` | Activar area |
| GET | `/api/auditorias` | Ultimas 100 auditorias |
| GET | `/api/auditorias/usuario/{username}` | Auditorias por usuario |
| GET | `/api/auditorias/{id}` | Obtener auditoria por id |

## Aplicacion web

| Ruta | Descripcion |
|------|-------------|
| `/login` | Inicio de sesion |
| `/dashboard` | Panel principal |
| `/usuarios/**` | Gestion de usuarios |
| `/roles/**` | Gestion de roles y asignacion de permisos |
| `/areas/**` | Gestion de areas |
| `/auditoria` | Consulta de auditoria |
| `/recuperar` | Solicitud de recuperacion de contrasena |
| `/restablecer` | Restablecimiento de contrasena |

## Autor

Fernando Correa Huincho