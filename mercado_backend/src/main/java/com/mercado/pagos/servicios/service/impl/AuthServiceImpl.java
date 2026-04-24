package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.LoginRequestDTO;
import com.mercado.pagos.servicios.dto.request.RegistroUsuarioRequestDTO;
import com.mercado.pagos.servicios.dto.response.AuthResponseDTO;
import com.mercado.pagos.servicios.exception.BadRequestException;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.security.JwtService;
import com.mercado.pagos.servicios.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String TIPO_TOKEN = "Bearer";
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    requestDTO.getUsername(),
                    requestDTO.getPassword()
            ));
        } catch (BadCredentialsException exception) {
            log.warn("Credenciales invalidas para usuario {}", requestDTO.getUsername());
            throw new BadRequestException("Credenciales invalidas");
        } catch (AuthenticationException exception) {
            log.warn("Error de autenticacion para usuario {}: {}", requestDTO.getUsername(), exception.getMessage());
            throw new BadRequestException("No se pudo autenticar al usuario");
        }

        Usuario usuario = usuarioRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        String token = jwtService.generarToken(toUserDetails(usuario));
        log.info("Login exitoso para usuario {}", usuario.getUsername());

        return buildAuthResponse(usuario, token);
    }

    @Override
    public AuthResponseDTO registrarUsuarioInicial(RegistroUsuarioRequestDTO requestDTO) {
        if (usuarioRepository.count() > 0) {
            throw new BusinessRuleException("El usuario administrador inicial ya fue registrado");
        }
        if (usuarioRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new BusinessRuleException("Ya existe un usuario con el username indicado");
        }

        Usuario usuario = Usuario.builder()
                .username(requestDTO.getUsername())
                .passwordHash(passwordEncoder.encode(requestDTO.getPassword()))
                .nombreCompleto(requestDTO.getNombreCompleto())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(toUserDetails(guardado));
        log.info("Usuario administrador inicial registrado con id {}", guardado.getId());

        return buildAuthResponse(guardado, token);
    }

    private UserDetails toUserDetails(Usuario usuario) {
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(ADMIN_AUTHORITY)
                .build();
    }

    private AuthResponseDTO buildAuthResponse(Usuario usuario, String token) {
        return AuthResponseDTO.builder()
                .token(token)
                .tipoToken(TIPO_TOKEN)
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .build();
    }

}
