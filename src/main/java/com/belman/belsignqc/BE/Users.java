package com.belman.belsignqc.BE;

public class Users {
//
    private int userID;
    private String username;
    private String password;
    private boolean isAdmin;
    private boolean isOperator;
    private boolean isQA;
    private byte[] profilePicture;

    //Constructor
    public Users(int userID, String username, String password, boolean isAdmin, boolean isOperator, boolean isQA,
                 byte[] profilePicture) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isOperator = isOperator;
        this.isQA = isQA;
        this.profilePicture = profilePicture;
    }

    //Getters and Setters
    public int getUserID() {
        return this.userID;
    }

    public void setUserID(int value) {
        this.userID = value;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String value) {
        this.username = value;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String value) {
        this.password = value;
    }
    public boolean isAdmin() {
        return this.isAdmin;
    }
    public void setAdmin(boolean value) {
        this.isAdmin = value;
    }
    public boolean isOperator() {
        return this.isOperator;
    }
    public void setOperator(boolean value) {
        this.isOperator = value;
    }
    public boolean isQA() {
        return this.isQA;
    }
    public void setQA(boolean value) {
        this.isQA = value;
    }
    public byte[] getProfilePicture() {
        return this.profilePicture;
    }
    public void setProfilePicture(byte[] value) {
        this.profilePicture = value;
    }

}

