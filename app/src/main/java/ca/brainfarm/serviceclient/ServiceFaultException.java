package ca.brainfarm.serviceclient;

/**
 * Exception class used to contain information on Faults thrown by the Brainfarm Service
 */

public class ServiceFaultException extends Exception {

    private String faultCode;
    private String faultSubcode;

    public ServiceFaultException(String message, String faultCode, String faultSubcode) {
        super(message);
        this.faultCode = faultCode;
        this.faultSubcode = faultSubcode;
    }

    public String getFaultCode() {
        return faultCode;
    }

}
