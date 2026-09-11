package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.dto.AsignarPermisosRequest;
import com.tecup.exam_1.dto.RolRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Rol;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.PermisoService;
import com.tecup.exam_1.service.RolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;
    private final PermisoService permisoService;
    private final SeguridadService seguridadService;

    public RolController(RolService rolService, PermisoService permisoService, SeguridadService seguridadService) {
        this.rolService = rolService;
        this.permisoService = permisoService;
        this.seguridadService = seguridadService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        return "roles/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "ROLES", "CREAR")) {
            return "redirect:/acceso-denegado";
        }
        model.addAttribute("rol", new RolRequest());
        model.addAttribute("estados", Estado.values());
        model.addAttribute("titulo", "Nuevo rol");
        return "roles/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "ROLES", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        Rol rol = rolService.obtener(id);
        RolRequest request = new RolRequest();
        request.setId(rol.getId());
        request.setNombre(rol.getNombre());
        request.setDescripcion(rol.getDescripcion());
        request.setEstado(rol.getEstado().name());
        model.addAttribute("rol", request);
        model.addAttribute("estados", Estado.values());
        model.addAttribute("titulo", "Editar rol");
        return "roles/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("rol") RolRequest request,
                          HttpSession session,
                          RedirectAttributes flash) {
        try {
            if (request.getId() == null) {
                if (!seguridadService.tienePermiso(session, "ROLES", "CREAR")) {
                    return "redirect:/acceso-denegado";
                }
                Rol creado = rolService.crear(request);
                if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
                    rolService.asignarPermisos(creado.getId(), request.getPermisoIds());
                }
                flash.addFlashAttribute("exito", "Rol creado correctamente");
            } else {
                if (!seguridadService.tienePermiso(session, "ROLES", "EDITAR")) {
                    return "redirect:/acceso-denegado";
                }
                rolService.editar(request.getId(), request);
                flash.addFlashAttribute("exito", "Rol actualizado correctamente");
            }
            return "redirect:/roles";
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/roles" + (request.getId() == null ? "/nuevo" : "/editar/" + request.getId());
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "ROLES", "ELIMINAR")) {
            return "redirect:/acceso-denegado";
        }
        rolService.desactivar(id);
        flash.addFlashAttribute("exito", "Rol desactivado");
        return "redirect:/roles";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "ROLES", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        rolService.activar(id);
        flash.addFlashAttribute("exito", "Rol activado");
        return "redirect:/roles";
    }

    @GetMapping("/permisos/{id}")
    public String asignarPermisos(@PathVariable Long id, HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "ROLES", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        Rol rol = rolService.obtener(id);
        Set<Long> asignados = rol.getPermisos().stream().map(p -> p.getId()).collect(Collectors.toSet());
        AsignarPermisosRequest request = new AsignarPermisosRequest();
        request.setPermisoIds(asignados);
        model.addAttribute("rol", rol);
        model.addAttribute("asignacion", request);
        model.addAttribute("permisosPorModulo", permisoService.listarAgrupadosPorModulo());
        return "roles/permisos";
    }

    @PostMapping("/permisos/{id}")
    public String guardarPermisos(@PathVariable Long id,
                                  @ModelAttribute("asignacion") AsignarPermisosRequest request,
                                  HttpSession session,
                                  RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "ROLES", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        rolService.asignarPermisos(id, request.getPermisoIds() != null ? request.getPermisoIds() : Set.of());
        flash.addFlashAttribute("exito", "Permisos asignados correctamente");
        return "redirect:/roles";
    }
}