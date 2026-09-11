package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.dto.*;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.TokenRecuperacion;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AuthService;
import com.tecup.exam_1.service.RolService;
import com.tecup.exam_1.service.TokenRecuperacionService;
import com.tecup.exam_1.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final TokenRecuperacionService tokenService;
    private final SeguridadService seguridadService;

    public AuthRestController(AuthService authService,
                              UsuarioService usuarioService,
                              RolService rolService,
                              TokenRecuperacionService tokenService,
                              SeguridadService seguridadService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.tokenService = tokenService;
        this.seguridadService = seguridadService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        Usuario usuario = authService.login(request.getUsername(), request.getPassword(),
                seguridadService.ipActual(http));
        Set<String> permisos = rolService.clavesPermisos(usuario.getRol());
        LoginResponse respuesta = new LoginResponse();
        respuesta.setId(usuario.getId());
        respuesta.setUsername(usuario.getUsername());
        respuesta.setNombreCompleto(usuario.nombreCompleto());
        respuesta.setRol(usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        respuesta.setArea(usuario.getEmpleado() != null && usuario.getEmpleado().getArea() != null
                ? usuario.getEmpleado().getArea().getNombre() : null);
        respuesta.setMensaje("Sesión iniciada correctamente");
        respuesta.setPermisos(new ArrayList<>(permisos));
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> recuperar(@Valid @RequestBody RecuperarRequest request, HttpServletRequest http) {
        return usuarioService.listar().stream()
                .filter(usuario -> usuario.getEmpleado() != null
                        && request.getCorreo().equalsIgnoreCase(usuario.getEmpleado().getCorreo()))
                .findFirst()
                .map(usuario -> {
                    TokenRecuperacion token = tokenService.generarPara(usuario);
                    return ResponseEntity.ok(MensajeResponse.ok("Se generó el enlace de recuperación. Token: " + token.getToken()));
                })
                .orElseThrow(() -> new NegocioException("El correo no está registrado en el sistema"));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerRequest request) {
        tokenService.restablecer(request.getToken(), request.getPassword());
        return ResponseEntity.ok(MensajeResponse.ok("Contraseña restablecida correctamente"));
    }
}