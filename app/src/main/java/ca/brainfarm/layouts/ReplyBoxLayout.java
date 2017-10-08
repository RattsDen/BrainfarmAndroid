package ca.brainfarm.layouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import ca.brainfarm.R;
import ca.brainfarm.data.Comment;

/**
 * Created by Eric Thompson on 2017-09-22.
 */

public class ReplyBoxLayout extends RelativeLayout {

    private Comment parentComment;
    private ReplyBoxLayoutCallback callback;

    private EditText txtCommentBody;
    private Button btnSubmitComment;
    private Button btnCancel;

    public ReplyBoxLayout(Context context, Comment parentComment, ReplyBoxLayoutCallback callback) {
        super(context);
        this.parentComment = parentComment;
        this.callback = callback;

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_reply_box, this, true);

        txtCommentBody = (EditText)findViewById(R.id.txtCommentBody);
        btnSubmitComment = (Button)findViewById(R.id.btnSubmitComment);
        btnCancel = (Button)findViewById(R.id.btnCancel);

        setupListeners();
    }

    private void setupListeners() {
        btnSubmitComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPressed();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPressed();
            }
        });
    }

    private void submitPressed() {
        btnSubmitComment.setEnabled(false);
        callback.submitReplyPressed(this);
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

}
