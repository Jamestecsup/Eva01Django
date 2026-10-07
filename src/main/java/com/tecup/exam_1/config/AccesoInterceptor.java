package com.tecup.exam_1.config;

import com.tecup.exam_1.seguridad.SeguridadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccesoInterceptor implements HandlerInterceptor {

    private final SeguridadService seguridadService;

    public AccesoInterceptor(SeguridadService seguridadService) {
        this.seguridadService = seguridadService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ruta = request.getRequestURI();
        if (ruta.startsWith("/recuperar") || ruta.startsWith("/restablecer") || ruta.equals("/login")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (!seguridadService.estaAutenticado(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // Sincroniza la sesion con la base de datos: los cambios hechos por un
        // administrador (permisos, rol, estado) se reflejan en esta misma peticion.
        if (!seguridadService.refrescarSesion(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String modulo = moduloDe(ruta);
        if (modulo != null && !seguridadService.tienePermiso(session, modulo, "VER")) {
            response.sendRedirect(request.getContextPath() + "/acceso-denegado");
            return false;
        }
        return true;
    }

    private String moduloDe(String ruta) {
        if (ruta.startsWith("/usuarios")) {
            return "USUARIOS";
        }
        if (ruta.startsWith("/roles")) {
            return "ROLES";
        }
        if (ruta.startsWith("/areas")) {
            return "AREAS";
        }
        if (ruta.startsWith("/auditoria")) {
            return "AUDITORIA";
        }
        if (ruta.startsWith("/dashboard") || ruta.equals("/") || ruta.equals("/perfil")) {
            return "DASHBOARD";
        }
        return null;
    }
}