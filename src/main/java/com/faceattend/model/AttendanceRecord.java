package com.faceattend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRecord {

    private int recordId;
    private int studentId;
    private Integer markedBy;
    private LocalDate attendanceDate;
    private LocalDateTime timeMarked;
    private AttendanceStatus status;
    private boolean livenessPassed;
    private double matchConfidence;
    private boolean synced;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int studentId, LocalDate attendanceDate, AttendanceStatus status,
                             boolean livenessPassed, double matchConfidence) {
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.timeMarked = LocalDateTime.now();
        this.status = status;
        this.livenessPassed = livenessPassed;
        this.matchConfidence = matchConfidence;
        this.synced = false;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public Integer getMarkedBy() { return markedBy; }
    public void setMarkedBy(Integer markedBy) { this.markedBy = markedBy; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public LocalDateTime getTimeMarked() { return timeMarked; }
    public void setTimeMarked(LocalDateTime timeMarked) { this.timeMarked = timeMarked; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public boolean isLivenessPassed() { return livenessPassed; }
    public void setLivenessPassed(boolean livenessPassed) { this.livenessPassed = livenessPassed; }
    public double getMatchConfidence() { return matchConfidence; }
    public void setMatchConfidence(double matchConfidence) { this.matchConfidence = matchConfidence; }
    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}
