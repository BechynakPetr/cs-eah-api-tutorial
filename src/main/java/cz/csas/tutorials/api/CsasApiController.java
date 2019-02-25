package cz.csas.tutorials.api;

import cz.csas.tutorials.api.model.ExchangeCodeForTokenException;
import cz.csas.tutorials.api.model.ExpiredRefreshTokenException;
import cz.csas.tutorials.api.model.ExpiredTokenException;
import cz.csas.tutorials.api.model.StateNotFoundException;
import cz.csas.tutorials.api.model.TokenResponse;
import cz.csas.tutorials.api.services.AuthService;
import cz.csas.tutorials.api.services.CorpService;
import cz.csas.tutorials.api.services.PersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class, 3 methods for corporate API, 1 method for personal accounts API. All authorization calls are served
 * in authService.
 */
@RestController
@Slf4j
public class CsasApiController {

    private final AuthService authService;
    private final CorpService corpService;
    private final PersService persService;

    @Autowired
    public CsasApiController(AuthService authService, CorpService corpService, PersService persService) {
        this.authService = authService;
        this.corpService = corpService;
        this.persService = persService;
    }

    @Value("${authorizationRedirectUri}")
    private String authorizationRedirectUri;
    @Value("${redirectUri}")
    private String redirectUri;
    @Value("${webApiKey}")
    private String webApiKey;
    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;
    private String accessToken = null;
    private String refreshToken = null;

    /**
     * Builds url that is used for user authorization.
     *
     * @return url for user authorization
     */
    @GetMapping("/auth/authUrl")
    public ResponseEntity getAuthorizationUrl() {
        String authorizationUrl = authService.getAuthorizationUrl(authorizationRedirectUri, clientId);
        return ResponseEntity.ok(authorizationUrl);
    }


	/**
	 * CSAS IDP redirect user to this endpoint after successful authorization. Application exchange received code for for access and refresh tokens.
	 *
	 * @param code  received from CSAS
	 * @param state received from CSAS
	 * @return message for user
	 * @throws StateNotFoundException        if received state is not the one we sent to CSAS.
	 * @throws ExchangeCodeForTokenException if anything bad happens during exchanging code.
	 */
	@GetMapping("/auth/callback")
	public ResponseEntity obtainTokens(@RequestParam String code,
									   @RequestParam String state) throws StateNotFoundException, ExchangeCodeForTokenException {
		TokenResponse tokens = authService.obtainTokens(code, state);
		accessToken = tokens.getAccessToken();
		refreshToken = tokens.getRefreshToken();
		return ResponseEntity.ok("Code has been changed for tokens. Application is now ready to serve API calls.");
	}

    /**
     * Calls corporate accounts API v1/corporate/our/accounts, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v1/corporate
     *
     * @param page  number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size  of page
     * @param sort  for results sorting
     * @param order asc/desc
     * @return JSON response in String form (object is not returned on purpose)
     * @throws ExpiredTokenException if new access token is rejected by CSAS IDP.
     */
    @GetMapping("/corpaccounts")
    public ResponseEntity<String> getCorpAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order) throws ExpiredTokenException {
        String accounts;
        try {
            accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
            try {
                accessToken = authService.getNewAccessToken(refreshToken, clientId, clientSecret);
            } catch (ExpiredRefreshTokenException e1) {
                log.debug("Refresh token has expired. Client has to be authorized.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.header("WWW-Authenticate", "Bearer")
						.body("Refresh token has expired. Client has to be authorized.");
            }
            accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
        }

        return ResponseEntity.ok().body(accounts);
    }

    /**
     * Calls corporate account balance API v1/corporate/our/accounts/id/balance, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v1/corporate
     *
     * @param id - account number, e.g. 3520EF975815E488AFED5180CD32689934720E12
     * @return information about account balance in form of String
     * @throws ExpiredTokenException if new access token is rejected by CSAS IDP.
     */
    @GetMapping("/corpaccbalance")
    public ResponseEntity<String> getCorpAccBalance(@RequestParam(defaultValue = "1") String id) throws ExpiredTokenException {
        String accounts;
        try {
            accounts = corpService.getCorpAccBalance(accessToken, webApiKey, id);
            log.debug("Calling corporate accounts balance check API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
			log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
			try {
				accessToken = authService.getNewAccessToken(refreshToken, clientId, clientSecret);
			} catch (ExpiredRefreshTokenException e1) {
				log.debug("Refresh token has expired. Client has to be authorized.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.header("WWW-Authenticate", "Bearer")
						.body("Refresh token has expired. Client has to be authorized.");
			}
            accounts = corpService.getCorpAccBalance(accessToken, webApiKey, id);
        }

        return ResponseEntity.ok().body(accounts);
    }

    /**
     * Calls corporate account balance API v1/corporate/our/accounts/id/transactions, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v1/corporate
     *
     * @param id        account number, e.g. 3520EF975815E488AFED5180CD32689934720E12
     * @param page      number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size      of page
     * @param sort      for results sorting
     * @param order     asc/desc
     * @param startDate start date of transactions list
     * @param endDate   end date of transactions list
     * @return list of transactions in form of String
     * @throws ExpiredTokenException if new access token is rejected by CSAS IDP.
     */
    @GetMapping("/corptranshist")
    public ResponseEntity<String> getCorpAccounts(@RequestParam(defaultValue = "1") String id,
                                  @RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(defaultValue = "2016-09-04T00:00:00+01:00") String startDate,
                                  @RequestParam(defaultValue = "2018-09-04T00:00:00+01:00") String endDate
    ) throws ExpiredTokenException {
        String transHistory;
        try {
            transHistory = corpService.getTransHistory(id, accessToken, webApiKey, page, size, sort, order, startDate, endDate);
            log.debug("Calling corporate transaction history API. Response = " + transHistory);
        } catch (ExpiredTokenException e) {
			log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
			try {
				accessToken = authService.getNewAccessToken(refreshToken, clientId, clientSecret);
			} catch (ExpiredRefreshTokenException e1) {
				log.debug("Refresh token has expired. Client has to be authorized.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.header("WWW-Authenticate", "Bearer")
						.body("Refresh token has expired. Client has to be authorized.");
			}
            log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
			transHistory = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
            log.debug("Calling corporate transaction history API with new access token. Response = " + transHistory);
        }

        return ResponseEntity.ok().body(transHistory);
    }

    /**
     * Calls personal accounts list API v3/netbanking/my/accounts, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v3/netbanking
     *
     * @param page       number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size       of page
     * @param sort       for results sorting
     * @param order      asc/desc
     * @param type       An optional comma-separated list of requested product types. Example: CURRENT
     * @param flagFilter An optional comma-separated list of flags that will be used for account filtering. AND
     *                   Logical operator is used for joining flags in filter. In other words only accounts that has
     *                   all specified flags will be returned. Example: netPayAllowed
     * @return list of accounts in form of String
     * @throws ExpiredTokenException if new access token is rejected by CSAS IDP.
     */
    @GetMapping("/persaccounts")
    public ResponseEntity<String> getPersAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String flagFilter
    ) throws ExpiredTokenException {
        String accounts;
        try {
            accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
			try {
				accessToken = authService.getNewAccessToken(refreshToken, clientId, clientSecret);
			} catch (ExpiredRefreshTokenException e1) {
				log.debug("Refresh token has expired. Client has to be authorized.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.header("WWW-Authenticate", "Bearer")
						.body("Refresh token has expired. Client has to be authorized.");
			}
			log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
            accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
            log.debug("Calling corporate accounts API with new access token. Response = " + accounts);
        }
        return ResponseEntity.ok().body(accounts);
    }

}
