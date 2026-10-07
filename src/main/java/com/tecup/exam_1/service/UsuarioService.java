package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.UsuarioRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.*;
import com.tecup.exam_1.repository.EmpleadoRepository;
import com.tecup.exam_1.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RolService rolService;
    private final AreaService areaService;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmpleadoRepository empleadoRepository,
                          RolService rolService,
                          AreaService areaService,
                          AuditoriaService auditoriaService,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.rolService = rolService;
        this.areaService = areaService;
        this.auditoriaService = auditoriaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscar(String busqueda, Estado estado, Long rolId) {
        return listar().stream()
                .filter(usuario -> (estado == null || usuario.getEstado() == estado))
                .filter(usuario -> (rolId == null || (usuario.getRol() != null && usuario.getRol().getId().equals(rolId))))
                .filter(usuario -> busqueda == null || busqueda.isBlank() || coincide(usuario, busqueda))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con id " + id));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario " + username));
    }

    public Usuario crear(UsuarioRequest request, Usuario actor, String ip) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new NegocioException("La contraseña es obligatoria al crear un usuario");
        }
        if (request.getRolId() == null || request.getAreaId() == null) {
            throw new NegocioException("Debe seleccionar un rol y un área");
        }
        if (usuarioRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new NegocioException("Ya existe un usuario con el nombre de usuario " + request.getUsername());
        }
        Rol rol = rolService.obtener(request.getRolId());
        Area area = areaService.obtener(request.getAreaId());

        Empleado empleado = empleadoRepository.findByDni(request.getDni()).orElse(null);
        if (empleado != null && empleado.getUsuario() != null) {
            throw new NegocioException("El DNI " + request.getDni() + " ya tiene una cuenta de usuario");
        }
        if (empleado == null && empleadoRepository.existsByDni(request.getDni())) {
            throw new NegocioException("Ya existe un empleado con el DNI " + request.getDni());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()
                && (empleado == null || !request.getCorreo().equalsIgnoreCase(empleado.getCorreo()))
                && empleadoRepository.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new NegocioException("Ya existe un empleado con el correo " + request.getCorreo());
        }

        if (empleado == null) {
            empleado = new Empleado();
            empleado.setEstado(Estado.ACTIVO);
        }
        aplicarDatosEmpleado(empleado, request, area);

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(Estado.valueOf(request.getEstado() != null ? request.getEstado() : "ACTIVO"));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuario.setRol(rol);
        usuario.setEmpleado(empleado);
        empleado.setUsuario(usuario);

        usuario = usuarioRepository.save(usuario);
        empleadoRepository.save(empleado);
        auditar(actor, ip, "CREAR", "USUARIO", "Se creó el usuario " + usuario.getUsername());
        return usuario;
    }

    public Usuario editar(Long id, UsuarioRequest request, Usuario actor, String ip) {
        Usuario usuario = obtener(id);
        if (request.getRolId() == null || request.getAreaId() == null) {
            throw new NegocioException("Debe seleccionar un rol y un área");
        }

        // Capturar valores anteriores
        String usernameAnterior = usuario.getUsername();
        Rol rolAnterior = usuario.getRol();
        Estado estadoAnterior = usuario.getEstado();
        Empleado empleado = usuario.getEmpleado();
        String nombresAnterior = empleado != null ? empleado.getNombres() : null;
        String apellidosAnterior = empleado != null ? empleado.getApellidos() : null;
        String dniAnterior = empleado != null ? empleado.getDni() : null;
        String correoAnterior = empleado != null ? empleado.getCorreo() : null;
        String telefonoAnterior = empleado != null ? empleado.getTelefono() : null;
        String cargoAnterior = empleado != null ? empleado.getCargo() : null;
        Area areaAnterior = empleado != null ? empleado.getArea() : null;

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String nuevoUsername = request.getUsername().trim().toLowerCase();
            if (!nuevoUsername.equalsIgnoreCase(usuario.getUsername())
                    && usuarioRepository.existsByUsernameIgnoreCase(nuevoUsername)) {
                throw new NegocioException("Ya existe un usuario con el nombre de usuario " + nuevoUsername);
            }
            usuario.setUsername(nuevoUsername);
        }
        if (empleado == null) {
            empleado = new Empleado();
            empleado.setEstado(Estado.ACTIVO);
            empleado.setUsuario(usuario);
        }
        if (empleado.getUsuario() != null && !empleado.getUsuario().getId().equals(usuario.getId())) {
            throw new NegocioException("El empleado vinculado pertenece a otra cuenta de usuario");
        }
        Area areaNueva = areaService.obtener(request.getAreaId());
        aplicarDatosEmpleado(empleado, request, areaNueva);
        Rol rolNuevo = rolService.obtener(request.getRolId());
        usuario.setRol(rolNuevo);
        if (request.getEstado() != null) {
            usuario.setEstado(Estado.valueOf(request.getEstado()));
        }
        usuario = usuarioRepository.save(usuario);
        empleadoRepository.save(empleado);

        // Construir detalle de auditoría específico
        StringBuilder detalle = new StringBuilder("Se actualizó el usuario " + usuario.getUsername());
        List<String> cambios = new ArrayList<>();

        if (!Objects.equals(usernameAnterior, usuario.getUsername())) {
            cambios.add("Username: " + usernameAnterior + " → " + usuario.getUsername());
        }
        if (!Objects.equals(rolAnterior, usuario.getRol())) {
            cambios.add("Rol: " + (rolAnterior != null ? rolAnterior.getNombre() : "null") + " → " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "null"));
        }
        if (estadoAnterior != usuario.getEstado()) {
            cambios.add("Estado: " + estadoAnterior + " → " + usuario.getEstado());
        }
        if (empleado != null) {
            if (!Objects.equals(nombresAnterior, empleado.getNombres())) {
                cambios.add("Nombres: " + (nombresAnterior != null ? nombresAnterior : "") + " → " + (empleado.getNombres() != null ? empleado.getNombres() : ""));
            }
            if (!Objects.equals(apellidosAnterior, empleado.getApellidos())) {
                cambios.add("Apellidos: " + (apellidosAnterior != null ? apellidosAnterior : "") + " → " + (empleado.getApellidos() != null ? empleado.getApellidos() : ""));
            }
            if (!Objects.equals(dniAnterior, empleado.getDni())) {
                cambios.add("DNI: " + (dniAnterior != null ? dniAnterior : "") + " → " + (empleado.getDni() != null ? empleado.getDni() : ""));
            }
            if (!Objects.equals(correoAnterior, empleado.getCorreo())) {
                cambios.add("Correo: " + (correoAnterior != null ? correoAnterior : "") + " → " + (empleado.getCorreo() != null ? empleado.getCorreo() : ""));
            }
            if (!Objects.equals(telefonoAnterior, empleado.getTelefono())) {
                cambios.add("Teléfono: " + (telefonoAnterior != null ? telefonoAnterior : "") + " → " + (empleado.getTelefono() != null ? empleado.getTelefono() : ""));
            }
            if (!Objects.equals(cargoAnterior, empleado.getCargo())) {
                cambios.add("Cargo: " + (cargoAnterior != null ? cargoAnterior : "") + " → " + (empleado.getCargo() != null ? empleado.getCargo() : ""));
            }
            if (!Objects.equals(areaAnterior, empleado.getArea())) {
                cambios.add("Área: " + (areaAnterior != null ? areaAnterior.getNombre() : "null") + " → " + (empleado.getArea() != null ? empleado.getArea().getNombre() : "null"));
            }
        }

        if (!cambios.isEmpty()) {
            detalle.append(". ").append(String.join("; ", cambios));
        } else {
            detalle.append(" (sin cambios)");
        }

        auditar(actor, ip, "EDITAR", "USUARIO", detalle.toString());
        return usuario;
    }

    public void desactivar(Long id, Usuario actor, String ip) {
        Usuario usuario = obtener(id);
        if (usuario.equals(actor)) {
            throw new NegocioException("No se puede desactivar la cuenta en uso");
        }
        usuario.setEstado(Estado.INACTIVO);
        auditar(actor, ip, "DESACTIVAR", "USUARIO", "Se desactivó el usuario " + usuario.getUsername());
    }

    public void activar(Long id, Usuario actor, String ip) {
        Usuario usuario = obtener(id);
        usuario.setEstado(Estado.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        auditar(actor, ip, "ACTIVAR", "USUARIO", "Se activó el usuario " + usuario.getUsername());
    }

    public void cambiarPassword(Long id, String passwordActual, String passwordNueva, Usuario actor, String ip) {
        Usuario usuario = obtener(id);
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new NegocioException("La contraseña actual no es correcta");
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        auditar(actor, ip, "CAMBIAR_PASSWORD", "USUARIO", "Se cambió la contraseña del usuario " + usuario.getUsername());
    }

    public void restablecerPassword(Long id, String passwordNueva, Usuario actor, String ip) {
        Usuario usuario = obtener(id);
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        auditar(actor, ip, "RESTABLECER_PASSWORD", "USUARIO", "Se restableció la contraseña del usuario " + usuario.getUsername());
    }

    private void aplicarDatosEmpleado(Empleado empleado, UsuarioRequest request, Area area) {
        empleado.setNombres(request.getNombres());
        empleado.setApellidos(request.getApellidos());
        empleado.setDni(request.getDni());
        empleado.setCorreo(request.getCorreo());
        empleado.setTelefono(request.getTelefono());
        empleado.setCargo(request.getCargo());
        empleado.setArea(area);
    }

    private boolean coincide(Usuario usuario, String busqueda) {
        String termino = busqueda.trim().toLowerCase();
        Empleado empleado = usuario.getEmpleado();
        return usuario.getUsername().toLowerCase().contains(termino)
                || (empleado != null && (empleado.getNombres().toLowerCase().contains(termino)
                || empleado.getApellidos().toLowerCase().contains(termino)
                || (empleado.getDni() != null && empleado.getDni().contains(termino))
                || (empleado.getCorreo() != null && empleado.getCorreo().toLowerCase().contains(termino))));
    }

    private void auditar(Usuario actor, String ip, String accion, String entidad, String detalle) {
        auditoriaService.registrar(actor, accion, entidad, detalle, ip);
    }
}