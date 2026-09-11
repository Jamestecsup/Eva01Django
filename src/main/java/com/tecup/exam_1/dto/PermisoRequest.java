package com.tecup.exam_1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoRequest {

    private Long id;

    @NotBlank(message = "El nombre del permiso es obligatorio")
    private String nombre;

    @NotBlank(message = "El módulo es obligatorio")
    private String modulo;

    private String descripcion;

    private String estado;
}