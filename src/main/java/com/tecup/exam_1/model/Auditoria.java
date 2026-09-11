package com.tecup.exam_1.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String accion;

    @Column(nullable = false, length = 80)
    private String entidad;

    @Column(length = 500)
    private String detalle;

    private String ip;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}