package com.belman.belsignqc.BLL;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DAO.UserDAO;
import com.belman.belsignqc.DAL.IUserDataAccess;

import java.io.IOException;
import java.util.List;

public class UserManager {

    private IUserDataAccess userDAO;

    public UserManager() throws IOException {
        userDAO = new UserDAO();
    }

    /**
     * Gets all users from the database
     * @return List of all users
     * @throws Exception if retrieval fails
     */
    public List<Users> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }

    /**
     * Creates a new user in the database
     * @param newUser User to create
     * @return Created user with generated ID
     * @throws Exception if creation fails
     */
    public Users createUser(Users newUser) throws Exception {
        return userDAO.createUser(newUser);
    }

    /**
     * Deletes a user from the database
     * @param user User to delete
     * @throws Exception if deletion fails
     */
    public void deleteUser(Users user) throws Exception {
        userDAO.deleteUser(user);
    }

    /**
     * Updates a user's information in the database
     * @param user User with updated information
     * @throws Exception if update fails
     */
    public void updateUser(Users user) throws Exception {
        userDAO.updateUser(user);
    }
//
    /**
     * Gets a user by username
     * @param username Username to search for
     * @return User with the specified username or null if not found
     */
    public Users getUserByUsername(String username) {
        return userDAO.getUsername(username);
    }
}