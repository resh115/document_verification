package com.documentverification.model;

public class Workflow {

    private long id;
    private long organizationId;
    private long documentTypeId;
    private String name;
    private boolean active;

    public Workflow() {
    }

    public Workflow(long id, long organizationId, long documentTypeId,
                    String name, boolean active) {
        this.id = id;
        this.organizationId = organizationId;
        this.documentTypeId = documentTypeId;
        this.name = name;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
