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
public class AreaRequest {

    private Long id;

    @NotBlank(message = "El nombre del área es obligatorio")
    private String nombre;

    private String descripcion;

    private String estado;
}