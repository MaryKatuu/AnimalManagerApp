package com.example.animalmanagerapp.model;

/**
 * A farmer's account. Passwords and the security answer/recovery code are
 * never stored in plain text — only salted hashes.
 */
public class User {

    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private String securityQuestion;
    private String securityAnswerHash;
    private String securityAnswerSalt;
    private String recoveryCodeHash;
    private String recoveryCodeSalt;

    public User() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswerHash() { return securityAnswerHash; }
    public void setSecurityAnswerHash(String securityAnswerHash) { this.securityAnswerHash = securityAnswerHash; }

    public String getSecurityAnswerSalt() { return securityAnswerSalt; }
    public void setSecurityAnswerSalt(String securityAnswerSalt) { this.securityAnswerSalt = securityAnswerSalt; }

    public String getRecoveryCodeHash() { return recoveryCodeHash; }
    public void setRecoveryCodeHash(String recoveryCodeHash) { this.recoveryCodeHash = recoveryCodeHash; }

    public String getRecoveryCodeSalt() { return recoveryCodeSalt; }
    public void setRecoveryCodeSalt(String recoveryCodeSalt) { this.recoveryCodeSalt = recoveryCodeSalt; }
}