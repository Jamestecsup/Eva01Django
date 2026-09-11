package com.tecup.exam_1.service;

import com.tecup.exam_1.exception.RecursoNoEncontradoException;
import com.tecup.exam_1.model.Auditoria;
import com.tecup.exam_1.model.Usuario;
import com.tecup.exam_1.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(Usuario usuario, String accion, String entidad, String detalle, String ip) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setDetalle(detalle != null && detalle.length() > 500 ? detalle.substring(0, 500) : detalle);
        auditoria.setIp(ip);
        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarRecientes() {
        return auditoriaRepository.findTop100ByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public List<Auditoria> listarPorUsuario(String username) {
        return auditoriaRepository.findByUsuarioUsernameIgnoreCaseOrderByFechaDesc(username);
    }

    @Transactional(readOnly = true)
    public Auditoria obtener(Long id) {
        return auditoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la auditoría con id " + id));
    }
}