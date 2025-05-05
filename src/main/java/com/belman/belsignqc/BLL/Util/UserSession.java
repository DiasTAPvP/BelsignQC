package com.belman.belsignqc.BLL.Util;

import com.belman.belsignqc.BE.Users;

public class UserSession {

    private static Users loggedInUser;

    public static void setLoggedInUser(Users user) {
        loggedInUser = user;
    }

    public static Users getLoggedInUser() {
        return loggedInUser;
    }




}
