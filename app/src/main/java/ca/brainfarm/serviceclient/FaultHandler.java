package ca.brainfarm.serviceclient;

/**
 * Handler interface for handling exception responses from the service.
 */

public interface FaultHandler {

    void handleFault(ServiceFaultException ex);

}
