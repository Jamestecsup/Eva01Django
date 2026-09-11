package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.MensajeResponse;
import com.tecup.exam_1.dto.PermisoRequest;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.service.PermisoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permisos")
public class PermisoRestController {

    private final PermisoService permisoService;

    public PermisoRestController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping
    public List<Permiso> listar() {
        return permisoService.listar();
    }

    @GetMapping("/modulos")
    public Map<String, List<Permiso>> listarPorModulo() {
        return permisoService.listarAgrupadosPorModulo();
    }

    @GetMapping("/{id}")
    public Permiso obtener(@PathVariable Long id) {
        return permisoService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Permiso> crear(@Valid @RequestBody PermisoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.crear(request));
    }

    @PutMapping("/{id}")
    public Permiso editar(@PathVariable Long id, @Valid @RequestBody PermisoRequest request) {
        return permisoService.editar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        permisoService.obtener(id);
        return ResponseEntity.ok(MensajeResponse.ok("Permiso eliminado correctamente"));
    }
}