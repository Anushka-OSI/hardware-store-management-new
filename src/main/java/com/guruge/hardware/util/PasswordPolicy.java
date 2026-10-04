package com.guruge.hardware.util;

import com.guruge.hardware.exception.BusinessException;

/**
 * Central password strength policy for staff accounts.
 * Rule: minimum 8 characters with at least one uppercase letter,
 * one lowercase letter, one digit and one special character.
 * The password must not contain the username.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        validate(null, rawPassword);
    }

    public static void validate(String username, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new BusinessException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!rawPassword.chars().anyMatch(Character::isUpperCase)) {
            throw new BusinessException("Password must contain at least one uppercase letter (A-Z)");
        }
        if (!rawPassword.chars().anyMatch(Character::isLowerCase)) {
            throw new BusinessException("Password must contain at least one lowercase letter (a-z)");
        }
        if (!rawPassword.chars().anyMatch(Character::isDigit)) {
            throw new BusinessException("Password must contain at least one digit (0-9)");
        }
        if (rawPassword.chars().noneMatch(c -> !Character.isLetterOrDigit(c))) {
            throw new BusinessException("Password must contain at least one special character (e.g. @ # $ % & *)");
        }
        if (username != null && !username.isBlank()) {
            String userPart = username.contains("@") ? username.substring(0, username.indexOf('@')) : username;
            if (!userPart.isBlank() && rawPassword.toLowerCase().contains(userPart.toLowerCase())) {
                throw new BusinessException("Password must not contain your username");
            }
        }
    }

    /** Human-readable summary of the rule, for UI hints and error toasts. */
    public static String describe() {
        return "Min " + MIN_LENGTH + " chars with uppercase, lowercase, digit and special character";
    }
}
