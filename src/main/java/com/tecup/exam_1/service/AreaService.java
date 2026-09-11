package com.tecup.exam_1.service;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.repository.AreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
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

    public Area crear(AreaRequest request) {
        if (areaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new NegocioException("Ya existe un área con el nombre " + request.getNombre());
        }
        Area area = new Area();
        aplicarDatos(area, request);
        return areaRepository.save(area);
    }

    public Area editar(Long id, AreaRequest request) {
        Area area = obtener(id);
        aplicarDatos(area, request);
        return areaRepository.save(area);
    }

    public void desactivar(Long id) {
        Area area = obtener(id);
        area.setEstado(Estado.INACTIVO);
    }

    public void activar(Long id) {
        Area area = obtener(id);
        area.setEstado(Estado.ACTIVO);
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