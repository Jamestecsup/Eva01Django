package com.tecup.exam_1.service;

import com.tecup.exam_1.exception.NegocioException;
import com.tecup.exam_1.model.TokenRecuperacion;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.TokenRecuperacionRepository;
import com.tecup.exam_1.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class TokenRecuperacionService {

    private static final long HORAS_DE_VALIDEZ = 24;

    private final TokenRecuperacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;

    public TokenRecuperacionService(TokenRecuperacionRepository tokenRepository,
                                    UsuarioRepository usuarioRepository,
                                    AuditoriaService auditoriaService,
                                    PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenRecuperacion generarPara(Usuario usuarioOrigen) {
        Usuario usuario = usuarioRepository.findById(usuarioOrigen.getId())
                .orElseThrow(() -> new NegocioException("El usuario no existe"));
        tokenRepository.deleteByUsuarioId(usuario.getId());
        TokenRecuperacion token = new TokenRecuperacion();
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setUsuario(usuario);
        token.setFechaExpiracion(LocalDateTime.now().plusHours(HORAS_DE_VALIDEZ));
        token.setUtilizado(false);
        auditoriaService.registrar(usuario, "SOLICITAR_RECUPERACION", "USUARIO",
                "Se solicitó la recuperación de contraseña del usuario " + usuario.getUsername(), null);
        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Usuario validarToken(String valorToken) {
        return tokenRepository.findByToken(valorToken)
                .map(TokenRecuperacion::getUsuario)
                .orElseThrow(() -> new NegocioException("El enlace no es válido"));
    }

    public void restablecer(String valorToken, String nuevaPassword) {
        TokenRecuperacion token = tokenRepository.findByToken(valorToken)
                .orElseThrow(() -> new NegocioException("El enlace no es válido"));
        if (!token.esValido()) {
            throw new NegocioException("El enlace ha expirado o ya fue utilizado");
        }
        Usuario usuario = token.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        token.setUtilizado(true);
        auditoriaService.registrar(usuario, "RESTABLECER_PASSWORD", "USUARIO",
                "Se restableció la contraseña del usuario " + usuario.getUsername(), null);
    }
}