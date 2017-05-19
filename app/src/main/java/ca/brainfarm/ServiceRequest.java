package ca.brainfarm;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used to easily communicate with the Brainfarm service.
 * Use this class to send a request to a service method of the Brainfarm service.
 */

public abstract class ServiceRequest {

    public static String baseURL;

    private String methodName;
    private JSONObject args;

    // If an exception occurs when the request is sent this variable will be set
    private Exception sendingException;


    public ServiceRequest(String methodName) {
        this.methodName = methodName;
        this.args = new JSONObject();
    }

    public ServiceRequest(String methodName, JSONObject args) {
        this.methodName = methodName;
        this.args = args;
    }

    public void execute() {
        new ServiceCallTask().execute();
    }

    protected abstract void onComplete(ServiceResponse response);

    protected void onCommunicationError(Exception ex) {
        Log.e("ServiceRequest", ex.getMessage(), ex);
    }


    private class ServiceCallTask extends AsyncTask<Void, Void, ServiceResponse> {
        @Override
        protected ServiceResponse doInBackground(Void... params) {
            return sendRequest();
        }

        @Override
        protected void onPostExecute(ServiceResponse result) {
            if (result != null) {
                onComplete(result);
            } else {
                onCommunicationError(sendingException);
            }
        }
    }

    private ServiceResponse sendRequest() {
        HttpURLConnection con;
        ServiceResponse serviceResponse = new ServiceResponse();

        try {
            // Setup base connection
            con = getBaseConnection(getServiceMethodURL(methodName));

            // Add arguments to request body
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(args.toString());
            wr.flush();
            wr.close();

            // Send request and receive response
            serviceResponse.responseCode = con.getResponseCode();
            BufferedReader in;
            // For some strange reason, a different stream object is used when the response code is 4xx or 5xx
            // This gets the appropriate one
            if (serviceResponse.responseCode >= 400) {
                in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            String inputLine;
            StringBuilder responseBody = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();

            // Set response object body
            serviceResponse.setResponseBody(responseBody.toString());

            return serviceResponse;

        } catch (Exception ex) {
            sendingException = ex;
            return null;
        }
    }

    private URL getServiceMethodURL(String methodName) throws MalformedURLException {
        return new URL(baseURL + methodName);
    }

    private HttpURLConnection getBaseConnection(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-type", "application/json");

        return con;
    }

}
