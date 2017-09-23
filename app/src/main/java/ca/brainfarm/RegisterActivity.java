package ca.brainfarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.brainfarm.data.User;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class RegisterActivity extends BaseBrainfarmActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtPasswordConfirm;
    private EditText txtEmail;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtPasswordConfirm = (EditText)findViewById(R.id.txtPasswordConfirm);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        btnRegister = (Button)findViewById(R.id.btnRegister);
    }

    public void btnRegisterClicked(View view) {
        // TODO: check that password fields match
        if (passwordFieldsMatch()) {
            // Make service call
            registerUser();
        } else {
            String warning = getString(R.string.warn_password_match);
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean passwordFieldsMatch() {
        String p1 = txtPassword.getText().toString();
        String p2 = txtPasswordConfirm.getText().toString();
        return p1.equals(p2);
    }

    private void registerUser() {
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        String email = txtEmail.getText().toString();

        ServiceCall registerUser = new ServiceCall("RegisterUser");
        registerUser.addArgument("username", username);
        registerUser.addArgument("password", password);
        registerUser.addArgument("email", email);
        registerUser.execute(User.class, new SuccessHandler<User>() {
            @Override
            public void handleSuccess(User result) {
                // Show message
                String message = "Registration Successful!";
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                // Go to login activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("username", result.username); // Pass new user's username to login activity
                startActivity(intent);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(RegisterActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
