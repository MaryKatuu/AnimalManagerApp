package com.example.animalmanagerapp.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Salted SHA-256 hashing for passwords, security answers, and recovery
 * codes. Nothing sensitive is ever stored or compared in plain text.
 */
public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;
    private static final String RECOVERY_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0, I/1

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    public static String hash(String text, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt.getBytes());
            byte[] hashed = digest.digest(text.getBytes());
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public static boolean verify(String input, String salt, String storedHash) {
        if (input == null || salt == null || storedHash == null) return false;
        return hash(input, salt).equals(storedHash);
    }

    /** Generates an easy-to-write-down recovery code, e.g. "K7XP-4RTQ". */
    public static String generateRecoveryCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append('-');
            sb.append(RECOVERY_CODE_CHARS.charAt(random.nextInt(RECOVERY_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /** Strips spaces/dashes and uppercases, so "k7xp4rtq" matches "K7XP-4RTQ". */
    public static String normalizeCode(String code) {
        if (code == null) return "";
        return code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}