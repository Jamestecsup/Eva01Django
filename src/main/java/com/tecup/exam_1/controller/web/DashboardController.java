package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AreaService;
import com.tecup.exam_1.service.AuditoriaService;
import com.tecup.exam_1.service.RolService;
import com.tecup.exam_1.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final SeguridadService seguridadService;
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final AreaService areaService;
    private final AuditoriaService auditoriaService;

    public DashboardController(SeguridadService seguridadService,
                               UsuarioService usuarioService,
                               RolService rolService,
                               AreaService areaService,
                               AuditoriaService auditoriaService) {
        this.seguridadService = seguridadService;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.areaService = areaService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = seguridadService.usuarioActual(session);
        model.addAttribute("usuario", usuario);
        if (seguridadService.tienePermiso(session, "USUARIOS", "VER")) {
            model.addAttribute("totalUsuarios", usuarioService.listar().size());
        }
        if (seguridadService.tienePermiso(session, "ROLES", "VER")) {
            model.addAttribute("totalRoles", rolService.listar().size());
        }
        if (seguridadService.tienePermiso(session, "AREAS", "VER")) {
            model.addAttribute("totalAreas", areaService.listarActivas().size());
        }
        if (seguridadService.tienePermiso(session, "AUDITORIA", "VER")) {
            model.addAttribute("totalAuditorias", auditoriaService.listarRecientes().size());
        }
        return "dashboard";
    }
}