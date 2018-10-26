package cz.csas.tutorials.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PersService {
    private final RestTemplate restTemplate;
    private final Environment environment;
    @Autowired
    public PersService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public String getPersAccounts(String token, String webApiKey){
        String persTransHistUrl = environment.getProperty("persTransHistUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<String> accounts = restTemplate.exchange(persTransHistUrl, HttpMethod.GET, entity, String.class);
        return accounts.getBody();
    }

}
