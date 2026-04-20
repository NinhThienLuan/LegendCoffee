package fpt.legendcoffee.common.config;


import fpt.legendcoffee.common.properties.GhnProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Configuration
public class AppConfig {
    private final GhnProperties ghnProperties;

    @Bean(name = "ghnRestTemplate")
    public RestTemplate ghnRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(ghnProperties.getConnectTimeout());
        factory.setReadTimeout(ghnProperties.getReadTimeout());
        return new RestTemplate(factory);
    }

}
