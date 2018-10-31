package cz.csas.tutorials.api.services;

import cz.csas.tutorials.api.model.ExpiredTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PersService {
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public PersService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public String getPersAccounts(String token, String webApiKey, String page, String size, String sort, String order,
                                  String type, String flagFilter) throws ExpiredTokenException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpAccountsUrl"))
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .queryParam("order", order)
                .queryParam("type", type)
                .queryParam("flagFilter", flagFilter);
        String persAccountsUrl = builder.buildAndExpand().toString();

        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<String> accounts = restTemplate.exchange(persAccountsUrl, HttpMethod.GET, entity, String.class);
        if (HttpStatus.UNAUTHORIZED.equals(accounts.getStatusCode())) {
            throw new ExpiredTokenException("Token has expired.");
        }
        return accounts.getBody();
    }

}
