package ca.brainfarm;

import android.app.Application;

import ca.brainfarm.serviceclient.ServiceCall;

/**
 * Application class.
 * Used for doing things that need to be done on startup and whatnot.
 */

public class BrainfarmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set the URL for the service endpoint.
        // It's done from here so it can be grabbed from strings.xml.
        ServiceRequest.baseURL = getString(R.string.service_endpoint_url);
        ServiceCall.defaultBaseURL = getString(R.string.service_endpoint_url);
    }

}
