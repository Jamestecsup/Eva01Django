package com.tecup.exam_1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponse {

    private boolean exito;
    private String mensaje;

    public static MensajeResponse ok(String mensaje) {
        return new MensajeResponse(true, mensaje);
    }

    public static MensajeResponse error(String mensaje) {
        return new MensajeResponse(false, mensaje);
    }
}