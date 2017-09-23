package ca.brainfarm;

import ca.brainfarm.data.User;

/**
 * Created by Eric Thompson on 2017-09-22.
 */

public class UserSessionManager {

    // Singleton stuff
    private static UserSessionManager instance = new UserSessionManager();

    private UserSessionManager() {
        loginToken = null;
    }

    public static UserSessionManager getInstance() {
        return instance;
    }
    // End singleton stuff

    private String loginToken;
    private User currentUser;

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
