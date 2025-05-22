package com.belman.belsignqc.GUI.Model;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.BLL.UserManager;

import java.io.IOException;
import java.util.List;

public class UserModel {
    private UserManager userManager;

    public UserModel() throws IOException {
        userManager = new UserManager();
    }

    /**
     * Gets all users from the system
     * @return List of all users
     * @throws Exception if retrieval fails
     */
    public List<Users> getAllUsers() throws Exception {
        return userManager.getAllUsers();
    }

    /**
     * Creates a new user
     * @param username Username for new user
     * @param password Password for new user
     * @param isAdmin Admin status
     * @param isQA QA status
     * @param isOperator Operator status
     * @param profilePicture Profile picture as byte array for BLOB
     * @return Created user with generated ID
     * @throws Exception if creation fails
     */
    public Users createUser(String username, String password, boolean isAdmin,
                            boolean isQA, boolean isOperator, byte[] profilePicture) throws Exception {
        Users newUser = new Users(0, username, password, isAdmin, isQA, isOperator, profilePicture);
        return userManager.createUser(newUser);
    }

    /**
     * Deletes a user from the system
     * @param user User to delete
     * @throws Exception if deletion fails
     */
    public void deleteUser(Users user) throws Exception {
        userManager.deleteUser(user);
    }

    /**
     * Updates a user's information
     * @param user User with updated information
     * @throws Exception if update fails
     */
    public void updateUser(Users user) throws Exception {
        userManager.updateUser(user);
    }

    /**
     * Gets a user by username
     * @param username Username to search for
     * @return User with the specified username or null if not found
     */
    public Users getUserByUsername(String username) {
        return userManager.getUserByUsername(username);
    }

    //Mock data versions for initial testing
    private String username;

    public UserModel(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
