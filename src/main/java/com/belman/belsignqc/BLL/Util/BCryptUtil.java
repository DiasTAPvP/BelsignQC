package com.belman.belsignqc.BLL.Util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptUtil {

    // Method to hash a password
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.withDefaults().hashToString(12, plainTextPassword.toCharArray());
    }

    // Method to check a plain password against a hashed password
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainTextPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
