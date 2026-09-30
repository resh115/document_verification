package com.documentverification.model;

public class Role {
    private long id;
    private Long organizationId;
    private String name;
    private String description;

    public Role() {}

    public Role(long id, Long organizationId, String name, String description) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
