package ca.brainfarm;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

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
        registerUser();
    }

    private void registerUser() {
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        String email = txtEmail.getText().toString();

        ServiceCall registerUser = new ServiceCall("RegisterUser");
        registerUser.addArgument("username", username);
        registerUser.addArgument("password", password);
        registerUser.addArgument("email", email);
        registerUser.execute(Boolean.class, new SuccessHandler<Boolean>() {
            @Override
            public void handleSuccess(Boolean result) {

            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }
}
