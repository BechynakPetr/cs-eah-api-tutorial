package cz.csas.tutorials.api;

import cz.csas.tutorials.api.model.ExchangeCodeForTokenException;
import cz.csas.tutorials.api.model.ExpiredTokenException;
import cz.csas.tutorials.api.model.RefreshAccessTokenException;
import cz.csas.tutorials.api.model.TokenResponse;
import cz.csas.tutorials.api.services.AuthService;
import cz.csas.tutorials.api.services.CorpService;
import cz.csas.tutorials.api.model.GetCodeException;
import cz.csas.tutorials.api.services.PersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

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
     * Calls corporate accounts API v1/corporate/our/accounts, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v1/corporate
     *
     * @param page  number for paging (paging and sorting works only in production, not sandbox environment)
     * @param size  of page
     * @param sort  for results sorting
     * @param order asc/desc
     * @return JSON response in String form (object is not returned on purpose)
     * @throws MalformedURLException if redirectUri is not correct URI
     */
    @GetMapping("/corpaccounts")
    public String getCorpAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order) throws MalformedURLException, RefreshAccessTokenException,
            GetCodeException, ExchangeCodeForTokenException {
        checkAccessToken();
        String accounts = null;
        try {
            accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            accessToken = authService.refreshAccessToken(refreshToken, clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
            log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
            try {
                accounts = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
                log.debug("Calling corporate accounts API with new access token. Response = " + accounts);
            } catch (ExpiredTokenException e1) {
                log.error("Error when trying to refresh access token");
                throw new RefreshAccessTokenException("Error when trying to refresh access token");
            }
        }

        return accounts;
    }

    /**
     * Calls corporate account balance API v1/corporate/our/accounts/id/balance, see docs https://developers.erstegroup.com/docs/apis/bank.csas/v1/corporate
     *
     * @param id - account number, e.g. 3520EF975815E488AFED5180CD32689934720E12
     * @return information about account balance in form of String
     * @throws MalformedURLException if redirectUri is not correct URI
     */
    @GetMapping("/corpaccbalance")
    public String getCorpAccBalance(@RequestParam(defaultValue = "1") String id) throws MalformedURLException, RefreshAccessTokenException,
            GetCodeException, ExchangeCodeForTokenException {
        checkAccessToken();
        String accounts = null;
        try {
            accounts = corpService.getCorpAccBalance(accessToken, webApiKey, id);
            log.debug("Calling corporate accounts balance check API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            accessToken = authService.refreshAccessToken(refreshToken, clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
            log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
            try {
                accounts = corpService.getCorpAccBalance(accessToken, webApiKey, id);
                log.debug("Calling corporate accounts balance check API with new access token. Response = " + accounts);
            } catch (ExpiredTokenException e1) {
                log.error("Error when trying to refresh access token");
                throw new RefreshAccessTokenException("Error when trying to refresh access token");
            }
        }

        return accounts;
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
     * @throws MalformedURLException if redirectUri is not correct URI
     */
    @GetMapping("/corptranshist")
    public String getCorpAccounts(@RequestParam(defaultValue = "1") String id,
                                  @RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(defaultValue = "2016-09-04T00:00:00+01:00") String startDate,
                                  @RequestParam(defaultValue = "2018-09-04T00:00:00+01:00") String endDate
    ) throws MalformedURLException, RefreshAccessTokenException, GetCodeException, ExchangeCodeForTokenException {
        checkAccessToken();
        String transHistory = null;
        try {
            transHistory = corpService.getTransHistory(id, accessToken, webApiKey, page, size, sort, order, startDate, endDate);
            log.debug("Calling corporate transaction history API. Response = " + transHistory);
        } catch (ExpiredTokenException e) {
            accessToken = authService.refreshAccessToken(refreshToken, clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
            log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
            try {
                transHistory = corpService.getCorpAccounts(accessToken, webApiKey, page, size, sort, order);
                log.debug("Calling corporate transaction history API with new access token. Response = " + transHistory);
            } catch (ExpiredTokenException e1) {
                log.error("Error when trying to refresh access token");
                throw new RefreshAccessTokenException("Error when trying to refresh access token");
            }
        }

        return transHistory;
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
     * @throws MalformedURLException if redirectUri is not correct URI
     */
    @GetMapping("/persaccounts")
    public String getPersAccounts(@RequestParam(defaultValue = "0") String page,
                                  @RequestParam(defaultValue = "1") String size,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) String order,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String flagFilter
    ) throws MalformedURLException, RefreshAccessTokenException, GetCodeException, ExchangeCodeForTokenException {
        checkAccessToken();
        String accounts = null;
        try {
            accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
            log.debug("Calling corporate accounts API. Response = " + accounts);
        } catch (ExpiredTokenException e) {
            accessToken = authService.refreshAccessToken(refreshToken, clientId, clientSecret);
            log.debug("Refreshing access token with refresh token = " + refreshToken); // Do not log token in production!
            log.debug("Obtained new access token = " + accessToken); // Do not log token in production!
            try {
                accounts = persService.getPersAccounts(accessToken, webApiKey, page, size, sort, order, type, flagFilter);
                log.debug("Calling corporate accounts API with new access token. Response = " + accounts);
            } catch (ExpiredTokenException e1) {
                log.error("Error when trying to refresh access token");
                throw new RefreshAccessTokenException("Error when trying to refresh access token");
            }
        }
        return accounts;
    }

    private void checkAccessToken() throws MalformedURLException, GetCodeException, ExchangeCodeForTokenException {
        if (accessToken == null) {
            TokenResponse tokenResponse = authService.getNewTokenResponse(redirectUri, clientId);
            accessToken = tokenResponse.getAccessToken();
            refreshToken = tokenResponse.getRefreshToken();
        }
    }
}
