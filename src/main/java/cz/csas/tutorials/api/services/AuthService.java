package cz.csas.tutorials.api.services;

import cz.csas.tutorials.api.model.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Service
public class AuthService {
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public AuthService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    public String getCode(String redirectUri, String clientId, String state) throws MalformedURLException {
        String codeUri = environment.getProperty("codeUri");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("redirect_uri", redirectUri);
        map.add("client_id", clientId);
        map.add("response_type", "code");
        map.add("access_type", "offline");
        map.add("state", state);

        URI uri = UriComponentsBuilder.fromUriString(codeUri)
                .queryParams(map)
                .encode()
                .build()
                .toUri();
        ResponseEntity<Object> tokenEntity = restTemplate.getForEntity(uri, Object.class);
        String location = (tokenEntity.getHeaders().get("location")).get(0);
        URL urlLocation = new URL(location);
        List<String> params = Arrays.asList(urlLocation.getQuery().split("&"));
        String code = params.stream()
                .map(q->q.split("="))
                .filter(a->a[0].equals("code"))
                .map(a->a[1])
                .findAny()
                .orElse("");
        return code;
    }

    public TokenResponse changeCodeForToken(String code, String clientId, String secret) {
        String tokenUri = environment.getProperty("tokenUri");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("client_id", clientId);
        map.add("client_secret", secret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<TokenResponse> tokenEntity = restTemplate.postForEntity(tokenUri, request, TokenResponse.class);
        if (tokenEntity.getStatusCode().equals(OK)) {
            return tokenEntity.getBody();
        }
        return null;
    }

    public String refreshToken(String refreshToken, String clientId, String secret) {
        String tokenUri = environment.getProperty("tokenUri");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);
        map.add("client_id", clientId);
        map.add("client_secret", secret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<TokenResponse> tokenEntity = restTemplate.postForEntity(tokenUri, request, TokenResponse.class);
        if (tokenEntity.getStatusCode().equals(OK)) {
            return tokenEntity.getBody().getAccessToken();
        }
        return "";
    }
}
