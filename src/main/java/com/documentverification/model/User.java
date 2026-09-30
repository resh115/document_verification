package com.documentverification.model;

public class User {
    private long id;
    private long organizationId;
    private long roleId;
    private String name;
    private String email;
    private String passwordHash;
    private String roleName;
    private String organizationName;
    private boolean active;

    public User() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getOrganizationId() { return organizationId; }
    public void setOrganizationId(long organizationId) { this.organizationId = organizationId; }
    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
