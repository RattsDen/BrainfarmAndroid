package ca.brainfarm.layouts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ca.brainfarm.R;
import ca.brainfarm.data.Project;

/**
 * Created by Eric Thompson on 2017-10-30.
 */

public class ProjectListItemLayout extends LinearLayout {

    private Project project;
    private ProjectListItemLayoutCallback callback;

    private TextView lblProjectTitle;
    private TextView lblUsername;
    private TextView lblCreateDate;
    private LinearLayout tagContainer;

    public ProjectListItemLayout(Context context) {
        super(context);
    }

    public ProjectListItemLayout(Context context, Project project,
                                 ProjectListItemLayoutCallback callback) {
        super(context);
        this.project = project;
        this.callback = callback;

        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_project_list_item, this, true);

        // Get component references
        lblProjectTitle = (TextView)findViewById(R.id.lblProjectTitle);
        lblUsername = (TextView)findViewById(R.id.lblUsername);
        lblCreateDate = (TextView)findViewById(R.id.lblCreateDate);
        tagContainer = (LinearLayout)findViewById(R.id.tagContainer);

        setComponentValues();
        setupListeners();

    }

    private void setComponentValues() {
        lblProjectTitle.setText(project.title);
        lblUsername.setText("by " + project.username);
        lblCreateDate.setText(new SimpleDateFormat("yyyy-MM-dd h:mm")
                .format(project.creationDate.getTime()));
        for(TextView tagView : createTagViews()) {
            tagContainer.addView(tagView);
        }
    }

    private ArrayList<TextView> createTagViews() {
        ArrayList<TextView> tagViews = new ArrayList<>();
        for(String tag : project.tags) {
            TextView tagView = new TextView(getContext());
            tagView.setText(tag);
            tagView.setPadding(5, 5, 5, 5);
            tagView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            tagViews.add(tagView);
        }
        return tagViews;
    }

    private void setupListeners() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                projectPressed();
            }
        });
    }

    private void projectPressed() {
        callback.projectPressed(this);
    }

    public Project getProject() {
        return project;
    }

}
