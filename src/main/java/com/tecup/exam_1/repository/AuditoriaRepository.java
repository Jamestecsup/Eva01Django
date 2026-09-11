package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findTop100ByOrderByFechaDesc();

    List<Auditoria> findByUsuarioUsernameIgnoreCaseOrderByFechaDesc(String username);
}