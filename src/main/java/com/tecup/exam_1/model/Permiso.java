package com.tecup.exam_1.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String modulo;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    public String clave() {
        return modulo + ":" + nombre.toUpperCase();
    }
}