package com.tecup.exam_1.repository;

import com.tecup.exam_1.model.Area;
import com.tecup.exam_1.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {

    List<Area> findAllByOrderByNombreAsc();

    List<Area> findByEstado(Estado estado);

    boolean existsByNombreIgnoreCase(String nombre);
}