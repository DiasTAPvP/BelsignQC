package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.Util.ScreenManager;

/**
 * BaseController is an abstract class that serves as a base for all controllers in the application.
 * It provides common functionality such as managing the screen manager and handling user data.
 * Subclasses should extend this class to inherit its functionality.
 */
public abstract class BaseController {
    protected ScreenManager screenManager;
    protected Users currentUser;

    public void setScreenManager(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    public void onScreenActivated() {

    }

    public void receiveData(Object data) {
        // This method can be overridden by subclasses to handle data passed from other controllers
        // Default implementation does nothing
    }

    /**
     * This method is called to receive user data from the UserSession.
     * It can be overridden by subclasses to handle user data as needed.
     *
     * @param user The user data received.
     */
    public void receiveUserData(Users user) {
        this.currentUser = user;
        System.out.println("User received in controller: " + (user != null ? user.getUserID() : "null"));
    }

}