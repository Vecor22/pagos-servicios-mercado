package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.LoginRequestDTO;
import com.mercado.pagos.servicios.dto.request.RegistroUsuarioRequestDTO;
import com.mercado.pagos.servicios.dto.response.AuthResponseDTO;
import com.mercado.pagos.servicios.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        log.info("Solicitud de login para usuario {}", requestDTO.getUsername());
        return ResponseEntity.ok(authService.login(requestDTO));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registrarUsuarioInicial(
            @Valid @RequestBody RegistroUsuarioRequestDTO requestDTO
    ) {
        log.info("Solicitud para registrar usuario administrador inicial {}", requestDTO.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrarUsuarioInicial(requestDTO));
    }

}
