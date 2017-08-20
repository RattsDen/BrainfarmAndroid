package ca.brainfarm.serviceclient;

/**
 * Handler interface for handling successful responses from the service.
 */

public interface SuccessHandler<T> {

    void handleSuccess(T result);

}
