package com.imageprocessing.server.config;

import com.imageprocessing.server.soap.ImageProcessingServiceImpl;
import jakarta.xml.ws.Endpoint;
import lombok.RequiredArgsConstructor;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SoapConfig {

    private final Bus bus;
    private final ImageProcessingServiceImpl imageProcessingService;

    @Bean
    public Endpoint imageProcessingEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, imageProcessingService);
        endpoint.publish("/ImageProcessingService");
        return endpoint;
    }
}
