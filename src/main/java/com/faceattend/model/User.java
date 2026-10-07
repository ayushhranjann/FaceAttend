package com.faceattend.model;

public class User extends Person {

    private int userId;
    private String username;
    private String password;
    private Role role;

    public User() {
        super(null);
    }

    public User(String username, String password, String fullName, Role role) {
        super(fullName);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(int userId, String username, String password, String fullName, Role role) {
        super(fullName);
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String describe() {
        return fullName + " (" + role + ")";
    }
}
