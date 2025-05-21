package com.belman.belsignqc.GUI.Model;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.UserManager;
import com.belman.belsignqc.BLL.Util.BCryptUtil;
import com.belman.belsignqc.BLL.Util.UserSession;

import java.io.IOException;

public class LoginModel {
    private UserManager userManager;

    public LoginModel() throws IOException {
        userManager = new UserManager();
    }

    /**
     * Attempts to authenticate a user with BCrypt password verification
     * @param username Username to check
     * @param password Plain text password to validate
     * @return true if authentication succeeds, false otherwise
     */
    public boolean authenticate(String username, String password) {
        Users user = userManager.getUserByUsername(username);

        // Check if the user exists and if the password matches
        if (user != null && BCryptUtil.checkPassword(password, user.getPassword())) {
            // Set the user in the session when authentication is successful
            UserSession.getInstance().setUser(user);
            return true;
        }
        return false;
    }

    /**
     * Gets the currently logged-in user from UserSession
     * @return Current user or null if no user is logged in
     */
    public Users getCurrentUser() {
        return UserSession.getInstance().getUser();
    }

    /**
     * Logs out the current user by clearing the UserSession
     */
    public void logout() {
        UserSession.getInstance().clearSession();
    }
}