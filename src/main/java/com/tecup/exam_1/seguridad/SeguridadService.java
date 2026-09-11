package com.tecup.exam_1.seguridad;

import com.tecup.exam_1.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class SeguridadService {

    public static final String ATTR_USUARIO = "usuarioSesion";
    public static final String ATTR_PERMISOS = "permisosSesion";

    public void iniciarSesion(HttpSession session, Usuario usuario, Set<String> permisos) {
        session.setAttribute(ATTR_USUARIO, usuario);
        session.setAttribute(ATTR_PERMISOS, new HashSet<>(permisos));
    }

    public void cerrarSesion(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public boolean estaAutenticado(HttpSession session) {
        return session != null && session.getAttribute(ATTR_USUARIO) != null;
    }

    public Usuario usuarioActual(HttpSession session) {
        return session == null ? null : (Usuario) session.getAttribute(ATTR_USUARIO);
    }

    @SuppressWarnings("unchecked")
    public Set<String> permisosActuales(HttpSession session) {
        if (session == null) {
            return Collections.emptySet();
        }
        Object permisos = session.getAttribute(ATTR_PERMISOS);
        return permisos instanceof Set ? (Set<String>) permisos : Collections.emptySet();
    }

    public boolean tienePermiso(HttpSession session, String modulo, String accion) {
        return permisosActuales(session).contains((modulo + ":" + accion).toUpperCase());
    }

    public String ipActual(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}