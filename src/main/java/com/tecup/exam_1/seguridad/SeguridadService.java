package com.tecup.exam_1.seguridad;

import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Permiso;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeguridadService {

    public static final String ATTR_USUARIO = "usuarioSesion";
    public static final String ATTR_PERMISOS = "permisosSesion";

    private final UsuarioRepository usuarioRepository;

    public SeguridadService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void iniciarSesion(HttpSession session, Usuario usuario, Set<String> permisos) {
        session.setAttribute(ATTR_USUARIO, usuario);
        session.setAttribute(ATTR_PERMISOS, new HashSet<>(permisos));
    }

    /**
     * Recarga desde la base de datos el usuario y los permisos de la sesion, de modo
     * que los cambios realizados por un administrador (rol, permisos, estado, nombre)
     * se apliquen de inmediato y no recien al cerrar sesion.
     *
     * @return false cuando la sesion ya no es valida (usuario inexistente o desactivado),
     *         en cuyo caso la sesion es invalidada.
     */
    public boolean refrescarSesion(HttpSession session) {
        Usuario enSesion = usuarioActual(session);
        if (enSesion == null || enSesion.getId() == null) {
            return false;
        }

        Usuario actual = usuarioRepository.findById(enSesion.getId()).orElse(null);
        if (actual == null || actual.getEstado() == Estado.INACTIVO) {
            cerrarSesion(session);
            return false;
        }

        session.setAttribute(ATTR_USUARIO, actual);
        session.setAttribute(ATTR_PERMISOS, permisosDe(actual));
        return true;
    }

    private Set<String> permisosDe(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getEstado() == Estado.INACTIVO) {
            return new HashSet<>();
        }
        return usuario.getRol().getPermisos().stream()
                .filter(permiso -> permiso.getEstado() == null || permiso.getEstado() == Estado.ACTIVO)
                .map(Permiso::clave)
                .collect(Collectors.toCollection(HashSet::new));
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