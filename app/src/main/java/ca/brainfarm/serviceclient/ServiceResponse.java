package ca.brainfarm.serviceclient;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is used to easily communicate with the Brainfarm service.
 * This class wraps the response from the Brainfarm service.
 */

class ServiceResponse {

    public int responseCode;
    private String responseBody;


    public boolean isSuccess() {
        return responseCode == 200;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public <T> T getResponseObject(Class<T> type) throws ServiceFaultException {
        if (isSuccess()) {

            return new GsonBuilder()
                    .registerTypeAdapter(Calendar.class, new DateDeserializer())
                    .create()
                    .fromJson(responseBody, type);
        } else {
            throw getException();
        }
    }

    public ServiceFaultException getException() {

        if (isSuccess()) {
            return null;
        }

        try {

            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(responseBody)));

            String message = "";
            String faultCode = null;
            String faultSubcode = null;

            // Get fault code
            Element codeElement = (Element)xml.getElementsByTagName("Code").item(0);
            if (codeElement != null) {
                Element codeValueElement = (Element)codeElement.getElementsByTagName("Value").item(0);
                if (codeValueElement != null) {
                    faultCode = codeValueElement.getTextContent();
                }
                // Get fault subcode
                Element codeSubcodeElement = (Element)codeElement.getElementsByTagName("Subcode").item(0);
                if (codeSubcodeElement != null) {
                    Element codeSubcodeValueElement = (Element)codeSubcodeElement.getElementsByTagName("Value").item(0);
                    if (codeSubcodeValueElement != null) {
                        faultSubcode = codeSubcodeValueElement.getTextContent();
                    }
                }
            }

            // Get fault message
            Element reasonElement = (Element)xml.getElementsByTagName("Reason").item(0);
            if (reasonElement != null) {
                Element reasonTextElement = (Element) reasonElement.getElementsByTagName("Text").item(0);
                if (reasonTextElement != null) {
                    message = reasonTextElement.getTextContent();
                }
            }

            return new ServiceFaultException(message, faultCode, faultSubcode);

        } catch (SAXException | ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Log.e("ServiceResponse", ex.getMessage(), ex);
        }

        return null;
    }

    private static class DateDeserializer implements JsonDeserializer<Calendar> {
        @Override
        public Calendar deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement == null ? null : parseJsonDate(jsonElement.getAsString());
        }

        private Calendar parseJsonDate(String dateString) {
            // TODO: JSON date parsing is not working correctly

            String millisString = dateString.substring(6, dateString.length() - 7);
            long millis = Long.parseLong(millisString);

            String timezoneString = dateString.substring(dateString.length() - 7, dateString.length() - 4);
            int timezoneOffset;
            if (timezoneString.startsWith("+")) {
                timezoneOffset = Integer.parseInt(timezoneString.substring(1));
            } else {
                timezoneOffset = Integer.parseInt(timezoneString);
            }

            Calendar cal = Calendar.getInstance();
            cal.set(1970, Calendar.JANUARY, 1);
            cal.setTimeInMillis(cal.getTimeInMillis() + millis);

            //cal.add(Calendar.HOUR, timezoneOffset);

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String ds = sdf.format(cal.getTime());

            return cal;
        }
    }

}
