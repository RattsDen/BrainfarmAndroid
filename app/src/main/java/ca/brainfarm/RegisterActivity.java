package ca.brainfarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends BaseBrainfarmActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtPasswordConfirm;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtPasswordConfirm = (EditText)findViewById(R.id.txtPasswordConfirm);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
    }

    public void btnRegisterClicked(View view) {
        // TODO: check that password fields match
        sendRegistrationRequest();
    }

    private void sendRegistrationRequest() {
        JSONObject args = new JSONObject();
        try {
            args.put("username", txtUsername.getText().toString());
            args.put("password", txtPassword.getText().toString());
            args.put("email", txtEmail.getText().toString());
        } catch (JSONException ignored) {} // It is impossible to enter this block. Thanks, Java.

        ServiceRequest req = new ServiceRequest("RegisterUser", args) {
            @Override
            protected void onComplete(ServiceResponse response) {

                try {
                    handleRegistrationResponse(response.getResponseObject(Boolean.class));
                } catch (ServiceFaultException ex) {
                    Toast.makeText(RegisterActivity.this,
                            ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    Toast.makeText(RegisterActivity.this,
                            "An unexpected error occurred",
                            Toast.LENGTH_LONG).show();
                }

            }
        };
        req.execute();
    }

    private void handleRegistrationResponse(boolean success) {

    }
}
