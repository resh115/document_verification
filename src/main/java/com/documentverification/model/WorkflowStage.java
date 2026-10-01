package com.documentverification.model;

public class WorkflowStage {

  private long id, workflowId, userId;
  private int stageOrder;
  private String stageName, userName, userEmail;

  public long getId() {
    return id;
  }

  public void setId(long v) {
    id = v;
  }

  public long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(long v) {
    workflowId = v;
  }

  public int getStageOrder() {
    return stageOrder;
  }

  public void setStageOrder(int v) {
    stageOrder = v;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long v) {
    userId = v;
  }

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String v) {
    stageName = v;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String v) {
    userName = v;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String v) {
    userEmail = v;
  }
}
