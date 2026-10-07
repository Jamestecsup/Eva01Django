package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.dto.MensajeResponse;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AreaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final SeguridadService seguridadService;

    public AreaRestController(AreaService areaService, SeguridadService seguridadService) {
        this.areaService = areaService;
        this.seguridadService = seguridadService;
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
    public ResponseEntity<Area> crear(@Valid @RequestBody AreaRequest request, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.crear(request, actor, ip));
    }

    @PutMapping("/{id}")
    public Area editar(@PathVariable Long id, @Valid @RequestBody AreaRequest request, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        return areaService.editar(id, request, actor, ip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        areaService.eliminar(id, actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Área eliminada permanentemente"));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<MensajeResponse> desactivar(@PathVariable Long id, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        areaService.desactivar(id, actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Área desactivada correctamente"));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<MensajeResponse> activar(@PathVariable Long id, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        areaService.activar(id, actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Área activada correctamente"));
    }
}