package com.belman.belsignqc.BLL.Util;

import com.belman.belsignqc.BE.Users;

/**
 * UserSession is a singleton class that manages the logged-in user session.
 * It provides methods to set and get the currently logged-in user.
 */
public class UserSession {

    // Singleton instance
    private static UserSession instance;

    // The logged-in user
    private Users loggedInUser;

    // Private constructor to prevent instantiation from outside
    private UserSession() {
    }

    /**
     * Gets the singleton instance of UserSession
     * @return The UserSession instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Sets the logged-in user
     * @param user The user to set as logged in
     */
    public void setUser(Users user) {
        this.loggedInUser = user;
    }

    /**
     * Gets the currently logged-in user
     * @return The logged-in user or null if no user is logged in
     */
    public Users getUser() {
        return loggedInUser;
    }

    /**
     * Clears the logged-in user (logout)
     */
    public void clearSession() {
        this.loggedInUser = null;
    }

    /**
     * Checks if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return loggedInUser != null;
    }
}