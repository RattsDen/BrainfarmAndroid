package ca.brainfarm.layouts;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ca.brainfarm.R;
import ca.brainfarm.data.Comment;
import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.FileAttachmentRequest;
import ca.brainfarm.data.SynthesisRequest;

/**
 * Created by Eric Thompson on 2017-09-22.
 */

public class ReplyBoxLayout extends RelativeLayout {

    private Comment parentComment;
    private ReplyBoxLayoutCallback callback;

    private EditText txtCommentBody;
    private CheckBox chkIsSpecification;
    private CheckBox chkIsSynthesis;
    private CheckBox chkIsContribution;
    private LinearLayout synthesisPanel;
    private LinearLayout contributionPanel;
    private Button btnChooseFile;
    private TextView lblFilename;
    private Button btnSubmitComment;
    private Button btnCancel;
    private TextView lblReplyTitle;

    private boolean isEditBox = false;

    private ArrayList<FileAttachmentRequest> fileAttachmentRequests = new ArrayList<>();
    private ArrayList<SynthesisRequest> synthesisRequests = new ArrayList<>();

    public ReplyBoxLayout(Context context, Comment parentComment, ReplyBoxLayoutCallback callback) {
        super(context);
        this.parentComment = parentComment;
        this.callback = callback;

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_reply_box, this, true);

        txtCommentBody = (EditText)findViewById(R.id.txtCommentBody);
        chkIsSpecification = (CheckBox)findViewById(R.id.chkIsSpecification);
        chkIsSynthesis = (CheckBox)findViewById(R.id.chkIsSynthesis);
        chkIsContribution = (CheckBox)findViewById(R.id.chkIsContribution);
        synthesisPanel = (LinearLayout)findViewById(R.id.synthesisPanel);
        contributionPanel = (LinearLayout)findViewById(R.id.contributionPanel);
        btnChooseFile = (Button)findViewById(R.id.btnChooseFile);
        lblFilename = (TextView)findViewById(R.id.lblFilename);
        btnSubmitComment = (Button)findViewById(R.id.btnSubmitComment);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        lblReplyTitle = (TextView)findViewById(R.id.lblReplyTitle);

        setupListeners();
    }

    public ReplyBoxLayout(Context context, Comment parentComment, ReplyBoxLayoutCallback callback, boolean isEditBox){
        this(context, parentComment, callback);
        this.isEditBox = isEditBox;
        txtCommentBody.setText(parentComment.bodyText);
        setupListeners();
    }

    private void setupListeners() {
        if(this.isEditBox){
            lblReplyTitle.setText("Edit Comment");
            btnSubmitComment.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitEditPressed();
                }
            });
        }
        else{
            btnSubmitComment.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitPressed();
                }
            });
        }

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPressed();
            }
        });

        chkIsSynthesis.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                synthesisPanel.setVisibility(chkIsSynthesis.isChecked() ? VISIBLE : GONE);
            }
        });

        chkIsContribution.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                contributionPanel.setVisibility(chkIsContribution.isChecked() ? VISIBLE : GONE);
            }
        });

        btnChooseFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFileButtonPressed();
            }
        });
    }

    private void chooseFileButtonPressed() {
        callback.chooseFilePressed(this);
    }

    private void submitPressed() {
        btnSubmitComment.setEnabled(false);
        callback.submitReplyPressed(this);
    }

    private void submitEditPressed(){
        btnSubmitComment.setEnabled(false);
        callback.submitEditReplyPressed(this);
    }

    private void cancelPressed() {
        callback.cancelReplyPressed(this);
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public String getReplyBody() {
        return txtCommentBody.getText().toString();
    }

    public void setFilePickerLabelText(String text) {
        lblFilename.setText(text);
    }

    public void addFileAttachmentRequest(FileAttachmentRequest fileAttachmentRequest) {
        fileAttachmentRequests.add(fileAttachmentRequest);
    }

    public void clearFileAttachmentRequests() {
        fileAttachmentRequests.clear();
    }

    public ArrayList<FileAttachmentRequest> getFileAttachmentRequests() {
        return new ArrayList<>(fileAttachmentRequests);
    }

    public void addSynthesisRequest(SynthesisRequest synthesisRequest) {
        synthesisRequests.add(synthesisRequest);
        SynthesisRequestLayout synthesisRequestLayout
                = new SynthesisRequestLayout(getContext(), this, synthesisRequest);
        synthesisPanel.addView(synthesisRequestLayout);
    }

    public void removeSynthesisRequest(SynthesisRequest synthesisRequest) {
        synthesisRequests.remove(synthesisRequest);
    }

    public ArrayList<SynthesisRequest> getSynthesisRequests() {
        return new ArrayList<>(synthesisRequests);
    }

    public boolean isSpecificationChecked() {
        return chkIsSpecification.isChecked();
    }

    public boolean isSynthesisChecked() {
        return chkIsSynthesis.isChecked();
    }

    public boolean isContributionChecked() {
        return chkIsContribution.isChecked();
    }

}
