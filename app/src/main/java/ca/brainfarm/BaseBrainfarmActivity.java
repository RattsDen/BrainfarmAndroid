package ca.brainfarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
        getMenuInflater().inflate(R.menu.menu_logged_out, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: handle menu action item presses
        switch (item.getItemId()) {
            case R.id.menu_register:
                if (!(this instanceof RegisterActivity)) { // If not already on Register activity
                    // Open Register activity
                    Intent intent = new Intent(this, RegisterActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.menu_project:
                // TODO: remove hardcoded navigation
                Intent intent = new Intent(this, ProjectActivity.class);
                intent.putExtra("projectID", 2);
                startActivity(intent);
                break;
        }
        return true;
    }

}
