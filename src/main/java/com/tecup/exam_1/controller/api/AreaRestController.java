package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.dto.MensajeResponse;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.service.AreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/areas")
public class AreaRestController {

    private final AreaService areaService;

    public AreaRestController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public List<Area> listar(@RequestParam(required = false) String estado) {
        if (estado == null) {
            return areaService.listar();
        }
        List<Area> areas = areaService.listar();
        return areas.stream().filter(a -> a.getEstado() == Estado.valueOf(estado)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Area obtener(@PathVariable Long id) {
        return areaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Area> crear(@Valid @RequestBody AreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.crear(request));
    }

    @PutMapping("/{id}")
    public Area editar(@PathVariable Long id, @Valid @RequestBody AreaRequest request) {
        return areaService.editar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        areaService.desactivar(id);
        return ResponseEntity.ok(MensajeResponse.ok("Área desactivada correctamente"));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<MensajeResponse> activar(@PathVariable Long id) {
        areaService.activar(id);
        return ResponseEntity.ok(MensajeResponse.ok("Área activada correctamente"));
    }
}