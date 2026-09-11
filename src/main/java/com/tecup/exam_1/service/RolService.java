package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.RolRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.model.Rol;
import com.tecup.exam_1.repository.PermisoRepository;
import com.tecup.exam_1.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
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

    public Rol crear(RolRequest request) {
        if (rolRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe un rol con el nombre " + request.getNombre());
        }
        Rol rol = new Rol();
        aplicarDatos(rol, request);
        return rolRepository.save(rol);
    }

    public Rol editar(Long id, RolRequest request) {
        Rol rol = obtener(id);
        aplicarDatos(rol, request);
        return rolRepository.save(rol);
    }

    public Rol asignarPermisos(Long id, Set<Long> permisoIds) {
        Rol rol = obtener(id);
        if (permisoIds == null || permisoIds.isEmpty()) {
            rol.setPermisos(new LinkedHashSet<>());
            return rolRepository.save(rol);
        }
        Set<Permiso> permisos = new LinkedHashSet<>();
        permisoIds.forEach(permisoId -> permisos.add(permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el permiso con id " + permisoId))));
        rol.setPermisos(permisos);
        return rolRepository.save(rol);
    }

    public void desactivar(Long id) {
        Rol rol = obtener(id);
        rol.setEstado(Estado.INACTIVO);
    }

    public void activar(Long id) {
        Rol rol = obtener(id);
        rol.setEstado(Estado.ACTIVO);
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