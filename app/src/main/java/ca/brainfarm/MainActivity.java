package ca.brainfarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

public class MainActivity extends BaseBrainfarmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ALL OF THIS IS TESTING STUFF
        // PLEASE IGNORE


        ServiceResponse resp = new ServiceResponse();
        resp.responseCode = 200;
        resp.setResponseBody("{\"CreationDate\": \"/Date(1493264634133-0400)/\",\"Email\": \"java@mail.com\",\"UserID\": 2008,\"Username\": \"Java\"}");
        try {
            User user = resp.getResponseObject(User.class);
            Log.e("ASD", user.username);
        } catch (Exception ex) {
            Log.e("ASD", ex.getMessage());
        }
        resp.setResponseBody("<Fault xmlns=\"http://schemas.microsoft.com/ws/2005/05/envelope/none\"><Code></Code><Reason><Text xml:lang=\"en-CA\">Invalid email address</Text></Reason></Fault>");
        //ServiceFaultException ex = resp.getException();

        JSONObject args = new JSONObject();

        ServiceRequest req = new ServiceRequest("") {
            @Override
            protected void onComplete(ServiceResponse response) {
                Log.d("COMPLETE", "" + response.responseCode);
            }
        };
        //req.execute();
    }
}
