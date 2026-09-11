package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AuthService;
import com.tecup.exam_1.service.RolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AuthService authService;
    private final RolService rolService;
    private final SeguridadService seguridadService;

    public LoginController(AuthService authService, RolService rolService, SeguridadService seguridadService) {
        this.authService = authService;
        this.rolService = rolService;
        this.seguridadService = seguridadService;
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        if (seguridadService.estaAutenticado(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String username,
                                @RequestParam String password,
                                HttpServletRequest request,
                                Model model) {
        try {
            Usuario usuario = authService.login(username, password, seguridadService.ipActual(request));
            seguridadService.iniciarSesion(request.getSession(true), usuario, rolService.clavesPermisos(usuario.getRol()));
            return "redirect:/dashboard";
        } catch (NegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        seguridadService.cerrarSesion(session);
        return "redirect:/login";
    }
}