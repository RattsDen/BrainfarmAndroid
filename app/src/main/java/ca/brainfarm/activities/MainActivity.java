package ca.brainfarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

import ca.brainfarm.R;
import ca.brainfarm.UserSessionManager;
import ca.brainfarm.activities.BaseBrainfarmActivity;
import ca.brainfarm.data.Comment;
import ca.brainfarm.data.ContributionFile;
import ca.brainfarm.data.Project;
import ca.brainfarm.data.SynthesisJunction;
import ca.brainfarm.data.User;
import ca.brainfarm.layouts.CommentLayout;
import ca.brainfarm.layouts.CommentLayoutCallback;
import ca.brainfarm.layouts.ProjectListItemLayout;
import ca.brainfarm.layouts.ProjectListItemLayoutCallback;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

public class MainActivity extends BaseBrainfarmActivity
        implements ProjectListItemLayoutCallback, CommentLayoutCallback {

    private Button btnLogin;
    private Button btnCreateProject;
    private Button btnSearchProjects;
    private TabLayout dashboardTabs;
    private LinearLayout tabContentContainer;

    private ArrayList<Project> popularProjects;
    private ArrayList<Project> recommendedProjects;
    private ArrayList<Comment> bookmarkmarkedComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnCreateProject = (Button)findViewById(R.id.btnCreateProject);
        btnSearchProjects = (Button)findViewById(R.id.btnSearchProjects);
        dashboardTabs = (TabLayout)findViewById(R.id.dashboardTabs);
        tabContentContainer = (LinearLayout)findViewById(R.id.tabContentContainer);

        // Set button visibility and tabs based on login state
        dashboardTabs.addTab(dashboardTabs.newTab().setText("Popular Projects"));
        if (UserSessionManager.getInstance().getLoginToken() != null) {
            // logged in
            btnLogin.setVisibility(View.GONE);
            dashboardTabs.addTab(dashboardTabs.newTab().setText("Recommended Projects"));
            dashboardTabs.addTab(dashboardTabs.newTab().setText("Recent Bookmarks"));
        } else {
            // logged out
            btnCreateProject.setVisibility(View.GONE);
        }

        setupListeners();

        getPopularProjects();
        if (UserSessionManager.getInstance().getLoginToken() != null) {
            getRecommendedProjects();
            getRecentBookmarks();
        }

    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateProjectActivity.class);
                startActivity(intent);
            }
        });

        btnSearchProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        dashboardTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Popular projects
                        showPopularProjects();
                        break;
                    case 1: // Recommended projects
                        showRecommendedProjects();
                        break;
                    case 2: // Recent bookmarks
                        showRecentBookmarks();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void getPopularProjects() {
        ServiceCall serviceCall = new ServiceCall("GetPopularProjects");
        serviceCall.addArgument("top", 5);
        serviceCall.execute(Project[].class, new SuccessHandler<Project[]>() {
            @Override
            public void handleSuccess(Project[] result) {
                // store results
                popularProjects = new ArrayList<>(Arrays.asList(result));
                // If popular tab is selected, show results
                if (dashboardTabs.getSelectedTabPosition() == 0) {
                    showPopularProjects();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void getRecommendedProjects() {
        ServiceCall serviceCall = new ServiceCall("GetRecommendedProjects");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.addArgument("top", 5);
        serviceCall.execute(Project[].class, new SuccessHandler<Project[]>() {
            @Override
            public void handleSuccess(Project[] result) {
                // store results
                recommendedProjects = new ArrayList<>(Arrays.asList(result));
                // If recommended tab is selected, show results
                if (dashboardTabs.getSelectedTabPosition() == 1) {
                    showRecommendedProjects();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void getRecentBookmarks() {
        ServiceCall serviceCall = new ServiceCall("GetUserBookmarkedComments");
        serviceCall.addArgument("sessionToken", UserSessionManager.getInstance().getLoginToken());
        serviceCall.execute(Comment[].class, new SuccessHandler<Comment[]>() {
            @Override
            public void handleSuccess(Comment[] result) {
                // store results
                bookmarkmarkedComments = new ArrayList<>(
                        Arrays.asList(Arrays.copyOfRange(result, 0, 5)) // top 5 only
                );
                // If bookamark tab is selected, show results
                if (dashboardTabs.getSelectedTabPosition() == 2) {
                    showRecentBookmarks();
                }
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {

            }
        });
    }

    private void showPopularProjects() {
        tabContentContainer.removeAllViews();
        if (popularProjects != null) {
            for (Project project : popularProjects) {
                ProjectListItemLayout projectListItem = new ProjectListItemLayout(this, project, this);
                tabContentContainer.addView(projectListItem);
            }
        }
    }

    private void showRecommendedProjects() {
        tabContentContainer.removeAllViews();
        if (recommendedProjects != null) {
            for (Project project : recommendedProjects) {
                ProjectListItemLayout projectListItem = new ProjectListItemLayout(this, project, this);
                tabContentContainer.addView(projectListItem);
            }
        }
    }

    private void showRecentBookmarks() {
        tabContentContainer.removeAllViews();
        if (bookmarkmarkedComments != null) {
            for (Comment comment : bookmarkmarkedComments) {
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
                        Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
                        intent.putExtra("projectID", commentView.getComment().projectID);
                        intent.putExtra("commentID", commentView.getComment().commentID);
                        startActivity(intent);
                }
                return true;
            }
        });
    }
}
