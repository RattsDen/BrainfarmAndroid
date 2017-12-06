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
import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.SynthesisJunction;

/**
 * Created by Eric Thompson on 2017-09-18.
 */

public class CommentLayout extends RelativeLayout {

    private Comment comment;
    private CommentLayoutCallback callback;

    private TextView lblUsername;
    private TextView lblCreateDate;
    private TextView lblBookmark;
    private TextView lblScore;
    private TextView lblCommentID;
    private TextView lblRibbonProject;
    private TextView lblRibbonSynth;
    private TextView lblRibbonSpec;
    private TextView lblRibbonContrib;
    private TextView lblCommentBody;
    private Button btnCommentOptions;
    private LinearLayout synthesisLinkContainer;
    private LinearLayout attachmentContainer;
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
        lblBookmark = (TextView)findViewById(R.id.lblBookmark);
        lblScore = (TextView)findViewById(R.id.lblScore);
        lblCommentID = (TextView)findViewById(R.id.lblCommentID);
        lblRibbonProject = (TextView)findViewById(R.id.lblRibbonProject);
        lblRibbonSynth = (TextView)findViewById(R.id.lblRibbonSynth);
        lblRibbonSpec = (TextView)findViewById(R.id.lblRibbonSpec);
        lblRibbonContrib = (TextView)findViewById(R.id.lblRibbonContrib);
        lblCommentBody = (TextView)findViewById(R.id.lblCommentBody);
        btnCommentOptions = (Button)findViewById(R.id.btnCommentOptions);
        synthesisLinkContainer = (LinearLayout)findViewById(R.id.synthesisLinkContainer);
        attachmentContainer = (LinearLayout)findViewById(R.id.attachmentContainer);
        commentContentContainer = (LinearLayout)findViewById(R.id.commentContentContainer);
        childCommentContainer = (LinearLayout)findViewById(R.id.childCommentContainer);

        // Set tag for easily finding this view by comment ID later
        setTag(comment.commentID);

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
                createSynthesisLinks();
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
                createContributionLinks();
            }

            lblCommentBody.setText(comment.bodyText);

            setScoreDisplay(comment.score);
        }
        else{
            lblUsername.setText("[COMMENT REMOVED]");
            LinearLayout commentContentContainer = (LinearLayout)findViewById(R.id.commentContentContainer);
            commentContentContainer.setVisibility(INVISIBLE);
        }


    }

    private void createSynthesisLinks() {
        if (comment.syntheses != null) {
            for (SynthesisJunction synthesisJunction : comment.syntheses) {

                // Need a final variable to be referenced inside the event listener
                final SynthesisJunction synthesisJunctionRef = synthesisJunction;

                TextView synthesisView = new TextView(getContext());
                synthesisView.setText("#" + synthesisJunction.linkedCommentID
                        + " " + synthesisJunction.subject);
                synthesisView.setTextColor(
                        ContextCompat.getColor(getContext(), R.color.ribbonSynth));
                synthesisView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.synthesisLinkPressed(CommentLayout.this, synthesisJunctionRef);
                    }
                });

                synthesisLinkContainer.addView(synthesisView);
            }
        }
    }

    private void createContributionLinks() {
        if (comment.contributionFiles != null) {
            for (ContributionFile contributionFile : comment.contributionFiles) {

                // Need a final variable to be referenced inside the event listener
                final ContributionFile contributionFileRef = contributionFile;

                TextView contributionView = new TextView(getContext());
                contributionView.setText(contributionFile.filename);
                contributionView.setTextColor(
                        ContextCompat.getColor(getContext(), R.color.ribbonContrib));
                contributionView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.contributionLinkPressed(CommentLayout.this, contributionFileRef);
                    }
                });

                attachmentContainer.addView(contributionView);
            }
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

    public void setBookmarkVisible(boolean visible) {
        lblBookmark.setVisibility(visible ? VISIBLE : INVISIBLE);
    }

    public void setScoreDisplay(int score) {
        if (score == 0) {
            lblScore.setVisibility(GONE);
        } else {
            lblScore.setVisibility(VISIBLE);
            lblScore.setText(Integer.toString(score));
        }
    }
}
