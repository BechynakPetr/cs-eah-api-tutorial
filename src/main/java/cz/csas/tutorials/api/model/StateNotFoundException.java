package cz.csas.tutorials.api.model;

/**
 * Thrown when received state is not same as we sent to CSAS.
 */
public class StateNotFoundException extends Exception {
    public StateNotFoundException(String message) {
        super(message);
    }
}
