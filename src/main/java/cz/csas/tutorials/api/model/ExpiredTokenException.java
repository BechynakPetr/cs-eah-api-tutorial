package cz.csas.tutorials.api.model;

/**
 * Thrown when access token has been expired.
 */
public class ExpiredTokenException extends Throwable {
    public ExpiredTokenException(String message) {
    }
}
