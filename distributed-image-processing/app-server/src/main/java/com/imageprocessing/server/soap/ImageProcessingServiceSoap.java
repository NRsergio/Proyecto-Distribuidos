package com.imageprocessing.server.soap;

import com.imageprocessing.server.model.dto.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService(name = "ImageProcessingService",
            targetNamespace = "http://imageprocessing.com/soap")
public interface ImageProcessingServiceSoap {

    @WebMethod(operationName = "login")
    LoginResponse login(@WebParam(name = "request") LoginRequest request);

    @WebMethod(operationName = "enviarLote")
    EnviarLoteResponse enviarLote(@WebParam(name = "request") EnviarLoteRequest request);

    @WebMethod(operationName = "consultarProgreso")
    ProgresoBatchResponse consultarProgreso(@WebParam(name = "idLote") Long idLote,
                                             @WebParam(name = "token") String token);
}
