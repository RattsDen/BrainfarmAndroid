package ca.brainfarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.brainfarm.R;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.data.Project;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class CreateProjectActivity extends BaseBrainfarmActivity {

    private EditText txtProjectTitle;
    private EditText txtProjectTags;
    private EditText txtProjectDescription;
    private Button btnCreateProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        txtProjectTitle = (EditText)findViewById(R.id.txtProjectTitle);
        txtProjectTags = (EditText)findViewById(R.id.txtProjectTags);
        txtProjectDescription = (EditText)findViewById(R.id.txtProjectDescription);
        btnCreateProject = (Button)findViewById(R.id.btnCreateProject);

        setupListeners();
    }

    private void setupListeners() {
        btnCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProject();
            }
        });
    }

    private void createProject() {
        String sessionToken = UserSessionManager.getInstance().getLoginToken();

        String projectTitle = txtProjectTitle.getText().toString();
        String[] projectTags = splitTags(txtProjectTags.getText().toString());
        String projectDescription = txtProjectDescription.getText().toString();

        ServiceCall createProjectCall = new ServiceCall("CreateProject");
        createProjectCall.addArgument("sessionToken", sessionToken);
        createProjectCall.addArgument("title", projectTitle);
        createProjectCall.addArgument("tags", projectTags);
        createProjectCall.addArgument("firstCommentBody", projectDescription);
        createProjectCall.execute(Project.class, new SuccessHandler<Project>() {
            @Override
            public void handleSuccess(Project result) {
                // Go to project activity
                Intent intent = new Intent(CreateProjectActivity.this, ProjectActivity.class);
                intent.putExtra("projectID", result.projectID); // Set id of project to display
                startActivity(intent);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(CreateProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String[] splitTags(String tagsString) {
        return tagsString.trim().split("\\s+");
    }
}
