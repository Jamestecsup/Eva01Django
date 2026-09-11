package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.dto.AreaRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AreaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/areas")
public class AreaController {

    private final AreaService areaService;
    private final SeguridadService seguridadService;

    public AreaController(AreaService areaService, SeguridadService seguridadService) {
        this.areaService = areaService;
        this.seguridadService = seguridadService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("areas", areaService.listar());
        return "areas/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "AREAS", "CREAR")) {
            return "redirect:/acceso-denegado";
        }
        model.addAttribute("area", new AreaRequest());
        model.addAttribute("estados", Estado.values());
        model.addAttribute("titulo", "Nueva área");
        return "areas/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "AREAS", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        Area area = areaService.obtener(id);
        AreaRequest request = new AreaRequest();
        request.setId(area.getId());
        request.setNombre(area.getNombre());
        request.setDescripcion(area.getDescripcion());
        request.setEstado(area.getEstado().name());
        model.addAttribute("area", request);
        model.addAttribute("estados", Estado.values());
        model.addAttribute("titulo", "Editar área");
        return "areas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("area") AreaRequest request,
                          HttpSession session,
                          RedirectAttributes flash) {
        try {
            if (request.getId() == null) {
                if (!seguridadService.tienePermiso(session, "AREAS", "CREAR")) {
                    return "redirect:/acceso-denegado";
                }
                areaService.crear(request);
                flash.addFlashAttribute("exito", "Área creada correctamente");
            } else {
                if (!seguridadService.tienePermiso(session, "AREAS", "EDITAR")) {
                    return "redirect:/acceso-denegado";
                }
                areaService.editar(request.getId(), request);
                flash.addFlashAttribute("exito", "Área actualizada correctamente");
            }
            return "redirect:/areas";
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/areas" + (request.getId() == null ? "/nuevo" : "/editar/" + request.getId());
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "AREAS", "ELIMINAR")) {
            return "redirect:/acceso-denegado";
        }
        areaService.desactivar(id);
        flash.addFlashAttribute("exito", "Área desactivada");
        return "redirect:/areas";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "AREAS", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        areaService.activar(id);
        flash.addFlashAttribute("exito", "Área activada");
        return "redirect:/areas";
    }
}