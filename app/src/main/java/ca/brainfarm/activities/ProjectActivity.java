package ca.brainfarm.activities;

import android.app.Service;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ca.brainfarm.ContributionFileDownloader;
import ca.brainfarm.data.Bookmark;
import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.FileAttachmentRequest;
import ca.brainfarm.data.Rating;
import ca.brainfarm.data.SynthesisJunction;
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
    private Integer commentID;

    private TextView lblProjectTitle;
    private LinearLayout projectTagContainer;
    private LinearLayout commentContainer;
    private ScrollView pageScroll;

    private ReplyBoxLayout currentReplyBox;

    private ArrayList<Integer> bookmarkedCommentIDs = new ArrayList<>();
    private ArrayList<Rating> userRatings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Get project ID from intent
        projectID = getIntent().getExtras().getInt("projectID");

        // Get ID of comment to scroll to once comments are loaded, if specified
        // and if the activity was not just re-created (savedInstanceState == null)
        if (getIntent().getExtras().containsKey("commentID")
                && savedInstanceState == null) {
            commentID = getIntent().getExtras().getInt("commentID");
        }


        lblProjectTitle = (TextView)findViewById(R.id.lblProjectTitle);
        projectTagContainer = (LinearLayout)findViewById(R.id.projectTagContainer);
        commentContainer = (LinearLayout)findViewById(R.id.commentContainer);
        pageScroll = (ScrollView)findViewById(R.id.pageScroll);

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
                // If logged in get bookmarks and ratings
                if (UserSessionManager.getInstance().getLoginToken() != null) {
                    getBookmarksFromService();
                    getRatingsFromService();
                }
                // If a commentID was specified in the intent scroll to it
                if (commentID != null) {
                    scrollToComment(commentID);
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Log.e("ProjectActivity", "Fault exception", ex);
            }
        });
    }

    private void getBookmarksFromService() {
        ServiceCall serviceCall = new ServiceCall("GetBookmarksForProject");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("projectID", projectID);
        serviceCall.execute(int[].class, new SuccessHandler<int[]>() {
            @Override
            public void handleSuccess(int[] result) {
                for (int id : result) {
                    // Add id to list
                    bookmarkedCommentIDs.add(id);
                    // Set bookmark label to visible
                    ((CommentLayout)commentContainer.findViewWithTag(id)).setBookmarkVisible(true);
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void getRatingsFromService() {
        ServiceCall serviceCall = new ServiceCall("GetUserRatings");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("projectID", projectID);
        serviceCall.execute(Rating[].class, new SuccessHandler<Rating[]>() {
            @Override
            public void handleSuccess(Rating[] result) {
                for (Rating rating : result) {
                    userRatings.add(rating);
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private boolean isCommentBookmarked(int commentID) {
        return bookmarkedCommentIDs.contains(commentID);
    }

    private boolean isCommentLiked(int commentID) {
        for (int i = 0; i < userRatings.size(); i++) {
            if (userRatings.get(i).commentID == commentID)
                return true;
        }
        return false;
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

    private void scrollToComment(int commentID) {
        // Scroll to top of child container so that the whole comment can be seen
        final View scrollTo = commentContainer.findViewWithTag(commentID)
                .findViewById(R.id.childCommentContainer);
        pageScroll.post(new Runnable() {
            @Override
            public void run() {
                pageScroll.smoothScrollTo(0, scrollTo.getTop());
            }
        });
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
            MenuItem menuCommentLike = menu.findItem(R.id.menu_comment_like);
            menuCommentBookmark.setEnabled(false);
            menuCommentReply.setEnabled(false);
            menuCommentLike.setEnabled(false);
        }

        // If in synthesis mode, make synthesize option visible
        if (currentReplyBox != null && currentReplyBox.isSynthesisChecked()) {
            MenuItem menuCommentSynth = menu.findItem(R.id.menu_comment_synth);
            menuCommentSynth.setVisible(true);
        }

        // If comment is bookmarked, show "Unbookmark" item instead
        if (isCommentBookmarked(commentView.getComment().commentID)) {
            MenuItem menuCommentBookmark = menu.findItem(R.id.menu_comment_bookmark);
            MenuItem menuCommentUnbookmark = menu.findItem(R.id.menu_comment_unbookmark);
            menuCommentBookmark.setVisible(false);
            menuCommentUnbookmark.setVisible(true);
        }

        // If comment is liked, show "Unlike" item instead
        if (isCommentLiked(commentView.getComment().commentID)) {
            MenuItem menuCommentLike = menu.findItem(R.id.menu_comment_like);
            MenuItem menuCommentUnlike = menu.findItem(R.id.menu_comment_unlike);
            menuCommentLike.setVisible(false);
            menuCommentUnlike.setVisible(true);
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
                    case R.id.menu_comment_unbookmark:
                        unbookmarkPressed(commentView);
                        break;
                    case R.id.menu_comment_like:
                        likePressed(commentView);
                        break;
                    case R.id.menu_comment_unlike:
                        unlikePressed(commentView);
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

    public void bookmarkPressed(final CommentLayout commentView) {
        ServiceCall serviceCall = new ServiceCall("BookmarkComment");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("commentID", commentView.getComment().commentID);
        serviceCall.execute(Bookmark.class, new SuccessHandler<Bookmark>() {
            @Override
            public void handleSuccess(Bookmark result) {
                bookmarkedCommentIDs.add(result.commentID);
                commentView.setBookmarkVisible(true);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void unbookmarkPressed(final CommentLayout commentView) {
        ServiceCall serviceCall = new ServiceCall("UnbookmarkComment");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("commentID", commentView.getComment().commentID);
        serviceCall.execute(Bookmark.class, new SuccessHandler<Bookmark>() {
            @Override
            public void handleSuccess(Bookmark result) {
                // Casting an int to Integer causes the remove(Object o) method to be called
                //instead of the remove(int index) method
                bookmarkedCommentIDs.remove((Integer)result.commentID);
                commentView.setBookmarkVisible(false);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void likePressed(final CommentLayout commentView) {
        ServiceCall serviceCall = new ServiceCall("AddRating");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("commentID", commentView.getComment().commentID);
        serviceCall.execute(Rating.class, new SuccessHandler<Rating>() {
            @Override
            public void handleSuccess(Rating result) {
                userRatings.add(result);
                commentView.getComment().score += result.weight;
                commentView.setScoreDisplay(commentView.getComment().score);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void unlikePressed(final CommentLayout commentView) {
        ServiceCall serviceCall = new ServiceCall("RemoveRating");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("commentID", commentView.getComment().commentID);
        serviceCall.execute(Rating.class, new SuccessHandler<Rating>() {
            @Override
            public void handleSuccess(Rating result) {
                // Remove rating from list.
                // Can't use userRatings.remove(result) since the result object, and the one in the
                // list aren't actually the same
                for (int i = 0; i < userRatings.size(); i++) {
                    if (userRatings.get(i).commentID == result.commentID) {
                        userRatings.remove(i);
                    }
                }
                commentView.getComment().score -= result.weight;
                commentView.setScoreDisplay(commentView.getComment().score);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Toast.makeText(ProjectActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void synthesizePressed(CommentLayout commentView) {
        if (currentReplyBox != null) {
            SynthesisRequest synthesisRequest = new SynthesisRequest();
            synthesisRequest.linkedCommentID = commentView.getComment().commentID;
            synthesisRequest.subject = "";
            currentReplyBox.addSynthesisRequest(synthesisRequest);
        }
    }

    @Override
    public void synthesisLinkPressed(CommentLayout commentView, SynthesisJunction synthesisJunction) {
        scrollToComment(synthesisJunction.linkedCommentID);
    }

    @Override
    public void contributionLinkPressed(CommentLayout commentView, ContributionFile contributionFile) {
        ContributionFileDownloader downloader
                = new ContributionFileDownloader(this, contributionFile);
        downloader.startDownload();
    }

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
            ServiceCall uploadFileCall = new ServiceCall(
                    "UploadFile",               // UploadFile service method
                    ServiceCall.FORMAT_BINARY,  // Request data is binary
                    ServiceCall.FORMAT_JSON);   // Expect response to be JSON
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
        editCommentCall.addArgument("syntheses",
                currentReplyBox.isSynthesisChecked() ?
                currentReplyBox.getSynthesisRequests() :
                null
        );

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
