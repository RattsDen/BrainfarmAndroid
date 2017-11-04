package ca.brainfarm.layouts;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.brainfarm.R;
import ca.brainfarm.data.SynthesisRequest;

/**
 * Created by Eric Thompson on 2017-11-04.
 */

public class SynthesisRequestLayout extends LinearLayout {

    private SynthesisRequest synthesisRequest;
    private ReplyBoxLayout replyBox;

    private Button btnRemove;
    private TextView lblCommentID;
    private EditText txtSubject;

    public SynthesisRequestLayout(Context context, ReplyBoxLayout replyBoxLayout,
                                  SynthesisRequest synthesisRequest) {
        super(context);
        this.synthesisRequest = synthesisRequest;
        this.replyBox = replyBoxLayout;

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_synthesis_request, this, true);

        btnRemove = (Button)findViewById(R.id.btnRemove);
        lblCommentID = (TextView)findViewById(R.id.lblCommentID);
        txtSubject = (EditText)findViewById(R.id.txtSubject);

        // Set control values
        lblCommentID.setText("#" + synthesisRequest.linkedCommentID);
        txtSubject.setText(synthesisRequest.subject);

        //Setup listeners
        btnRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removePressed();
            }
        });

        txtSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                textChanged();
            }
            // Not needed
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
    }

    private void removePressed() {
        replyBox.removeSynthesisRequest(synthesisRequest);
        // Remove self from reply box
        ((ViewGroup)getParent()).removeView(this);
    }

    private void textChanged() {
        synthesisRequest.subject = txtSubject.getText().toString();
    }

}
