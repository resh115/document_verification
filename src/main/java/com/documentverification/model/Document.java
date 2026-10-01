package com.documentverification.model;

import java.sql.Timestamp;

public class Document {

  private long id;
  private long organizationId;
  private long submitterId;
  private long documentTypeId;
  private long workflowId;
  private long currentStageId;

  private String title;
  private String description;
  private String status;
  private int currentStageOrder;
  private int currentVersion;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public Document() {}

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

  public long getSubmitterId() {
    return submitterId;
  }

  public void setSubmitterId(long submitterId) {
    this.submitterId = submitterId;
  }

  public long getDocumentTypeId() {
    return documentTypeId;
  }

  public void setDocumentTypeId(long documentTypeId) {
    this.documentTypeId = documentTypeId;
  }

  public long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(long workflowId) {
    this.workflowId = workflowId;
  }

  public long getCurrentStageId() {
    return currentStageId;
  }

  public void setCurrentStageId(long currentStageId) {
    this.currentStageId = currentStageId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getCurrentStageOrder() {
    return currentStageOrder;
  }

  public void setCurrentStageOrder(int currentStageOrder) {
    this.currentStageOrder = currentStageOrder;
  }

  public int getCurrentVersion() {
    return currentVersion;
  }

  public void setCurrentVersion(int currentVersion) {
    this.currentVersion = currentVersion;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
}
