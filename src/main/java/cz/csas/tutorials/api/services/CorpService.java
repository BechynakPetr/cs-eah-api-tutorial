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
import java.util.HashMap;
import java.util.Map;

@Service
public class CorpService {
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public CorpService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public String getCorpAccounts(String token, String webApiKey, String page, String size, String sort, String order) throws ExpiredTokenException {
        Map<String, String> uriParams = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpAccountsUrl"))
                // Add query parameter
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .queryParam("order", order);
        String corpAccountsUrl = builder.buildAndExpand(uriParams).toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<String> accounts = restTemplate.exchange(corpAccountsUrl, HttpMethod.GET, entity, String.class);
        if (HttpStatus.UNAUTHORIZED.equals(accounts.getStatusCode())){
            throw new ExpiredTokenException("Token has expired.");
        }
        return accounts.getBody();
    }

    public String getTransHistory(String id, String token, String webApiKey, String page, String size, String sort, String order,
                                  String dateStart, String dateEnd) throws ExpiredTokenException {

        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("id", id);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpHistoryTransUrl"))
                // Add query parameter
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .queryParam("order", order)
                .queryParam("dateStart", dateStart)
                .queryParam("dateEnd", dateEnd);
        String corpHistoryTransUrl = builder.buildAndExpand(uriParams).toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<String> accounts = restTemplate.exchange(corpHistoryTransUrl, HttpMethod.GET, entity, String.class);
        if (HttpStatus.UNAUTHORIZED.equals(accounts.getStatusCode())){
            throw new ExpiredTokenException("Token has expired.");
        }
        return accounts.getBody();
    }

}
