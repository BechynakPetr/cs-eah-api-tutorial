package cz.csas.tutorials.api.model;

/**
 * Thrown when refresh token has been expired.
 */
public class ExpiredRefreshTokenException extends Exception {
    public ExpiredRefreshTokenException(String message) {
        super(message);
    }
}
