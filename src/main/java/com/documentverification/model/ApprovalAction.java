package com.documentverification.model;

import java.sql.Timestamp;

public class ApprovalAction {

  private long id;
  private long documentId;
  private long versionId;
  private long approverId;
  private int stageOrder;

  private String approverName;
  private String approverRole;
  private String action;
  private String comment;
  private Timestamp actedAt;

  public ApprovalAction() {}

  public ApprovalAction(
    long id,
    long documentId,
    long versionId,
    long approverId,
    int stageOrder,
    String approverName,
    String action,
    String comment,
    Timestamp actedAt
  ) {
    this.id = id;
    this.documentId = documentId;
    this.versionId = versionId;
    this.approverId = approverId;
    this.stageOrder = stageOrder;
    this.approverName = approverName;
    this.action = action;
    this.comment = comment;
    this.actedAt = actedAt;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getDocumentId() {
    return documentId;
  }

  public void setDocumentId(long documentId) {
    this.documentId = documentId;
  }

  public long getVersionId() {
    return versionId;
  }

  public void setVersionId(long versionId) {
    this.versionId = versionId;
  }

  public long getApproverId() {
    return approverId;
  }

  public void setApproverId(long approverId) {
    this.approverId = approverId;
  }

  public int getStageOrder() {
    return stageOrder;
  }

  public void setStageOrder(int stageOrder) {
    this.stageOrder = stageOrder;
  }

  public String getApproverName() {
    return approverName;
  }

  public void setApproverName(String approverName) {
    this.approverName = approverName;
  }

  public String getApproverRole() {
    return approverRole;
  }

  public void setApproverRole(String approverRole) {
    this.approverRole = approverRole;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Timestamp getActedAt() {
    return actedAt;
  }

  public void setActedAt(Timestamp actedAt) {
    this.actedAt = actedAt;
  }
}
