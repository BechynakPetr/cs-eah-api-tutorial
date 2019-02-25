package cz.csas.tutorials.api.services;

import cz.csas.tutorials.api.model.ExpiredTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    /**
     * Calls corporate accounts API
     *
     * @param token     access token
     * @param webApiKey webapi key to connect to webapi
     * @param page      number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size      of page
     * @param sort      for results sorting
     * @param order     asc/desc
     * @return accounts - JSON response in String form
     * @throws ExpiredTokenException if access token is expired
     */
    public String getCorpAccounts(String token, String webApiKey, String page, String size, String sort, String order) throws ExpiredTokenException {
        Map<String, String> uriParams = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpAccountsUrl"))
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort)
                .queryParam("order", order);
        String corpAccountsUrl = builder.buildAndExpand(uriParams).toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(corpAccountsUrl, HttpMethod.GET, entity, String.class).getBody();
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.FORBIDDEN.equals(ex.getStatusCode())) {
                throw new ExpiredTokenException("Token has expired or is invalid.");
            } else {
                throw ex;
            }
        }
    }

    /**
     * Calls corporate account balance
     *
     * @param token     access token
     * @param webApiKey webapi key to connect to webapi
     * @param id        account id
     * @return balance on particular account, JSON response in String form
     * @throws ExpiredTokenException if access token is expired
     */
    public String getCorpAccBalance(String token, String webApiKey, String id) throws ExpiredTokenException {
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("id", id);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpAccBalanceUrl"));
        String corpAccountsUrl = builder.buildAndExpand(uriParams).toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("web-api-key", webApiKey);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(corpAccountsUrl, HttpMethod.GET, entity, String.class).getBody();
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.FORBIDDEN.equals(ex.getStatusCode())) {
                throw new ExpiredTokenException("Token has expired or is invalid.");
            } else {
                throw ex;
            }
        }
    }

    /**
     * Calls transaction history on particular account in given time window
     *
     * @param id        account id
     * @param token     access token
     * @param webApiKey webapi key to connect to webapi
     * @param page      number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size      of page
     * @param sort      for results sorting
     * @param order     asc/desc
     * @param dateStart start of time window
     * @param dateEnd   end of time window
     * @return accounts - JSON response in String form
     * @throws ExpiredTokenException if access token is expired
     */
    public String getTransHistory(String id, String token, String webApiKey, String page, String size, String sort, String order,
                                  String dateStart, String dateEnd) throws ExpiredTokenException {

        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("id", id);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(environment.getProperty("corpHistoryTransUrl"))
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
        try {
            return restTemplate.exchange(corpHistoryTransUrl, HttpMethod.GET, entity, String.class).getBody();
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.FORBIDDEN.equals(ex.getStatusCode())) {
                throw new ExpiredTokenException("Token has expired or is invalid.");
            } else {
                throw ex;
            }
        }
    }

}
