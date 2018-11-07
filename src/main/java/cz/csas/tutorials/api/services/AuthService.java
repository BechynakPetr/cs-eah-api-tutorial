package cz.csas.tutorials.api.services;

import cz.csas.tutorials.api.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthService {
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public AuthService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    /**
     * Gets code and changes it for access token.
     * @param redirectUri uri where the browser will redirect after getting code. Not used in this example.
     * @param clientId application id
     * @return access token
     * @throws MalformedURLException if the uri is incorrect
     */
    public TokenResponse getNewTokenResponse(String redirectUri, String clientId) throws MalformedURLException {
        String state = "someValue"; // useful when using redirection from id provider get-code call, not used here
        String code = getCode(redirectUri, clientId, state);
        log.debug("Getting code. Code = " + code);
        TokenResponse tokenResponse = changeCodeForToken(code, clientId, environment.getRequiredProperty("clientSecret"));
        assert tokenResponse != null;
        String accessToken = tokenResponse.getAccessToken();
        log.debug("Changing code for token. Token = " + accessToken); // Do not log token in production!
        return tokenResponse;
    }

    /**
     * Gets code.
     * @param redirectUri uri where the browser will redirect after getting code. Not used in this example.
     * @param clientId application id
     * @param state variable used to pass any information through getting code process, see Oauth for details
     * @return code
     * @throws MalformedURLException if the uri is incorrect
     */
    private String getCode(String redirectUri, String clientId, String state) throws MalformedURLException {
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

    /**
     * Exchenages code for token.
     * @param code obtained from identity provider
     * @param clientId application id
     * @param secret secret obtained during app initialization at developers portal
     * @return access token, refresh token
     */
    private TokenResponse changeCodeForToken(String code, String clientId, String secret) {
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

    /**
     * Gets new access token based on refresh token.
     * @param refreshToken refresh token obtained together with access token
     * @param clientId application id
     * @param secret secret obtained during app initialization at developers portal
     * @return access token
     */
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
