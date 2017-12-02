package ca.brainfarm.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ca.brainfarm.R;
import ca.brainfarm.data.Project;
import ca.brainfarm.layouts.ProjectListItemLayout;
import ca.brainfarm.layouts.ProjectListItemLayoutCallback;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class SearchActivity extends BaseBrainfarmActivity implements ProjectListItemLayoutCallback {

    private EditText txtSearchString;
    private Button btnSearch;
    private CheckBox chkTitle;
    private CheckBox chkTags;
    private LinearLayout projectListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get component references
        txtSearchString = (EditText)findViewById(R.id.txtSearchString);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        chkTitle = (CheckBox)findViewById(R.id.chkTitle);
        chkTags = (CheckBox)findViewById(R.id.chkTags);
        projectListContainer = (LinearLayout)findViewById(R.id.projectListContainer);

        setupListeners();
    }

    private void setupListeners() {
        // Perform search when enter is pressed in text box
        txtSearchString.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchProjects();
                    return true;
                }
                return false;
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchProjects();
            }
        });
    }

    private void validateSearchOptions() {
        // If neither title or tag checkboxes are checked, check the title box by default
        if (!(chkTitle.isChecked() || chkTags.isChecked())) {
            chkTitle.setChecked(true);
        }
    }

    private void searchProjects() {

        validateSearchOptions();

        String searchString = txtSearchString.getText().toString();
        boolean searchTitle = chkTitle.isChecked();
        boolean searchTags = chkTags.isChecked();

        ServiceCall searchProjectsCall = new ServiceCall("SearchProjects");
        searchProjectsCall.addArgument("searchKeywordsString", searchString);
        searchProjectsCall.addArgument("searchTitles", searchTitle);
        searchProjectsCall.addArgument("searchTags", searchTags);

        searchProjectsCall.execute(Project[].class, new SuccessHandler<Project[]>() {
            @Override
            public void handleSuccess(Project[] result) {
                displaySearchResults(result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(SearchActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displaySearchResults(Project[] projects) {
        projectListContainer.removeAllViews();

        for (Project project : projects) {
            ProjectListItemLayout projectListItem = new ProjectListItemLayout(this, project, this);
            projectListContainer.addView(projectListItem);
        }
    }

    @Override
    public void projectPressed(ProjectListItemLayout projectListItemLayout) {
        int projectID = projectListItemLayout.getProject().projectID;

        // Go to project activity
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("projectID", projectID);
        startActivity(intent);
    }
}
