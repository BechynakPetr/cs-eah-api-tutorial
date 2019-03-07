package cz.csas.tutorials.api.services;

import cz.csas.tutorials.api.model.ExchangeCodeForTokenException;
import cz.csas.tutorials.api.model.ExpiredRefreshTokenException;
import cz.csas.tutorials.api.model.StateNotFoundException;
import cz.csas.tutorials.api.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class AuthService {
    private final String state = "someValue";
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Autowired
    public AuthService(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    /**
     * Builds url that is used for user authorization.
     *
     * @param redirectUri where the user should be redirected after successful authorization
     * @param clientId    application id
     * @return url for authorization
     */
    public String getAuthorizationUrl(String redirectUri, String clientId) {
        String authorizationUrl = environment.getRequiredProperty("authorizationUrl");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authorizationUrl)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("state", state)
                .queryParam("scope", "AISP,coract,peract");

        return builder.toUriString();
    }

    /**
     * Checks that received state is the one we sent to CSAS and exchange received code for for access and refresh tokens.
     *
     * @param code          for exchanging for tokens
     * @param receivedState should match with the one we sent to CSAS
     * @return access and refresh tokens
     * @throws StateNotFoundException        if received state is not the one we sent to CSAS.
     * @throws ExchangeCodeForTokenException if anything bad happens during exchanging code.
     */
    public TokenResponse obtainTokens(String code, String receivedState) throws ExchangeCodeForTokenException, StateNotFoundException {
        if (state.equals(receivedState)) {
            return changeCodeForToken(code, environment.getRequiredProperty("clientId"), environment.getRequiredProperty("clientSecret"),
                    environment.getRequiredProperty("authorizationRedirectUri"));
        } else {
            throw new StateNotFoundException("Received state not found");
        }
    }

    /**
     * Exchanges code for token.
     *
     * @param code     obtained from identity provider
     * @param clientId application id
     * @param secret   secret obtained during app initialization at developers portal
     * @param authorizationRedirectUri redirect URI sent on auth endpoint
     * @return access token, refresh token
     * @throws ExchangeCodeForTokenException if anything bad happens during exchanging code.
     */
    private TokenResponse changeCodeForToken(String code, String clientId, String secret, String authorizationRedirectUri) throws ExchangeCodeForTokenException {
        String tokenUrl = environment.getProperty("tokenUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("client_id", clientId);
        map.add("client_secret", secret);
        map.add("redirect_uri", authorizationRedirectUri);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<TokenResponse> tokenEntity = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
            return tokenEntity.getBody();
        } catch (Exception ex) {
            throw new ExchangeCodeForTokenException("Error during exchanging code for token");
        }
    }

    /**
     * Gets new access token based on refresh token.
     *
     * @param refreshToken refresh token obtained together with access token
     * @param clientId     application id
     * @param secret       secret obtained during app initialization at developers portal
     * @return access token
     */
    public String getNewAccessToken(String refreshToken, String clientId, String secret) throws ExpiredRefreshTokenException {
        String tokenUrl = environment.getProperty("tokenUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);
        map.add("client_id", clientId);
        map.add("client_secret", secret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<TokenResponse> tokenEntity = restTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
            return tokenEntity.getBody().getAccessToken();
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
                throw new ExpiredRefreshTokenException("Refresh token has expired.");
            } else {
                throw ex;
            }
        }
    }
}
