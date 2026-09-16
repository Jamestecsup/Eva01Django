package com.tecup.exam_1.service;

import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class AuthService {

    private static final int MAX_INTENTOS = 3;
    private static final long MINUTOS_BLOQUEO = 30;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public Usuario login(String identificador, String password, String ip) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(identificador)
                .or(() -> usuarioRepository.findByEmpleadoCorreoIgnoreCase(identificador))
                .orElseThrow(() -> new NegocioException("Usuario o contraseña incorrectos"));

        if (usuario.getEstado() == Estado.INACTIVO) {
            throw new NegocioException("La cuenta está desactivada. Contacte con el administrador");
        }
        if (estaBloqueado(usuario)) {
            throw new NegocioException("Cuenta bloqueada temporalmente hasta "
                    + usuario.getBloqueadoHasta().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            registrarIntentoFallido(usuario, ip);
            int intentos = usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0;
            if (intentos >= MAX_INTENTOS) {
                String mensaje = "Cuenta bloqueada por intentos fallidos. Podrá intentar nuevamente a las "
                        + usuario.getBloqueadoHasta().format(DateTimeFormatter.ofPattern("HH:mm"));
                throw new NegocioException(mensaje);
            }
            throw new NegocioException("Usuario o contraseña incorrectos");
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario, "LOGIN", "USUARIO",
                "Inicio de sesión exitoso del usuario " + usuario.getUsername(), ip);
        return usuario;
    }

    private void registrarIntentoFallido(Usuario usuario, String ip) {
        int intentos = (usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0) + 1;
        usuario.setIntentosFallidos(intentos);
        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario, "INTENTO_FALLIDO", "USUARIO",
                "Intento de inicio de sesión fallido (" + intentos + "/" + MAX_INTENTOS
                        + ") para el usuario " + usuario.getUsername(), ip);
    }

    public boolean estaBloqueado(Usuario usuario) {
        return usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now());
    }
}