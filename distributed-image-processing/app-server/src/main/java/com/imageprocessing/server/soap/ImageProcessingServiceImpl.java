package com.imageprocessing.server.soap;

import com.imageprocessing.server.model.dto.*;
import com.imageprocessing.server.service.AuthService;
import com.imageprocessing.server.service.BatchService;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WebService(endpointInterface = "com.imageprocessing.server.soap.ImageProcessingServiceSoap",
            serviceName = "ImageProcessingService",
            portName = "ImageProcessingPort",
            targetNamespace = "http://imageprocessing.com/soap")
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingServiceSoap {

    private final AuthService authService;
    private final BatchService batchService;

    @Override
    public LoginResponse register(RegisterRequest request) {
        log.info("SOAP register: {}", request.getEmail());
        return authService.register(request);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("SOAP login: {}", request.getEmail());
        return authService.login(request);
    }

    @Override
    public EnviarLoteResponse enviarLote(EnviarLoteRequest request) {
        Long idUsuario = authService.validarToken(request.getToken());
        log.info("SOAP enviarLote: usuario={}, imagenes={}", idUsuario, request.getImagenes().size());
        return batchService.procesarEnvioLote(idUsuario, request);
    }

    @Override
    public ProgresoBatchResponse consultarProgreso(Long idLote, String token) {
        authService.validarToken(token);
        return batchService.consultarProgreso(idLote);
    }
}
