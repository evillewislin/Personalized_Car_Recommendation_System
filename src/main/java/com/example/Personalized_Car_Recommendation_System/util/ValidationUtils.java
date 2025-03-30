package com.example.Personalized_Car_Recommendation_System.util;

public class ValidationUtils {

    public static void validateRegistration(String username, String password, String confirmPassword, String ageStr) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("密码和确认密码不匹配");
        }
        if (ageStr != null && !ageStr.isEmpty()) {
            try {
                Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("年龄必须是有效的整数");
            }
        }
    }

    public static void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
    }

    public static Integer parseAge(String ageStr) {
        if (ageStr == null || ageStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("年龄必须是有效的整数");
        }
    }
}