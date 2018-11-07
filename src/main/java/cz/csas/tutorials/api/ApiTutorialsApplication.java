package cz.csas.tutorials.api;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Springboot application - main class. It's goal is to show calls to CSAS API.
 */
@SpringBootApplication
@Configuration
public class ApiTutorialsApplication {

    private final Environment environment;

    public ApiTutorialsApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiTutorialsApplication.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        String proxyUrl = environment.getRequiredProperty("proxyUrl");
        String proxyPort = environment.getRequiredProperty("proxyPort");
        String proxyScheme = environment.getRequiredProperty("proxyScheme");

        CloseableHttpClient httpClient;
        if (!StringUtils.isEmpty(proxyUrl) && !StringUtils.isEmpty(proxyPort) && !StringUtils.isEmpty(proxyScheme)) {
            httpClient = HttpClientBuilder.create()
                    .disableRedirectHandling()
                    .setProxy(new HttpHost(proxyUrl, Integer.parseInt(proxyPort), proxyScheme))
                    .build();
        } else {
            httpClient = HttpClientBuilder.create()
                    .disableRedirectHandling()
                    .build();
        }

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        FormHttpMessageConverter converter = new FormHttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        converter.setSupportedMediaTypes(mediaTypes);
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }
}
