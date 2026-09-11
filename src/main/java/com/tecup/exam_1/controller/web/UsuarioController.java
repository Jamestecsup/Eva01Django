package com.tecup.exam_1.controller.web;

import com.tecup.exam_1.dto.CambioPasswordRequest;
import com.tecup.exam_1.dto.UsuarioRequest;
import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.seguridad.SeguridadService;
import com.tecup.exam_1.service.AreaService;
import com.tecup.exam_1.service.RolService;
import com.tecup.exam_1.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final AreaService areaService;
    private final SeguridadService seguridadService;

    public UsuarioController(UsuarioService usuarioService,
                             RolService rolService,
                             AreaService areaService,
                             SeguridadService seguridadService) {
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.areaService = areaService;
        this.seguridadService = seguridadService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busqueda,
                         @RequestParam(required = false) String estado,
                         @RequestParam(required = false) Long rolId,
                         Model model) {
        model.addAttribute("usuarios", usuarioService.buscar(busqueda, estado != null ? Estado.valueOf(estado) : null, rolId));
        model.addAttribute("roles", rolService.listarActivos());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estado", estado);
        model.addAttribute("rolId", rolId);
        return "usuarios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "USUARIOS", "CREAR")) {
            return "redirect:/acceso-denegado";
        }
        prepararFormulario(model, new UsuarioRequest());
        model.addAttribute("titulo", "Nuevo usuario");
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, HttpSession session, Model model) {
        if (!seguridadService.tienePermiso(session, "USUARIOS", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        Usuario usuario = usuarioService.obtener(id);
        prepararFormulario(model, toRequest(usuario));
        model.addAttribute("titulo", "Editar usuario");
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") UsuarioRequest request,
                          HttpSession session,
                          HttpServletRequest http,
                          RedirectAttributes flash) {
        try {
            Usuario actor = seguridadService.usuarioActual(session);
            String ip = seguridadService.ipActual(http);
            if (request.getId() == null) {
                if (!seguridadService.tienePermiso(session, "USUARIOS", "CREAR")) {
                    return "redirect:/acceso-denegado";
                }
                usuarioService.crear(request, actor, ip);
                flash.addFlashAttribute("exito", "Usuario creado correctamente");
            } else {
                if (!seguridadService.tienePermiso(session, "USUARIOS", "EDITAR")) {
                    return "redirect:/acceso-denegado";
                }
                usuarioService.editar(request.getId(), request, actor, ip);
                flash.addFlashAttribute("exito", "Usuario actualizado correctamente");
            }
            return "redirect:/usuarios";
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/usuarios" + (request.getId() == null ? "/nuevo" : "/editar/" + request.getId());
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, HttpServletRequest http, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "USUARIOS", "ELIMINAR")) {
            return "redirect:/acceso-denegado";
        }
        try {
            usuarioService.desactivar(id, seguridadService.usuarioActual(session), seguridadService.ipActual(http));
            flash.addFlashAttribute("exito", "Usuario desactivado");
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, HttpSession session, HttpServletRequest http, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "USUARIOS", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        usuarioService.activar(id, seguridadService.usuarioActual(session), seguridadService.ipActual(http));
        flash.addFlashAttribute("exito", "Usuario activado");
        return "redirect:/usuarios";
    }

    @PostMapping("/restablecer-password/{id}")
    public String restablecerPassword(@PathVariable Long id, @RequestParam String passwordNueva,
                                      HttpSession session, HttpServletRequest http, RedirectAttributes flash) {
        if (!seguridadService.tienePermiso(session, "USUARIOS", "EDITAR")) {
            return "redirect:/acceso-denegado";
        }
        try {
            usuarioService.restablecerPassword(id, passwordNueva, seguridadService.usuarioActual(session),
                    seguridadService.ipActual(http));
            flash.addFlashAttribute("exito", "Contraseña restablecida correctamente");
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Usuario usuario = seguridadService.usuarioActual(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("cambioPassword", new CambioPasswordRequest());
        return "usuarios/perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(@ModelAttribute("cambioPassword") CambioPasswordRequest request,
                                  HttpSession session, HttpServletRequest http, RedirectAttributes flash) {
        try {
            Usuario usuario = seguridadService.usuarioActual(session);
            usuarioService.cambiarPassword(usuario.getId(), request.getPasswordActual(), request.getPasswordNueva(),
                    usuario, seguridadService.ipActual(http));
            flash.addFlashAttribute("exito", "Contraseña actualizada correctamente");
        } catch (NegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios/perfil";
    }

    private void prepararFormulario(Model model, UsuarioRequest request) {
        model.addAttribute("usuario", request);
        model.addAttribute("roles", rolService.listarActivos());
        model.addAttribute("areas", areaService.listarActivas());
        model.addAttribute("estados", Estado.values());
    }

    private UsuarioRequest toRequest(Usuario usuario) {
        UsuarioRequest request = new UsuarioRequest();
        request.setId(usuario.getId());
        request.setUsername(usuario.getUsername());
        request.setNombres(usuario.getEmpleado() != null ? usuario.getEmpleado().getNombres() : null);
        request.setApellidos(usuario.getEmpleado() != null ? usuario.getEmpleado().getApellidos() : null);
        request.setDni(usuario.getEmpleado() != null ? usuario.getEmpleado().getDni() : null);
        request.setCorreo(usuario.getEmpleado() != null ? usuario.getEmpleado().getCorreo() : null);
        request.setTelefono(usuario.getEmpleado() != null ? usuario.getEmpleado().getTelefono() : null);
        request.setCargo(usuario.getEmpleado() != null ? usuario.getEmpleado().getCargo() : null);
        request.setAreaId(usuario.getEmpleado() != null && usuario.getEmpleado().getArea() != null
                ? usuario.getEmpleado().getArea().getId() : null);
        request.setRolId(usuario.getRol() != null ? usuario.getRol().getId() : null);
        request.setEstado(usuario.getEstado().name());
        return request;
    }
}