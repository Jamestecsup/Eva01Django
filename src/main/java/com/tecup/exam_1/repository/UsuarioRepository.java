package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByEmpleadoDni(String dni);

    Optional<Usuario> findByEmpleadoCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByUsernameAsc();

    List<Usuario> findByEstado(Estado estado);

    boolean existsByUsernameIgnoreCase(String username);
}