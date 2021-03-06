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

@Service
public class PersService {
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public PersService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    /**
     *
     * @param token     access token
     * @param webApiKey webapi key to connect to webapi
     * @param page      number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size      of page
     * @param sort      for results sorting
     * @param order     asc/desc
     * @param type       An optional comma-separated list of requested product types. Example: CURRENT
     * @param flagFilter An optional comma-separated list of flags that will be used for account filtering. AND
     *                   Logical operator is used for joining flags in filter. In other words only accounts that has
     *                   all specified flags will be returned. Example: netPayAllowed
     * @return personal accounts - JSON response in String form
     * @throws ExpiredTokenException if access token is expired
     */
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
        try {
            return restTemplate.exchange(persAccountsUrl, HttpMethod.GET, entity, String.class).getBody();
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.FORBIDDEN.equals(ex.getStatusCode())) {
                throw new ExpiredTokenException("Token has expired or is invalid.");
            } else {
                throw ex;
            }
        }
    }

}
