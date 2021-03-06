package ca.brainfarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.brainfarm.R;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.data.User;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class LoginActivity extends BaseBrainfarmActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = (EditText)findViewById(R.id.txtUsername);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnRegister = (Button)findViewById(R.id.btnRegister);

        // Get username from intent if it exists
        // Username field is populated automatically when redirecting from RegisterActivity
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            txtUsername.setText(username);
        }

        setupListeners();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        ServiceCall loginCall = new ServiceCall("Login");
        loginCall.addArgument("username", username);
        loginCall.addArgument("password", password);
        loginCall.addArgument("keepLoggedIn", true);
        loginCall.execute(String.class, new SuccessHandler<String>() {
            @Override
            public void handleSuccess(String result) {
                // Store login token
                UserSessionManager.getInstance().setLoginToken(result);
                // Get the current user and store it
                getUser();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Called once the user has logged in to get the current user object and store it
    private void getUser() {
        ServiceCall getUserCall = new ServiceCall("GetCurrentUser");
        getUserCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        getUserCall.execute(User.class, new SuccessHandler<User>() {
            @Override
            public void handleSuccess(User result) {
                // Store current user
                UserSessionManager.getInstance().setCurrentUser(result);
                // Go to main activity
                returnToHome();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void returnToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
        startActivity(intent);
    }
}
