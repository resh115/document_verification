package com.documentverification.model;

public class Organization {

  private long id;
  private String name;
  private String code;
  private String type;
  private boolean active;

  public Organization() {}

  public Organization(
    long id,
    String name,
    String code,
    String type,
    boolean active
  ) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.type = type;
    this.active = active;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
