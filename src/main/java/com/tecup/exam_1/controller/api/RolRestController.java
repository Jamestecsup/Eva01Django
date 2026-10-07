package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.AsignarPermisosRequest;
import com.tecup.exam_1.dto.MensajeResponse;
import com.tecup.exam_1.dto.RolRequest;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Rol;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.RolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    private final RolService rolService;
    private final SeguridadService seguridadService;

    public RolRestController(RolService rolService, SeguridadService seguridadService) {
        this.rolService = rolService;
        this.seguridadService = seguridadService;
    }

    @GetMapping
    public List<Rol> listar(@RequestParam(required = false) String estado) {
        if (estado == null) {
            return rolService.listar();
        }
        Estado estadoEnum = Estado.valueOf(estado);
        return rolService.listar().stream()
                .filter(rol -> rol.getEstado() == estadoEnum)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Rol obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }

    @GetMapping("/{id}/permisos")
    public List<Long> permisosDelRol(@PathVariable Long id) {
        return rolService.obtener(id).getPermisos().stream()
                .map(p -> p.getId())
                .sorted()
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Rol> crear(@Valid @RequestBody RolRequest request, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(request, actor, ip));
    }

    @PutMapping("/{id}")
    public Rol editar(@PathVariable Long id, @Valid @RequestBody RolRequest request, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        return rolService.editar(id, request, actor, ip);
    }

    @PutMapping("/{id}/permisos")
    public ResponseEntity<MensajeResponse> asignarPermisos(@PathVariable Long id,
                                                           @RequestBody AsignarPermisosRequest request,
                                                           HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        rolService.asignarPermisos(id, request.getPermisoIds(), actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Permisos asignados correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        rolService.desactivar(id, actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Rol desactivado correctamente"));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<MensajeResponse> activar(@PathVariable Long id, HttpServletRequest http, HttpSession session) {
        Usuario actor = seguridadService.usuarioActual(session);
        String ip = seguridadService.ipActual(http);
        rolService.activar(id, actor, ip);
        return ResponseEntity.ok(MensajeResponse.ok("Rol activado correctamente"));
    }
}