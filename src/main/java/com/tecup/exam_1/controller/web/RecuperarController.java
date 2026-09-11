package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.TokenRecuperacion;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.service.TokenRecuperacionService;
import com.tecup.exam_1.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecuperarController {

    private final UsuarioService usuarioService;
    private final TokenRecuperacionService tokenService;

    public RecuperarController(UsuarioService usuarioService, TokenRecuperacionService tokenService) {
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperar(Model model) {
        model.addAttribute("correo", "");
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitarRecuperacion(@RequestParam String correo, Model model, HttpServletRequest request) {
        try {
            Usuario usuario = usuarioService.listar().stream()
                    .filter(u -> u.getEmpleado() != null && correo.equalsIgnoreCase(u.getEmpleado().getCorreo()))
                    .findFirst()
                    .orElseThrow(() -> new NegocioException("El correo no está registrado en el sistema"));
            TokenRecuperacion token = tokenService.generarPara(usuario);
            String enlace = request.getRequestURL().toString().replace("/recuperar", "/restablecer")
                    + "?token=" + token.getToken();
            model.addAttribute("correo", correo);
            model.addAttribute("enlace", enlace);
            model.addAttribute("mensaje", "Se generó el enlace de recuperación. En un entorno real se enviaría al correo indicado.");
        } catch (NegocioException ex) {
            model.addAttribute("correo", correo);
            model.addAttribute("error", ex.getMessage());
        }
        return "recuperar";
    }

    @GetMapping("/restablecer")
    public String mostrarRestablecer(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("password", "");
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String restablecer(@RequestParam String token, @RequestParam String password, RedirectAttributes flash) {
        try {
            tokenService.restablecer(token, password);
            flash.addFlashAttribute("exito", "Contraseña restablecida correctamente. Ya puede iniciar sesión.");
            return "redirect:/login";
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/login";
        }
    }
}