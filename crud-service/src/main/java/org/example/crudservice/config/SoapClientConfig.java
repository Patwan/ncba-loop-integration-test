package org.example.crudservice.config;

import com.developerhut.intothespring.ws.CountryInfoService;
import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import org.example.crudservice.components.SoapLogsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SoapClientConfig {

    @Bean
    public CountryInfoServiceSoapType countryInfoPort(CountryInfoProperties props) {
        CountryInfoService service = new CountryInfoService();
        CountryInfoServiceSoapType port = service.getCountryInfoServiceSoap();

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                props.getEndpointUrl()
        );
        bp.getRequestContext().put("com.sun.xml.ws.connect.timeout", props.getConnectTimeout());
        bp.getRequestContext().put("com.sun.xml.ws.request.timeout", props.getReadTimeout());

        // register log handler for debugging the soap envelopes
        BindingProvider bindingProvider = (BindingProvider) port;
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new SoapLogsHandler());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return port;
    }
}