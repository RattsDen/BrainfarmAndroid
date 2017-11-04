package ca.brainfarm.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.FileAttachmentRequest;
import ca.brainfarm.data.SynthesisRequest;
import ca.brainfarm.data.User;
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

    private static int FILE_SELECT = 1; // Code for identifying intent carrying file select results

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
    public void createCommentOptionsPopupMenu(PopupMenu popup, final CommentLayout commentView) {
        popup.getMenuInflater().inflate(R.menu.menu_comment, popup.getMenu());
        Menu menu = popup.getMenu();

        User currentUser = UserSessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            MenuItem menuCommentEdit = menu.findItem(R.id.menu_comment_edit);
            MenuItem menuCommentDelete = menu.findItem(R.id.menu_comment_delete);
            if (commentView.getComment().userID == currentUser.userID) {
                // Current user is comment owner - make edit and delete options visible
                menuCommentEdit.setVisible(true);
                menuCommentDelete.setVisible(true);
            }
        } else {
            // Current user is null - disable reply and bookmark options
            MenuItem menuCommentReply = menu.findItem(R.id.menu_comment_reply);
            MenuItem menuCommentBookmark = menu.findItem(R.id.menu_comment_bookmark);
            menuCommentBookmark.setEnabled(false);
            menuCommentReply.setEnabled(false);
        }

        // If in synthesis mode, make synthesize option visible
        if (currentReplyBox != null && currentReplyBox.isSynthesisChecked()) {
            MenuItem menuCommentSynth = menu.findItem(R.id.menu_comment_synth);
            menuCommentSynth.setVisible(true);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_comment_reply:
                        replyPressed(commentView);
                        break;
                    case R.id.menu_comment_bookmark:
                        bookmarkPressed(commentView);
                        break;
                    case R.id.menu_comment_edit:
                        editPressed(commentView);
                        break;
                    case R.id.menu_comment_delete:
                        deletePressed(commentView);
                        break;
                    case R.id.menu_comment_synth:
                        synthesizePressed(commentView);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void synthesizePressed(CommentLayout commentView) {
        if (currentReplyBox != null) {
            SynthesisRequest synthesisRequest = new SynthesisRequest();
            synthesisRequest.linkedCommentID = commentView.getComment().commentID;
            synthesisRequest.subject = "";
            currentReplyBox.addSynthesisRequest(synthesisRequest);
        }
    }

    @Override
    public void editPressed(CommentLayout commentView) {
        // Remove old reply box if there is one
        if (currentReplyBox != null) {
            ViewGroup replyBoxParent = (ViewGroup)currentReplyBox.getParent();
            replyBoxParent.removeView(currentReplyBox);
        }
        // Create a new reply box and add it underneath the clicked comment layout
        currentReplyBox = new ReplyBoxLayout(this, commentView.getComment(), this, true);
        commentView.addReplyBox(currentReplyBox);
    }

    @Override
    public void deletePressed(CommentLayout commentView) {
        final String sessionToken = UserSessionManager.getInstance().getLoginToken();
        final int commentId = commentView.getComment().commentID;

        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteComment(sessionToken, commentId);
                    }})
                .setNegativeButton(android.R.string.no, null).show();
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

    @Override
    public void submitEditReplyPressed(ReplyBoxLayout replyBoxLayout){
        int parentCommentID = replyBoxLayout.getParentComment().commentID;
        String bodyText = replyBoxLayout.getReplyBody();
        editComment(parentCommentID, bodyText);
    }

    // Called when the "Choose File" button is pressed on a reply box
    @Override
    public void chooseFilePressed(ReplyBoxLayout replyBoxLayout) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Any MIME file type
        startActivityForResult(intent, FILE_SELECT);
    }

    // Called when an activity started by calling startActivityForResult returns its result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT && resultCode == RESULT_OK) {
            if (currentReplyBox != null) {
                // Clear existing list of files to attach to the new comment
                currentReplyBox.clearFileAttachmentRequests();
                // Set text next to "Upload File" button
                currentReplyBox.setFilePickerLabelText("Uploading...");
                // Get file uri from result
                Uri fileUri = data.getData();
                uploadFile(fileUri);
            }
        }
    }

    private void uploadFile(Uri fileUri) {
        try {
            // Open file stream
            InputStream stream = getContentResolver().openInputStream(fileUri);
            final String filename = getFileNameFromUri(fileUri);

            // Upload
            ServiceCall uploadFileCall = new ServiceCall("UploadFile", ServiceCall.FORMAT_BINARY);
            uploadFileCall.setContentStream(stream);
            uploadFileCall.execute(ContributionFile.class, new SuccessHandler<ContributionFile>() {
                @Override
                public void handleSuccess(ContributionFile result) {
                    fileUploadSuccess(result, filename);
                }
            }, new FaultHandler() {
                @Override
                public void handleFault(ServiceFaultException ex) {
                    // Show toast with exception message
                    Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    if (currentReplyBox != null) {
                        currentReplyBox.setFilePickerLabelText("Upload failed");
                    }
                }
            });

        } catch (FileNotFoundException ex) {
            String message = "Could not find the specified file";
            Toast.makeText(ProjectActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void fileUploadSuccess(ContributionFile contributionFile, String filename) {
        // Create a new FileAttachmentRequest for the newly uploaded file, and add it to the list
        //of FileAttachmentRequests stored in the current reply box
        if (currentReplyBox != null) {
            FileAttachmentRequest fileAttachmentRequest = new FileAttachmentRequest();
            fileAttachmentRequest.contributionFileID = contributionFile.contributionFileID;
            fileAttachmentRequest.filename = filename;
            currentReplyBox.addFileAttachmentRequest(fileAttachmentRequest);
            // Set text next to "Upload File" button
            currentReplyBox.setFilePickerLabelText(filename);
        }
    }

    private void createComment(int parentCommentID, String bodyText) {
        String sessionToken = UserSessionManager.getInstance().getLoginToken();
        ServiceCall createCommentCall = new ServiceCall("CreateComment");

        createCommentCall.addArgument("sessionToken", sessionToken);
        createCommentCall.addArgument("projectID", this.projectID);
        createCommentCall.addArgument("parentCommentID", parentCommentID);
        createCommentCall.addArgument("bodyText", bodyText);
        createCommentCall.addArgument("isSynthesis", currentReplyBox.isSynthesisChecked());
        createCommentCall.addArgument("isContribution", currentReplyBox.isContributionChecked());
        createCommentCall.addArgument("isSpecification", currentReplyBox.isSpecificationChecked());
        createCommentCall.addArgument("syntheses",
                currentReplyBox.isSynthesisChecked() ?
                currentReplyBox.getSynthesisRequests() :
                null
        );
        createCommentCall.addArgument("attachments",
                currentReplyBox.isContributionChecked() ?
                currentReplyBox.getFileAttachmentRequests() :
                null
        );

        createCommentCall.execute(Void.class, new SuccessHandler<Void>() {
            @Override
            public void handleSuccess(Void result) {
                currentReplyBox = null;
                getCommentsFromService();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editComment(int commentID, String bodyText){
        String sessionToken = UserSessionManager.getInstance().getLoginToken();
        ServiceCall editCommentCall = new ServiceCall("EditComment");

        editCommentCall.addArgument("sessionToken", sessionToken);
        editCommentCall.addArgument("commentID", commentID);
        editCommentCall.addArgument("bodyText", bodyText);
        editCommentCall.addArgument("isSynthesis", currentReplyBox.isSynthesisChecked());
        editCommentCall.addArgument("isContribution", currentReplyBox.isContributionChecked());
        editCommentCall.addArgument("isSpecification", currentReplyBox.isSpecificationChecked());
        editCommentCall.addArgument("syntheses", null);

        editCommentCall.execute(Void.class, new SuccessHandler<Void>() {
            @Override
            public void handleSuccess(Void result) {
                currentReplyBox = null;
                getCommentsFromService();
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                // Show toast with exception message
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
                // Show toast with exception message
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper method for extracting file name from a URI
    // Used in method "uploadFile"
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
