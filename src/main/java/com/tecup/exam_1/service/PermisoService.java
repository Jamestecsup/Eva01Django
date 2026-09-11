package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.PermisoRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.repository.PermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PermisoService {

    private static final List<String> MODULOS = List.of(
            "DASHBOARD", "USUARIOS", "ROLES", "AREAS", "AUDITORIA"
    );

    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<Permiso> listar() {
        return permisoRepository.findAllByOrderByModuloAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, List<Permiso>> listarAgrupadosPorModulo() {
        Map<String, List<Permiso>> resultado = new LinkedHashMap<>();
        MODULOS.forEach(modulo -> resultado.put(modulo, new ArrayList<>()));
        permisoRepository.findAllByOrderByModuloAscNombreAsc()
                .forEach(permiso -> resultado.computeIfAbsent(permiso.getModulo(), k -> new ArrayList<>()).add(permiso));
        return resultado;
    }

    @Transactional(readOnly = true)
    public Permiso obtener(Long id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el permiso con id " + id));
    }

    public Permiso crear(PermisoRequest request) {
        validarUnico(request.getModulo(), request.getNombre(), null);
        Permiso permiso = new Permiso();
        permiso.setNombre(request.getNombre().toUpperCase());
        permiso.setModulo(request.getModulo().toUpperCase());
        permiso.setDescripcion(request.getDescripcion());
        permiso.setEstado(Estado.valueOf(request.getEstado() != null ? request.getEstado() : "ACTIVO"));
        return permisoRepository.save(permiso);
    }

    public Permiso editar(Long id, PermisoRequest request) {
        Permiso permiso = obtener(id);
        validarUnico(request.getModulo(), request.getNombre(), id);
        permiso.setNombre(request.getNombre().toUpperCase());
        permiso.setModulo(request.getModulo().toUpperCase());
        permiso.setDescripcion(request.getDescripcion());
        if (request.getEstado() != null) {
            permiso.setEstado(Estado.valueOf(request.getEstado()));
        }
        return permisoRepository.save(permiso);
    }

    private void validarUnico(String modulo, String nombre, Long idActual) {
        String moduloMayus = modulo.toUpperCase();
        String nombreMayus = nombre.toUpperCase();
        permisoRepository.findByModuloAndNombre(moduloMayus, nombreMayus)
                .ifPresent(existente -> {
                    if (idActual == null || !existente.getId().equals(idActual)) {
                        throw new NegocioException("Ya existe el permiso " + nombreMayus + " para el módulo " + moduloMayus);
                    }
                });
    }
}