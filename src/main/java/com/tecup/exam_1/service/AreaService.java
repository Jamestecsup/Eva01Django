package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.AreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AreaService {

    private final AreaRepository areaRepository;
    private final AuditoriaService auditoriaService;

    public AreaService(AreaRepository areaRepository, AuditoriaService auditoriaService) {
        this.areaRepository = areaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<Area> listar() {
        return areaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Area> listarActivas() {
        return areaRepository.findByEstado(Estado.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Area obtener(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el área con id " + id));
    }

    public Area crear(AreaRequest request, Usuario actor, String ip) {
        if (areaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe un área con el nombre " + request.getNombre());
        }
        Area area = new Area();
        aplicarDatos(area, request);
        area = areaRepository.save(area);
        auditar(actor, ip, "CREAR", "AREA", "Se creó el área " + area.getNombre());
        return area;
    }

    public Area editar(Long id, AreaRequest request, Usuario actor, String ip) {
        Area area = obtener(id);
        String nombreAnterior = area.getNombre();
        aplicarDatos(area, request);
        area = areaRepository.save(area);
        auditar(actor, ip, "EDITAR", "AREA", "Se actualizó el área: " + nombreAnterior + " → " + area.getNombre());
        return area;
    }

    public void desactivar(Long id, Usuario actor, String ip) {
        Area area = obtener(id);
        area.setEstado(Estado.INACTIVO);
        auditar(actor, ip, "DESACTIVAR", "AREA", "Se desactivó el área " + area.getNombre());
    }

    public void activar(Long id, Usuario actor, String ip) {
        Area area = obtener(id);
        area.setEstado(Estado.ACTIVO);
        auditar(actor, ip, "ACTIVAR", "AREA", "Se activó el área " + area.getNombre());
    }

    public void eliminar(Long id, Usuario actor, String ip) {
        Area area = obtener(id);
        String nombre = area.getNombre();
        areaRepository.delete(area);
        auditar(actor, ip, "ELIMINAR", "AREA", "Se eliminó permanentemente el área " + nombre);
    }

    private void auditar(Usuario actor, String ip, String accion, String entidad, String detalle) {
        auditoriaService.registrar(actor, accion, entidad, detalle, ip);
    }

    private void aplicarDatos(Area area, AreaRequest request) {
        area.setNombre(request.getNombre());
        area.setDescripcion(request.getDescripcion());
        if (request.getEstado() != null) {
            area.setEstado(Estado.valueOf(request.getEstado()));
        } else {
            area.setEstado(Estado.ACTIVO);
        }
    }
}