package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.RolRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.model.Rol;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.PermisoRepository;
import com.tecup.exam_1.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository, AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<Rol> listar() {
        return rolRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Rol> listarActivos() {
        return rolRepository.findByEstado(Estado.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el rol con id " + id));
    }

    public Rol crear(RolRequest request, Usuario actor, String ip) {
        if (rolRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe un rol con el nombre " + request.getNombre());
        }
        Rol rol = new Rol();
        aplicarDatos(rol, request);
        rol = rolRepository.save(rol);
        auditar(actor, ip, "CREAR", "ROL", "Se creó el rol " + rol.getNombre());
        return rol;
    }

    public Rol editar(Long id, RolRequest request, Usuario actor, String ip) {
        Rol rol = obtener(id);
        String nombreAnterior = rol.getNombre();
        Estado estadoAnterior = rol.getEstado();
        String descripcionAnterior = rol.getDescripcion();
        
        aplicarDatos(rol, request);
        rol = rolRepository.save(rol);
        
        StringBuilder detalle = new StringBuilder("Se actualizó el rol: " + nombreAnterior + " → " + rol.getNombre());
        if (!Objects.equals(descripcionAnterior, rol.getDescripcion())) {
            detalle.append(". Descripción: '").append(descripcionAnterior != null ? descripcionAnterior : "").append("' → '").append(rol.getDescripcion() != null ? rol.getDescripcion() : "").append("'");
        }
        if (estadoAnterior != rol.getEstado()) {
            detalle.append(". Estado: ").append(estadoAnterior).append(" → ").append(rol.getEstado());
        }
        
        auditar(actor, ip, "EDITAR", "ROL", detalle.toString());
        return rol;
    }

    public Rol asignarPermisos(Long id, Set<Long> permisoIds, Usuario actor, String ip) {
        Rol rol = obtener(id);
        
        // Obtener claves de permisos actuales antes del cambio
        Set<String> permisosAnteriores = rol.getPermisos().stream()
                .map(Permiso::clave)
                .collect(Collectors.toSet());
        
        Set<Permiso> nuevosPermisos = new LinkedHashSet<>();
        if (permisoIds != null && !permisoIds.isEmpty()) {
            permisoIds.forEach(permisoId -> nuevosPermisos.add(permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el permiso con id " + permisoId))));
        }
        
        // Calcular diferencias
        Set<String> permisosNuevos = nuevosPermisos.stream().map(Permiso::clave).collect(Collectors.toSet());
        Set<String> agregados = new LinkedHashSet<>(permisosNuevos);
        agregados.removeAll(permisosAnteriores);
        
        Set<String> removidos = new LinkedHashSet<>(permisosAnteriores);
        removidos.removeAll(permisosNuevos);
        
        // Aplicar cambios
        rol.setPermisos(nuevosPermisos);
        rol = rolRepository.save(rol);
        
        // Auditoría detallada
        StringBuilder detalle = new StringBuilder("Permisos actualizados en rol " + rol.getNombre());
        if (!agregados.isEmpty()) {
            detalle.append(". Agregados: ").append(String.join(", ", agregados));
        }
        if (!removidos.isEmpty()) {
            detalle.append(". Removidos: ").append(String.join(", ", removidos));
        }
        if (agregados.isEmpty() && removidos.isEmpty()) {
            detalle.append(" (sin cambios)");
        }
        
        auditar(actor, ip, "EDITAR", "ROL", detalle.toString());
        return rol;
    }

    public void desactivar(Long id, Usuario actor, String ip) {
        Rol rol = obtener(id);
        rol.setEstado(Estado.INACTIVO);
        auditar(actor, ip, "DESACTIVAR", "ROL", "Se desactivó el rol " + rol.getNombre());
    }

    public void activar(Long id, Usuario actor, String ip) {
        Rol rol = obtener(id);
        rol.setEstado(Estado.ACTIVO);
        auditar(actor, ip, "ACTIVAR", "ROL", "Se activó el rol " + rol.getNombre());
    }

    private void auditar(Usuario actor, String ip, String accion, String entidad, String detalle) {
        auditoriaService.registrar(actor, accion, entidad, detalle, ip);
    }

    @Transactional(readOnly = true)
    public Set<String> clavesPermisos(Rol rol) {
        return rol.getPermisos().stream()
                .map(Permiso::clave)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean tienePermiso(Rol rol, String modulo, String nombrePermiso) {
        return rol.getPermisos().stream()
                .anyMatch(p -> p.getModulo().equalsIgnoreCase(modulo)
                        && p.getNombre().equalsIgnoreCase(nombrePermiso));
    }

    private void aplicarDatos(Rol rol, RolRequest request) {
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        rol.setEstado(Estado.valueOf(request.getEstado() != null ? request.getEstado() : "ACTIVO"));
    }
}