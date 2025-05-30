package com.belman.belsignqc.DAL.DAO;

import com.belman.belsignqc.BE.Users;
import com.belman.belsignqc.DAL.DBConnector;
import com.belman.belsignqc.DAL.IUserDataAccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDataAccess {

    private DBConnector dbConnector;

    public UserDAO() throws IOException {
        dbConnector = new DBConnector();
    }

    /**
     * Gets all users from the database
     * @return
     * @throws Exception
     */
    @Override
    public List<Users> getAllUsers() throws Exception {
        ArrayList<Users> allUsers = new ArrayList<>();

        try (Connection connection = dbConnector.getConnection()) {
            String sql = "SELECT * FROM Users";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                int userid = rs.getInt("UserID");
                String username = rs.getString("Username");
                String password = rs.getString("PasswordHash");
                boolean isadmin = rs.getBoolean("IsAdmin");
                boolean isqa = rs.getBoolean("IsQAEmployee");
                boolean isoperator = rs.getBoolean("IsOperator");
                byte[] profilepicture = null;
                Blob blob = rs.getBlob("ProfilePicture");
                if (blob != null) {
                    profilepicture = blob.getBytes(1, (int) blob.length());
                } else {
                    System.out.println("No profile picture found for user: " + username);
                }

                Users user = new Users(userid, username, password, isadmin, isqa, isoperator, profilepicture);
                allUsers.add(user);
            }
            return allUsers;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get users from database.", ex);
        }
    }

    /**
     * Creates a new user in the database
     * @param newUser
     * @return
     * @throws Exception
     */
    @Override
    public Users createUser(Users newUser) throws Exception {
        try (Connection connection = dbConnector.getConnection()) {
            String sql = "INSERT INTO Users (username, passwordhash, isadmin, isqaemployee, isoperator, profilepicture) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, newUser.getUsername());
            statement.setString(2, newUser.getPassword());
            statement.setBoolean(3, newUser.isAdmin());
            statement.setBoolean(4, newUser.isQA());
            statement.setBoolean(5, newUser.isOperator());
            if (newUser.getProfilePicture() != null && newUser.getProfilePicture().length > 0) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(newUser.getProfilePicture());
                statement.setBinaryStream(6, inputStream, newUser.getProfilePicture().length);
            } else {
                statement.setNull(6, Types.BLOB);
            }
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                newUser.setUserID(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Could not create user in database.", e);
        }
        return newUser;
    }

    /**
     * Deletes a user from the database
     * @param users
     * @throws Exception
     */
    @Override
    public void deleteUser(Users users) throws Exception {
        String sql = "DELETE FROM Users WHERE userid = ?";

        try (Connection connection = dbConnector.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setInt(1, users.getUserID());

            //Run the SQL statement
            stmt.executeUpdate();

        } catch (SQLException ex) {
            throw new Exception("Could not get user from database.", ex);
        }
    }

    /**
     * Updates a user in the database
     * @param user
     * @throws Exception
     */
    @Override
    public void updateUser(Users user) throws Exception {

    }

    /**
     * Gets a user by username from the database
     * @param username
     * @return
     */
    @Override
    public Users getUsername(String username) {
        try (Connection connection = dbConnector.getConnection()) {
            String sql = "SELECT * FROM Users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Users(
                        resultSet.getInt("UserID"),
                        resultSet.getString("Username"),
                        resultSet.getString("PasswordHash"),
                        resultSet.getBoolean("IsAdmin"),
                        resultSet.getBoolean("IsQAEmployee"),
                        resultSet.getBoolean("IsOperator"),
                        resultSet.getBytes("ProfilePicture")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Gets a username by user ID
     *
     * @param userID The ID of the user
     * @return The username associated with the ID
     * @throws SQLException If a database error occurs
     */
    public String getUsernameById(int userID) throws SQLException {
        String sql = "SELECT username FROM Users WHERE userID = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
            return "Unknown User";
        }
    }
}
