package ca.brainfarm.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

import ca.brainfarm.R;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.data.Comment;
import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.Project;
import ca.brainfarm.data.SynthesisJunction;
import ca.brainfarm.layouts.CommentLayout;
import ca.brainfarm.layouts.CommentLayoutCallback;
import ca.brainfarm.layouts.ProjectListItemLayout;
import ca.brainfarm.layouts.ProjectListItemLayoutCallback;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class ManageAccountActivity extends BaseBrainfarmActivity
    implements ProjectListItemLayoutCallback, CommentLayoutCallback {

    private TabLayout sectionTabs;
    private LinearLayout tabContentContainer;

    private ArrayList<Project> userProjects;
    private ArrayList<Comment> userComments;
    private ArrayList<Comment> userBookmarkedComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        sectionTabs = (TabLayout)findViewById(R.id.sectionTabs);
        tabContentContainer = (LinearLayout)findViewById(R.id.tabContentContainer);

        sectionTabs.addTab(sectionTabs.newTab().setText("My Projects"));
        sectionTabs.addTab(sectionTabs.newTab().setText("My Comments"));
        sectionTabs.addTab(sectionTabs.newTab().setText("My Bookmarks"));

        sectionTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // projects
                        showUserProjects();
                        break;
                    case 1: // comments
                        showUserComments();
                        break;
                    case 2: // bookmarks
                        showUserBookmarks();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        getUserProjects();
        getUserComments();
        getUserBookmarks();
    }

    private void getUserProjects() {
        ServiceCall serviceCall = new ServiceCall("GetUserProjects");
        serviceCall.addArgument("userID", UserSessionManager.getInstance().getCurrentUser().userID);
        serviceCall.execute(Project[].class, new SuccessHandler<Project[]>() {
            @Override
            public void handleSuccess(Project[] result) {
                // store results
                userProjects = new ArrayList<>(Arrays.asList(result));
                // If projects tab is selected, show results
                if (sectionTabs.getSelectedTabPosition() == 0) {
                    showUserProjects();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void getUserComments() {
        ServiceCall serviceCall = new ServiceCall("GetUserComments");
        serviceCall.addArgument("userID", UserSessionManager.getInstance().getCurrentUser().userID);
        serviceCall.execute(Comment[].class, new SuccessHandler<Comment[]>() {
            @Override
            public void handleSuccess(Comment[] result) {
                // store results
                userComments = new ArrayList<>(Arrays.asList(result));
                // If comments tab is selected, show results
                if (sectionTabs.getSelectedTabPosition() == 1) {
                    showUserComments();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void getUserBookmarks() {
        ServiceCall serviceCall = new ServiceCall("GetUserBookmarkedComments");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.execute(Comment[].class, new SuccessHandler<Comment[]>() {
            @Override
            public void handleSuccess(Comment[] result) {
                // store results
                userBookmarkedComments = new ArrayList<>(Arrays.asList(result));
                // If bookmarks tab is selected, show results
                if (sectionTabs.getSelectedTabPosition() == 2) {
                    showUserBookmarks();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void showUserProjects() {
        tabContentContainer.removeAllViews();
        if (userProjects != null) {
            for (Project project : userProjects) {
                ProjectListItemLayout projectListItem = new ProjectListItemLayout(this, project, this);
                tabContentContainer.addView(projectListItem);
            }
        }
    }

    private void showUserComments() {
        tabContentContainer.removeAllViews();
        if (userComments != null) {
            for (Comment comment : userComments) {
                CommentLayout commentLayout = new CommentLayout(this, comment, this);
                tabContentContainer.addView(commentLayout);
            }
        }
    }

    private void showUserBookmarks() {
        tabContentContainer.removeAllViews();
        if (userBookmarkedComments != null) {
            for (Comment comment : userBookmarkedComments) {
                CommentLayout commentLayout = new CommentLayout(this, comment, this);
                tabContentContainer.addView(commentLayout);
            }
        }
    }

    @Override
    public void projectPressed(ProjectListItemLayout projectListItemLayout) {
        int projectID = projectListItemLayout.getProject().projectID;

        // Go to project activity
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("projectID", projectID);
        startActivity(intent);
    }

    @Override
    public void synthesisLinkPressed(CommentLayout commentView, SynthesisJunction synthesisJunction) {

    }

    @Override
    public void contributionLinkPressed(CommentLayout commentView, ContributionFile contributionFile) {

    }

    @Override
    public void createCommentOptionsPopupMenu(PopupMenu popup, final CommentLayout commentView) {
        popup.getMenuInflater().inflate(R.menu.menu_comment_bookmark, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_comment_goto:
                        // open project activity
                        Intent intent = new Intent(ManageAccountActivity.this, ProjectActivity.class);
                        intent.putExtra("projectID", commentView.getComment().projectID);
                        intent.putExtra("commentID", commentView.getComment().commentID);
                        startActivity(intent);
                }
                return true;
            }
        });
    }
}
