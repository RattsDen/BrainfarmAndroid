package ca.brainfarm;

import android.util.Log;

import ca.brainfarm.data.User;
import ca.brainfarm.serviceclient.FaultHandler;
import ca.brainfarm.serviceclient.ServiceCall;
import ca.brainfarm.serviceclient.ServiceFaultException;
import ca.brainfarm.serviceclient.SuccessHandler;

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

    public void setCurrentUser(String sessionToken) {
        ServiceCall getUserCall = new ServiceCall("GetCurrentUser");
        getUserCall.addArgument("sessionToken", sessionToken);
        getUserCall.execute(User.class, new SuccessHandler<User>() {
            @Override
            public void handleSuccess(User result) {
                UserSessionManager.getInstance().setCurrentUser(result);
            }
        }, new FaultHandler() {
            @Override
            public void handleFault(ServiceFaultException ex) {
                Log.e("UserSessionManager", "Fault exception", ex);
            }
        });
    }
}
