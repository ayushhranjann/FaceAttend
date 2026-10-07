package com.faceattend.model;

import java.time.LocalDate;

public class Student extends Person {

    private int studentId;
    private String rollNumber;
    private String classSection;
    private LocalDate enrolledOn;
    private LocalDate lastReenrolled;
    private String photoPath;
    private boolean active;

    public Student() {
        super(null);
    }

    public Student(String rollNumber, String fullName, String classSection, LocalDate enrolledOn) {
        super(fullName);
        this.rollNumber = rollNumber;
        this.classSection = classSection;
        this.enrolledOn = enrolledOn;
        this.active = true;
    }

    public Student(int studentId, String rollNumber, String fullName, String classSection,
                    LocalDate enrolledOn, LocalDate lastReenrolled, String photoPath, boolean active) {
        super(fullName);
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.classSection = classSection;
        this.enrolledOn = enrolledOn;
        this.lastReenrolled = lastReenrolled;
        this.photoPath = photoPath;
        this.active = active;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getClassSection() { return classSection; }
    public void setClassSection(String classSection) { this.classSection = classSection; }
    public LocalDate getEnrolledOn() { return enrolledOn; }
    public void setEnrolledOn(LocalDate enrolledOn) { this.enrolledOn = enrolledOn; }
    public LocalDate getLastReenrolled() { return lastReenrolled; }
    public void setLastReenrolled(LocalDate lastReenrolled) { this.lastReenrolled = lastReenrolled; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String describe() {
        return rollNumber + " - " + fullName + " (" + classSection + ")";
    }

    @Override
    public String toString() {
        return describe();
    }
}
