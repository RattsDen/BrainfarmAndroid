package ca.brainfarm;

import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.brainfarm.data.Comment;
import ca.brainfarm.data.Project;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class ProjectActivity extends BaseBrainfarmActivity implements CommentLayoutCallback {

    private int projectID;

    private TextView lblProjectTitle;
    private LinearLayout projectTagContainer;
    private LinearLayout commentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Get project ID from intent
        projectID = getIntent().getExtras().getInt("projectID");

        lblProjectTitle = (TextView)findViewById(R.id.lblProjectTitle);
        projectTagContainer = (LinearLayout)findViewById(R.id.projectTagContainer);
        commentContainer = (LinearLayout)findViewById(R.id.commentContainer);

        getProjectFromService();
        getCommentsFromService();
    }

    private void getProjectFromService() {
        ServiceCall serviceCall = new ServiceCall("GetProject");
        serviceCall.addArgument("projectID", projectID);
        serviceCall.execute(Project.class, new SuccessHandler<Project>() {
            @Override
            public void handleSuccess(Project result) {
                displayProjectInfo(result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Log.e("ProjectActivity", "Fault exception", ex);
            }
        });
    }

    private void getCommentsFromService() {
        ServiceCall serviceCall = new ServiceCall("GetComments");
        serviceCall.addArgument("projectID", projectID);
        serviceCall.addArgument("parentCommentID", null);
        serviceCall.execute(Comment[].class, new SuccessHandler<Comment[]>() {
            @Override
            public void handleSuccess(Comment[] result) {
                layoutComments(result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Log.e("ProjectActivity", "Fault exception", ex);
            }
        });
    }

    private void displayProjectInfo(Project project) {
        lblProjectTitle.setText(project.title);
        for (String tag : project.tags) {
            TextView tagLabel = new TextView(this);
            tagLabel.setText(tag);
            tagLabel.setPadding(5, 5, 5, 5);
            tagLabel.setTextColor(ContextCompat.getColor(this, R.color.colorGrey));
            projectTagContainer.addView(tagLabel);
        }
    }

    private void layoutComments(Comment[] comments) {
        for (Comment comment : comments) {
            CommentLayout commentLayout = new CommentLayout(this, comment, this);
            commentContainer.addView(commentLayout);
        }
    }

    @Override
    public void replyPressed(CommentLayout commentView) {

    }
}
