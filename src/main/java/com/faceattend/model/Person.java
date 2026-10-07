package com.faceattend.model;

public abstract class Person {

    protected String fullName;

    protected Person(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public abstract String describe();
}
