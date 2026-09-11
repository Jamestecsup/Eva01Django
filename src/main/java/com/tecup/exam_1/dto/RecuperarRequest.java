package com.tecup.exam_1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecuperarRequest {

    @NotBlank(message = "Ingrese su correo")
    @Email(message = "El correo no es válido")
    private String correo;
}