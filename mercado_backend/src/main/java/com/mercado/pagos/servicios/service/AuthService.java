package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.LoginRequestDTO;
import com.mercado.pagos.servicios.dto.request.RegistroUsuarioRequestDTO;
import com.mercado.pagos.servicios.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO requestDTO);

    AuthResponseDTO registrarUsuarioInicial(RegistroUsuarioRequestDTO requestDTO);

}
