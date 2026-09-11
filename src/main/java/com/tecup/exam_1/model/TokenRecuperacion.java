package com.tecup.exam_1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime fechaExpiracion;

    private Boolean utilizado;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    public boolean esValido() {
        return Boolean.FALSE.equals(utilizado)
                && fechaExpiracion != null
                && fechaExpiracion.isAfter(LocalDateTime.now());
    }
}