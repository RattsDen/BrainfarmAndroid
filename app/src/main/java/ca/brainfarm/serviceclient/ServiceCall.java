package ca.brainfarm.serviceclient;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * This class is used to make calls to the Brainfarm service.
 * Calls are made over HTTP using JSON.
 */

public class ServiceCall {

    public static final int FORMAT_JSON = 0;
    public static final int FORMAT_BINARY = 1;

    public static String defaultBaseURL;

    private String baseURL;
    private String resource;
    private int requestFormat;
    private int responseFormat;
    private JsonObject arguments = new JsonObject();
    private InputStream contentStream = null;

    // If an exception occurs when the request is sent this variable will be set
    private Exception sendingException = null;

    public ServiceCall(String resource) {
        this(resource, FORMAT_JSON, FORMAT_JSON);
    }

    public ServiceCall(String resource, int requestFormat, int responseFormat) {
        baseURL = defaultBaseURL;
        this.resource = resource;
        this.requestFormat = requestFormat;
        this.responseFormat = responseFormat;
    }

    public void addArgument(String name, Object data) {
        arguments.add(name, new Gson().toJsonTree(data));
    }

    public void setContentStream(InputStream stream) {
        contentStream = stream;
    }

    public <T> void execute(Class<T> expect, SuccessHandler<T> success, FaultHandler fault) {
        new ServiceCallTask<>(expect, success, fault).execute();
    }

    public <T> void execute(Class<T> expect, SuccessHandler<T> success, FaultHandler fault,
                            ExceptionHandler exception) {
        new ServiceCallTask<>(expect, success, fault, exception).execute();
    }

    public void executeForStream(SuccessHandler<InputStream> success, FaultHandler fault) {
        new ServiceCallTask<>(InputStream.class, success, fault).execute();
    }

    private class ServiceCallTask<T> extends AsyncTask<Void, Void, ServiceResponse> {

        private Class<T> expect;
        private SuccessHandler<T> successHandler;
        private FaultHandler faultHandler;
        private ExceptionHandler exceptionHandler;

        protected ServiceCallTask(Class<T> expect, SuccessHandler<T> successHandler,
                                  FaultHandler faultHandler) {
            this.expect = expect;
            this.successHandler = successHandler;
            this.faultHandler = faultHandler;
            this.exceptionHandler = new ExceptionHandler() {
                @Override
                public void handleException(Exception ex) {
                    Log.e("ServiceRequest", ex.getMessage(), ex);
                }
            };
        }

        protected ServiceCallTask(Class<T> expect, SuccessHandler<T> successHandler,
                                  FaultHandler faultHandler, ExceptionHandler exceptionHandler) {
            this.expect = expect;
            this.successHandler = successHandler;
            this.faultHandler = faultHandler;
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        protected ServiceResponse doInBackground(Void... params) {
            return sendRequest();
        }

        @Override
        protected void onPostExecute(ServiceResponse result) {
            if (sendingException == null) {
                //onComplete(result);
                try {
                    // JSON response - get POJO of expected class
                    if (responseFormat == FORMAT_JSON) {
                        successHandler.handleSuccess(expect.cast(result.getResponseObject(expect)));

                    // Binary response - get content stream - EXPECT MUST BE InputStream !!
                    } else if (responseFormat == FORMAT_BINARY) {
                        if (expect == InputStream.class) {
                            successHandler.handleSuccess(expect.cast(result.getContentStream()));
                        }
                    }
                } catch (ServiceFaultException ex) {
                    faultHandler.handleFault(ex);
                }
            } else {
                //onCommunicationError(sendingException);
                exceptionHandler.handleException(sendingException);
            }
        }
    }

    private ServiceResponse sendRequest() {
        HttpURLConnection con;
        ServiceResponse serviceResponse = new ServiceResponse();

        try {
            // Setup base connection
            con = getBaseConnection(getResourceURL(resource));

            // Add arguments to request body
            if (requestFormat == FORMAT_JSON) {
                writeRequestContentJSON(con.getOutputStream());
            } else if (requestFormat == FORMAT_BINARY) {
                writeRequestContentBinary(con.getOutputStream());
            }

            // Send request and receive response
            serviceResponse.responseCode = con.getResponseCode();

            InputStream stream;
            // For some strange reason, a different stream object is used when the
            // response code is 4xx or 5xx. This gets the appropriate one.
            if (serviceResponse.responseCode >= 400) {
                stream = con.getErrorStream();
            } else {
                stream = con.getInputStream();
            }

            // Store the response in the appropriate format
            if (responseFormat == FORMAT_JSON || serviceResponse.responseCode >= 400) {
                // Response is either JSON or a bad response code
                storeResponseContentJSON(stream, serviceResponse);
            } else if (responseFormat == FORMAT_BINARY) {
                // Response is binary
                storeResponseContentBinary(stream, serviceResponse);
            }

            return serviceResponse;

        } catch (Exception ex) {
            sendingException = ex;
            return null;
        }
    }

    private void writeRequestContentJSON(OutputStream outputStream) throws IOException {
        // JSON format
        // write json string
        OutputStreamWriter wr = new OutputStreamWriter(outputStream);
        wr.write(arguments.toString());
        wr.flush();
        wr.close();
    }

    private void writeRequestContentBinary(OutputStream outputStream) throws IOException {
        // Binary format
        if (contentStream != null) {
            // Copy from input stream to output stream
            byte[] buffer = new byte[8 * 1024];
            int length;
            while ((length = contentStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            // Close input stream, be courteous
            contentStream.close();
            // Flush and close output stream
            outputStream.flush();
            outputStream.close();
        }
    }

    private void storeResponseContentJSON(InputStream inputStream,
                                          ServiceResponse serviceResponse) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder responseBody = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            responseBody.append(inputLine);
        }
        reader.close();

        // Set response object body
        serviceResponse.setResponseBody(responseBody.toString());
    }

    private void storeResponseContentBinary(InputStream inputStream,
                                            ServiceResponse serviceResponse) throws IOException {
        serviceResponse.setContentStream(inputStream);
    }

    private URL getResourceURL(String methodName) throws MalformedURLException {
        return new URL(baseURL + methodName);
    }

    private HttpURLConnection getBaseConnection(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        if (requestFormat == FORMAT_JSON) {
            con.setRequestProperty("Content-type", "application/json");
        }

        return con;
    }

}
