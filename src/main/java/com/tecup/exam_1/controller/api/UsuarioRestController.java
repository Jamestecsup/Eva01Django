package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.*;
import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final SeguridadService seguridadService;

    public UsuarioRestController(UsuarioService usuarioService, SeguridadService seguridadService) {
        this.usuarioService = usuarioService;
        this.seguridadService = seguridadService;
    }

    @GetMapping
    public List<Usuario> listar(@RequestParam(required = false) String busqueda,
                                @RequestParam(required = false) String estado,
                                @RequestParam(required = false) Long rolId) {
        Estado estadoEnum = estado != null ? Estado.valueOf(estado) : null;
        return usuarioService.buscar(busqueda, estadoEnum, rolId);
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequest request, HttpServletRequest http) {
        Usuario creado = usuarioService.crear(request, null, seguridadService.ipActual(http));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public Usuario editar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request, HttpServletRequest http) {
        return usuarioService.editar(id, request, null, seguridadService.ipActual(http));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, HttpServletRequest http) {
        usuarioService.desactivar(id, null, seguridadService.ipActual(http));
        return ResponseEntity.ok(MensajeResponse.ok("Usuario desactivado correctamente"));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<MensajeResponse> activar(@PathVariable Long id, HttpServletRequest http) {
        usuarioService.activar(id, null, seguridadService.ipActual(http));
        return ResponseEntity.ok(MensajeResponse.ok("Usuario activado correctamente"));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<MensajeResponse> desactivar(@PathVariable Long id, HttpServletRequest http) {
        usuarioService.desactivar(id, null, seguridadService.ipActual(http));
        return ResponseEntity.ok(MensajeResponse.ok("Usuario desactivado correctamente"));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<MensajeResponse> cambiarPassword(@PathVariable Long id,
                                                           @Valid @RequestBody CambioPasswordRequest request,
                                                           HttpServletRequest http) {
        usuarioService.cambiarPassword(id, request.getPasswordActual(), request.getPasswordNueva(), null,
                seguridadService.ipActual(http));
        return ResponseEntity.ok(MensajeResponse.ok("Contraseña actualizada correctamente"));
    }

    @PutMapping("/{id}/password-admin")
    public ResponseEntity<MensajeResponse> restablecerPassword(@PathVariable Long id,
                                                               @RequestBody RestablecerRequest request,
                                                               HttpServletRequest http) {
        usuarioService.restablecerPassword(id, request.getPassword(), null, seguridadService.ipActual(http));
        return ResponseEntity.ok(MensajeResponse.ok("Contraseña restablecida correctamente"));
    }
}