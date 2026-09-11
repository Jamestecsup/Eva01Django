package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {

    List<Rol> findAllByOrderByNombreAsc();

    List<Rol> findByEstado(Estado estado);

    boolean existsByNombreIgnoreCase(String nombre);
}