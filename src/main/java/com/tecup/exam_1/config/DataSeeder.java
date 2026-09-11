package com.tecup.exam_1.config;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.dto.PermisoRequest;
import com.tecup.exam_1.dto.RolRequest;
import com.tecup.exam_1.dto.UsuarioRequest;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.model.Rol;
import com.tecup.exam_1.repository.AreaRepository;
import com.tecup.exam_1.repository.PermisoRepository;
import com.tecup.exam_1.repository.RolRepository;
import com.tecup.exam_1.repository.UsuarioRepository;
import com.tecup.exam_1.service.AreaService;
import com.tecup.exam_1.service.PermisoService;
import com.tecup.exam_1.service.RolService;
import com.tecup.exam_1.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final List<String> AREAS = List.of(
            "Dirección", "Consulta", "Recepcion", "Enfermería", "Farmacia",
            "Laboratorio", "Almacén", "Contabilidad"
    );

    private static final List<String> MODULOS = List.of("DASHBOARD", "USUARIOS", "ROLES", "AREAS", "AUDITORIA");
    private static final List<String> ACCIONES = List.of("VER", "CREAR", "EDITAR", "ELIMINAR", "EXPORTAR");

    private static final List<String> ROLES = List.of(
            "SUPER ADMINISTRADOR", "Administrador", "Director", "Médico", "Enfermería",
            "Recepcionista", "Farmacia", "Laboratorio", "Contabilidad", "Almacén", "Consulta"
    );

    private final AreaRepository areaRepository;
    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AreaService areaService;
    private final PermisoService permisoService;
    private final RolService rolService;
    private final UsuarioService usuarioService;

    public DataSeeder(AreaRepository areaRepository,
                      PermisoRepository permisoRepository,
                      RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      AreaService areaService,
                      PermisoService permisoService,
                      RolService rolService,
                      UsuarioService usuarioService) {
        this.areaRepository = areaRepository;
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.areaService = areaService;
        this.permisoService = permisoService;
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        seedAreas();
        seedPermisos();
        seedRoles();
        seedUsuarios();
    }

    private void seedAreas() {
        if (areaRepository.count() > 0) {
            return;
        }
        AREAS.forEach(nombre -> {
            AreaRequest request = new AreaRequest();
            request.setNombre(nombre);
            request.setDescripcion("Área: " + nombre);
            request.setEstado("ACTIVO");
            areaService.crear(request);
        });
    }

    private void seedPermisos() {
        if (permisoRepository.count() > 0) {
            return;
        }
        MODULOS.forEach(modulo -> ACCIONES.forEach(accion -> {
            PermisoRequest request = new PermisoRequest();
            request.setModulo(modulo);
            request.setNombre(accion);
            request.setDescripcion("Permite " + accion + " en el módulo " + modulo);
            request.setEstado("ACTIVO");
            permisoService.crear(request);
        }));
    }

    private void seedRoles() {
        if (rolRepository.count() > 0) {
            return;
        }
        List<Permiso> todos = permisoService.listar();
        Set<Long> todosIds = todos.stream().map(Permiso::getId).collect(Collectors.toSet());

        ROLES.forEach(nombre -> {
            RolRequest request = new RolRequest();
            request.setNombre(nombre);
            request.setDescripcion("Rol del sistema: " + nombre);
            request.setEstado("ACTIVO");
            Rol creado = rolService.crear(request);
            if (nombre.equals("SUPER ADMINISTRADOR") || nombre.equals("Administrador")) {
                rolService.asignarPermisos(creado.getId(), todosIds);
            } else if (nombre.equals("Director")) {
                Set<Long> directivos = todos.stream()
                        .filter(p -> p.getModulo().equals("DASHBOARD")
                                || p.getModulo().equals("USUARIOS") && p.getNombre().equals("VER")
                                || p.getModulo().equals("AUDITORIA") && p.getNombre().equals("VER"))
                        .map(Permiso::getId)
                        .collect(Collectors.toSet());
                rolService.asignarPermisos(creado.getId(), directivos);
            } else {
                Set<Long> basicos = todos.stream()
                        .filter(p -> p.getModulo().equals("DASHBOARD") && p.getNombre().equals("VER"))
                        .map(Permiso::getId)
                        .collect(Collectors.toSet());
                rolService.asignarPermisos(creado.getId(), basicos);
            }
        });
    }

    private void seedUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Area direccion = areaRepository.findAllByOrderByNombreAsc().stream()
                .filter(a -> a.getNombre().equals("Dirección"))
                .findFirst().orElse(null);
        Rol superAdmin = rolRepository.findAllByOrderByNombreAsc().stream()
                .filter(r -> r.getNombre().equals("SUPER ADMINISTRADOR"))
                .findFirst().orElse(null);
        if (direccion != null && superAdmin != null) {
            UsuarioRequest admin = new UsuarioRequest();
            admin.setUsername("admin");
            admin.setPassword("Admin123");
            admin.setNombres("Fernando");
            admin.setApellidos("Correa");
            admin.setDni("70584692");
            admin.setCorreo("admin@hospital.com");
            admin.setTelefono("999999999");
            admin.setCargo("Administrador del sistema");
            admin.setAreaId(direccion.getId());
            admin.setRolId(superAdmin.getId());
            admin.setEstado("ACTIVO");
            usuarioService.crear(admin, null, "0.0.0.0");
        }

        Area consulta = areaRepository.findAllByOrderByNombreAsc().stream()
                .filter(a -> a.getNombre().equals("Consulta"))
                .findFirst().orElse(null);
        Rol medico = rolRepository.findAllByOrderByNombreAsc().stream()
                .filter(r -> r.getNombre().equals("Médico"))
                .findFirst().orElse(null);
        if (consulta != null && medico != null) {
            UsuarioRequest med = new UsuarioRequest();
            med.setUsername("medico");
            med.setPassword("Medico123");
            med.setNombres("Ana");
            med.setApellidos("Garcia");
            med.setDni("00000002");
            med.setCorreo("medico@hospital.com");
            med.setTelefono("988888888");
            med.setCargo("Medico general");
            med.setAreaId(consulta.getId());
            med.setRolId(medico.getId());
            med.setEstado("ACTIVO");
            usuarioService.crear(med, null, "0.0.0.0");
        }
    }
}