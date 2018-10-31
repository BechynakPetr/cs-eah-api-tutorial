package cz.csas.tutorials.api;

import cz.csas.tutorials.api.model.ExpiredTokenException;
import cz.csas.tutorials.api.model.TokenResponse;
import cz.csas.tutorials.api.services.AuthService;
import cz.csas.tutorials.api.services.CorpService;
import cz.csas.tutorials.api.services.PersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

@RestController
@Slf4j
public class CsasApiController {

    private final AuthService authService;
    private final CorpService corpService;
    private final PersService persService;
    private final Environment environment;

    @Autowired
    public CsasApiController(AuthService authService, CorpService corpService, PersService persService, Environment environment) {
        this.authService = authService;
        this.corpService = corpService;
        this.persService = persService;
        this.environment = environment;
    }

    @GetMapping("/corpaccounts")
    public String getCorpAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order) throws MalformedURLException {
        String clientId = environment.getRequiredProperty("clientId");
        String redirectUri = environment.getRequiredProperty("redirectUri");
        String webApiKey = environment.getRequiredProperty("webApiKey");
        String clientSecret = environment.getRequiredProperty("clientSecret");
        String state = "someValue"; // useful when using redirection from id provider get-code call, not used here
        String code = authService.getCode(redirectUri, clientId, state);
        log.debug("Getting code. Code = " + code);
        TokenResponse tokenResponse = authService.changeCodeForToken(code, clientId, environment.getRequiredProperty("clientSecret"));
        String accessToken = tokenResponse.getAccessToken();
        log.debug("Changing code for token. Token = " + accessToken); // Do not log token in production!
        String accounts = null;
        try {
            accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            String newAccessToken = authService.refreshToken(tokenResponse.getRefreshToken(), clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + tokenResponse.getRefreshToken()); // Do not log token in production!
            log.debug("Obtained new access token = " + newAccessToken); // Do not log token in production!
            try {
                accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
                log.debug("Calling corporate accounts API with new access token. Response = " + accounts);
            } catch (ExpiredTokenException e1) {
                e1.printStackTrace();
            }
        }

        return accounts;
    }

    @GetMapping("/corptranshist")
    public String getCorpAccounts(@RequestParam(defaultValue = "1") String id,
                                  @RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(defaultValue = "2016-09-04T00:00:00+01:00") String startDate,
                                  @RequestParam(defaultValue = "2018-09-04T00:00:00+01:00") String endDate
    ) throws MalformedURLException {
        String clientId = environment.getRequiredProperty("clientId");
        String redirectUri = environment.getRequiredProperty("redirectUri");
        String webApiKey = environment.getRequiredProperty("webApiKey");
        String clientSecret = environment.getRequiredProperty("clientSecret");
        String state = "someValue"; // useful when using redirection from id provider get-code call, not used here
        String code = authService.getCode(redirectUri, clientId, state);
        log.debug("Getting code. Code = " + code);
        TokenResponse tokenResponse = authService.changeCodeForToken(code, clientId, environment.getRequiredProperty("clientSecret"));
        String accessToken = tokenResponse.getAccessToken();
        log.debug("Changing code for token. Token = " + accessToken); // Do not log token in production!

        String transHistory = null;
        try {
            transHistory = corpService.getTransHistory(id, accessToken, webApiKey, page, size, sort, order, startDate, endDate);
            log.debug("Calling corporate transaction history API. Response = " + transHistory);
        } catch (ExpiredTokenException e) {
            String newAccessToken = authService.refreshToken(tokenResponse.getRefreshToken(), clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + tokenResponse.getRefreshToken()); // Do not log token in production!
            log.debug("Obtained new access token = " + newAccessToken); // Do not log token in production!
            try {
                transHistory = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
                log.debug("Calling corporate transaction history API with new access token. Response = " + transHistory);
            } catch (ExpiredTokenException e1) {
                e1.printStackTrace();
            }
        }

        return transHistory;
    }

    @GetMapping("/persaccounts")
    public String getPersAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String flagFilter
    ) throws MalformedURLException {
        String clientId = environment.getRequiredProperty("clientId");
        String redirectUri = environment.getRequiredProperty("redirectUri");
        String webApiKey = environment.getRequiredProperty("webApiKey");
        String clientSecret = environment.getRequiredProperty("clientSecret");
        String state = "someValue"; // useful when using redirection from id provider get-code call, not used here

        String code = authService.getCode(redirectUri, clientId, state);
        log.debug("Getting code. Code = " + code);
        TokenResponse tokenResponse = authService.changeCodeForToken(code,
                clientId,
                environment.getRequiredProperty("clientSecret"));
        String accessToken = tokenResponse.getAccessToken();
        log.debug("Changing code for token. Token = " + accessToken); // Do not log token in production!
        String accounts = null;
        try {
            accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            String newAccessToken = authService.refreshToken(tokenResponse.getRefreshToken(), clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + tokenResponse.getRefreshToken()); // Do not log token in production!
            log.debug("Obtained new access token = " + newAccessToken); // Do not log token in production!
            try {
                accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
                log.debug("Calling corporate accounts API with new access token. Response = " + accounts);
            } catch (ExpiredTokenException e1) {
                e1.printStackTrace();
            }
        }
        return accounts;
    }
}
