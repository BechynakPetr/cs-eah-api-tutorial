package cz.csas.tutorials.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO object to hold response (from /token endpoint) data containing token
 */
@Data
public class TokenResponse {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    Long expiresIn;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("scope")
    String scope;
}
