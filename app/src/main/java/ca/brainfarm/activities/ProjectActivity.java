package ca.brainfarm.activities;

import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.brainfarm.layouts.CommentLayout;
import ca.brainfarm.layouts.CommentLayoutCallback;
import ca.brainfarm.R;
import ca.brainfarm.layouts.ReplyBoxLayout;
import ca.brainfarm.layouts.ReplyBoxLayoutCallback;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.data.Comment;
import ca.brainfarm.data.Project;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class ProjectActivity extends BaseBrainfarmActivity
        implements CommentLayoutCallback, ReplyBoxLayoutCallback {

    private int projectID;

    private TextView lblProjectTitle;
    private LinearLayout projectTagContainer;
    private LinearLayout commentContainer;

    private ReplyBoxLayout currentReplyBox;

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
        commentContainer.removeAllViews();
        for (Comment comment : comments) {
            CommentLayout commentLayout = new CommentLayout(this, comment, this);
            commentContainer.addView(commentLayout);
        }
    }

    @Override
    public void replyPressed(CommentLayout commentView) {
        // Remove old reply box if there is one
        if (currentReplyBox != null) {
            ViewGroup replyBoxParent = (ViewGroup)currentReplyBox.getParent();
            replyBoxParent.removeView(currentReplyBox);
        }
        // Create a new reply box and add it underneath the clicked comment layout
        currentReplyBox = new ReplyBoxLayout(this, commentView.getComment(), this);
        commentView.addReplyBox(currentReplyBox);
    }

    @Override
    public void bookmarkPressed(CommentLayout commentView) {

    }

    @Override
    public void editPressed(CommentLayout commentView) {

    }

    @Override
    public void deletePressed(CommentLayout commentView) {
        String sessionToken = UserSessionManager.getInstance().getLoginToken();
        int commentId = commentView.getComment().commentID;
        deleteComment(sessionToken, commentId);
    }

    @Override
    public void cancelReplyPressed(ReplyBoxLayout replyBoxLayout) {
        // Remove old reply box if there is one
        if (currentReplyBox != null) {
            ViewGroup replyBoxParent = (ViewGroup)currentReplyBox.getParent();
            replyBoxParent.removeView(currentReplyBox);
            currentReplyBox = null;
        }
    }

    @Override
    public void submitReplyPressed(ReplyBoxLayout replyBoxLayout) {
        int parentCommentID = replyBoxLayout.getParentComment().commentID;
        String bodyText = replyBoxLayout.getReplyBody();
        createComment(parentCommentID, bodyText);
    }

    private void createComment(int parentCommentID, String bodyText) {
        String sessionToken = UserSessionManager.getInstance().getLoginToken();
        ServiceCall createCommentCall = new ServiceCall("CreateComment");

        createCommentCall.addArgument("sessionToken", sessionToken);
        createCommentCall.addArgument("projectID", this.projectID);
        createCommentCall.addArgument("parentCommentID", parentCommentID);
        createCommentCall.addArgument("bodyText", bodyText);
        createCommentCall.addArgument("isSynthesis", false);
        createCommentCall.addArgument("isContribution", false);
        createCommentCall.addArgument("isSpecification", false);
        createCommentCall.addArgument("syntheses", null);
        createCommentCall.addArgument("fileUploads", null);

        createCommentCall.execute(Void.class, new SuccessHandler<Void>() {
            @Override
            public void handleSuccess(Void result) {
                getCommentsFromService();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void deleteComment(String sessionToken, int commentId){
        ServiceCall deleteCall = new ServiceCall("RemoveComment");
        deleteCall.addArgument("sessionToken", sessionToken);
        deleteCall.addArgument("commentID", commentId);

        deleteCall.execute(Integer.class, new SuccessHandler<Integer>() {
            @Override
            public void handleSuccess(Integer result) {
                getCommentsFromService();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }
}
