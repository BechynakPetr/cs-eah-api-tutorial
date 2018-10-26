package cz.csas.tutorials.api;

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
    public String getCorpAccounts(@RequestParam(defaultValue = "csas-auth") String state) throws MalformedURLException {
        String clientId = environment.getRequiredProperty("clientId");
        String redirectUri = environment.getRequiredProperty("redirectUri");
        String code = authService.getCode(redirectUri, clientId, state);
        String webApiKey = environment.getRequiredProperty("webApiKey");

        log.debug("Getting code. Code = " + code);
        String token = authService.changeCodeForToken(code,
                clientId,
                environment.getRequiredProperty("clientSecret"));
        log.debug("Changing code for token. Token = " + token); // Do not log token in production!
        String accounts = corpService.getCorpAccounts(token, webApiKey);
        log.debug("Calling corporate accounts API. Response = " + accounts);
        return accounts;
    }

    @GetMapping("/persaccounts")
    public String getPersAccounts(@RequestParam(defaultValue = "csas-auth") String state) throws MalformedURLException {
        String clientId = environment.getRequiredProperty("clientId");
        String redirectUri = environment.getRequiredProperty("redirectUri");
        String webApiKey = environment.getRequiredProperty("webApiKey");

        String code = authService.getCode(redirectUri, clientId, state);
        log.debug("Getting code. Code = " + code);
        String token = authService.changeCodeForToken(code,
                clientId,
                environment.getRequiredProperty("clientSecret"));
        log.debug("Changing code for token. Token = " + token); // Do not log token in production!
        String accounts = persService.getPersAccounts(token, webApiKey);
        log.debug("Calling corporate accounts API. Response = " + accounts);
        return accounts;
    }
}
