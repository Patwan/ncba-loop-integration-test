package org.example.crudservice.config;

import com.developerhut.intothespring.ws.CountryInfoService;
import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import org.example.crudservice.components.SoapLogsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;

@Configuration
public class SoapClientConfig {

    @Bean
    public CountryInfoServiceSoapType countryInfoPort(CountryInfoProperties props) throws Exception {
        URL wsdlUrl = getClass().getClassLoader().getResource("wsdl/countries.wsdl");
        if (wsdlUrl == null) {
            throw new IllegalStateException("WSDL not found on classpath: wsdl/countries.wsdl");
        }

        CountryInfoService service = new CountryInfoService(wsdlUrl);
        CountryInfoServiceSoapType port = service.getCountryInfoServiceSoap();

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                props.getEndpointUrl()
        );
        bp.getRequestContext().put("com.sun.xml.ws.connect.timeout", props.getConnectTimeout());
        bp.getRequestContext().put("com.sun.xml.ws.request.timeout", props.getReadTimeout());

        List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SoapLogsHandler());
        bp.getBinding().setHandlerChain(handlers);

        return port;
    }
}