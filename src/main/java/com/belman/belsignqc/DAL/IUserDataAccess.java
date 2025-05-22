package com.belman.belsignqc.DAL;

import com.belman.belsignqc.BE.Users;

import java.util.List;

public interface IUserDataAccess {

    List<Users> getAllUsers() throws Exception;

    Users createUser(Users newUser) throws Exception;

    void deleteUser(Users user) throws Exception;

    void updateUser(Users user) throws Exception;

    Users getUsername(String username);
}
