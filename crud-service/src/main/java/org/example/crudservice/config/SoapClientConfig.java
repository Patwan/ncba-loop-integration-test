package org.example.crudservice.config;

import com.developerhut.intothespring.ws.CountryInfoService;
import com.developerhut.intothespring.ws.CountryInfoServiceSoapType;
import jakarta.xml.ws.BindingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        return port;
    }
}