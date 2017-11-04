package ca.brainfarm.layouts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ca.brainfarm.R;
import ca.brainfarm.data.Comment;

/**
 * Created by Eric Thompson on 2017-09-18.
 */

public class CommentLayout extends RelativeLayout {

    private Comment comment;
    private CommentLayoutCallback callback;

    private TextView lblUsername;
    private TextView lblCreateDate;
    private TextView lblCommentID;
    private TextView lblRibbonProject;
    private TextView lblRibbonSynth;
    private TextView lblRibbonSpec;
    private TextView lblRibbonContrib;
    private TextView lblCommentBody;
    private Button btnCommentOptions;
    private LinearLayout commentContentContainer;
    private LinearLayout childCommentContainer;

    public CommentLayout(Context context) {
        super(context);
    }

    public CommentLayout(Context context, Comment comment, CommentLayoutCallback callback) {
        super(context);
        this.comment = comment;
        this.callback = callback;

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_comment, this, true);

        // Get component references
        lblUsername = (TextView)findViewById(R.id.lblUsername);
        lblCreateDate = (TextView)findViewById(R.id.lblCreateDate);
        lblCommentID = (TextView)findViewById(R.id.lblCommentID);
        lblRibbonProject = (TextView)findViewById(R.id.lblRibbonProject);
        lblRibbonSynth = (TextView)findViewById(R.id.lblRibbonSynth);
        lblRibbonSpec = (TextView)findViewById(R.id.lblRibbonSpec);
        lblRibbonContrib = (TextView)findViewById(R.id.lblRibbonContrib);
        lblCommentBody = (TextView)findViewById(R.id.lblCommentBody);
        btnCommentOptions = (Button)findViewById(R.id.btnCommentOptions);
        commentContentContainer = (LinearLayout)findViewById(R.id.commentContentContainer);
        childCommentContainer = (LinearLayout)findViewById(R.id.childCommentContainer);

        setComponentValues();
        setupListeners();
        createChildCommentViews();
    }

    private void setupListeners() {
        btnCommentOptions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), btnCommentOptions);
                callback.createCommentOptionsPopupMenu(popup, CommentLayout.this);
                popup.show();
            }
        });
    }

    private void setComponentValues() {
        if(!comment.isRemoved){
            lblUsername.setText(comment.username);
            lblCreateDate.setText(new SimpleDateFormat("yyyy-MM-dd h:mm")
                    .format(comment.creationDate.getTime()));
            lblCommentID.setText("#" + Integer.toString(comment.commentID));

            if (comment.parentCommentID == null) {
                lblRibbonProject.setVisibility(VISIBLE);
                commentContentContainer.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.commentBackgroundProject));
            }
            if (comment.isSynthesis) {
                lblRibbonSynth.setVisibility(VISIBLE);
                commentContentContainer.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.commentBackgroundSynth));
            }
            if (comment.isSpecification) {
                lblRibbonSpec.setVisibility(VISIBLE);
                commentContentContainer.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.commentBackgroundSpec));
            }
            if (comment.isContribution) {
                lblRibbonContrib.setVisibility(VISIBLE);
                commentContentContainer.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.commentBackgroundContrib));
            }

            lblCommentBody.setText(comment.bodyText);
        }
        else{
            lblUsername.setText("[COMMENT REMOVED]");
            LinearLayout commentContentContainer = (LinearLayout)findViewById(R.id.commentContentContainer);
            commentContentContainer.setVisibility(INVISIBLE);
        }


    }

    private void createChildCommentViews() {
        for (Comment child : comment.children) {
            CommentLayout commentLayout =
                    new CommentLayout(this.getContext(), child, this.callback);
            childCommentContainer.addView(commentLayout);
        }
    }

    public Comment getComment() {
        return comment;
    }

    public void addReplyBox(ReplyBoxLayout replyBoxLayout) {
        childCommentContainer.addView(replyBoxLayout, 0);
    }
}
