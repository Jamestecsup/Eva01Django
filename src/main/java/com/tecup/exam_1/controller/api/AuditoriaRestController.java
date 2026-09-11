package com.tecup.exam_1.controller.api;

import com.tecup.exam_1.model.Auditoria;
import com.tecup.exam_1.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
public class AuditoriaRestController {

    private final AuditoriaService auditoriaService;

    public AuditoriaRestController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Auditoria> listar() {
        return auditoriaService.listarRecientes();
    }

    @GetMapping("/usuario/{username}")
    public List<Auditoria> listarPorUsuario(@PathVariable String username) {
        return auditoriaService.listarPorUsuario(username);
    }

    @GetMapping("/{id}")
    public Auditoria obtener(@PathVariable Long id) {
        return auditoriaService.obtener(id);
    }
}