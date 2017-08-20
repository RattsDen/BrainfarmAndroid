package ca.brainfarm;

import android.os.Bundle;
import android.util.Log;

import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class MainActivity extends BaseBrainfarmActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ALL OF THIS IS TESTING STUFF
        // PLEASE IGNORE

        ServiceCall timestampCall = new ServiceCall("GetTimestamp");
        timestampCall.execute(String.class, new SuccessHandler<String>() {
            @Override
            public void handleSuccess(String result) {
                Log.i("Test", result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });


    }
}
