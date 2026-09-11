package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}