package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    List<Empleado> findAllByOrderByApellidosAsc();

    Optional<Empleado> findByDni(String dni);

    Optional<Empleado> findByCorreo(String correo);

    boolean existsByDni(String dni);

    boolean existsByCorreoIgnoreCase(String correo);
}