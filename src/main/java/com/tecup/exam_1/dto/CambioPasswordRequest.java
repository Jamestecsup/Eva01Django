package com.tecup.exam_1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordRequest {

    @NotBlank(message = "Ingrese la contraseña actual")
    private String passwordActual;

    @NotBlank(message = "Ingrese la nueva contraseña")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String passwordNueva;
}