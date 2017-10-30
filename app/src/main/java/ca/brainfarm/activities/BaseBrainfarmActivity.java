package ca.brainfarm.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ca.brainfarm.R;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.data.User;

/**
 * Created by Eric Thompson on 2017-05-08.
 */

public abstract class BaseBrainfarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add common onCreate code here

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Create the menu from xml
        // Menu shown should depend on whether user is logged in or not
        if (UserSessionManager.getInstance().getLoginToken() == null) {
            // Logged out
            getMenuInflater().inflate(R.menu.menu_logged_out, menu);
        } else {
            // Logged in
            getMenuInflater().inflate(R.menu.menu_logged_in, menu);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                loginMenuItemPressed();
                break;

            case R.id.menu_logout:
                logoutMenuItemPressed();
                break;

            case R.id.menu_register:
                registerMenuItemPressed();
                break;

            case R.id.menu_create_project:
                createProjectMenuItemPressed();
                break;

            case R.id.menu_search:
                searchProjectssMenuItemPressed();
                break;
        }
        return true;
    }

    protected void loginMenuItemPressed() {
        if (!(this instanceof LoginActivity)) { // If not already on login activity
            // Open login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    protected void logoutMenuItemPressed() {
        // Clear stored session token
        UserSessionManager.getInstance().setLoginToken(null);
        UserSessionManager.getInstance().setCurrentUser((User)null);
        // Go to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
        if (this instanceof MainActivity) {
            // Don't animate the activity transition if we're already on the main activity
            // It can look strange otherwise
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivity(intent);
    }

    protected void registerMenuItemPressed() {
        if (!(this instanceof RegisterActivity)) { // If not already on Register activity
            // Open Register activity
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    protected void createProjectMenuItemPressed() {
        if (!(this instanceof CreateProjectActivity)) { // If not already on Create Project activity
            // Open Create Project activity
            Intent intent = new Intent(this, CreateProjectActivity.class);
            startActivity(intent);
        }
    }

    protected void searchProjectssMenuItemPressed() {
        if (!(this instanceof SearchActivity)) { // If not already on Search activity
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

}
