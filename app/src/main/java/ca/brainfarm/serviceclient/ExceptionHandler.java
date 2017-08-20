package ca.brainfarm.serviceclient;

/**
 * Handler interface for handling any exceptions that occur during communication with the service.
 */

public interface ExceptionHandler {

    void handleException(Exception ex);

}
