package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Estado;
import com.tecup.exam_1.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    List<Permiso> findAllByOrderByModuloAscNombreAsc();

    List<Permiso> findByModuloOrderByNombreAsc(String modulo);

    List<Permiso> findByEstado(Estado estado);

    Optional<Permiso> findByModuloAndNombre(String modulo, String nombre);
}