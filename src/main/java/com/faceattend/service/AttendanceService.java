package com.faceattend.service;

import com.faceattend.dao.AttendanceDAO;
import com.faceattend.model.AttendanceRecord;
import com.faceattend.model.AttendanceStatus;
import com.faceattend.model.Student;
import com.faceattend.util.DuplicateAttendanceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AttendanceService {

    public static final double CONFIDENCE_THRESHOLD = 85.0;
    private static final double DETECTION_CHANCE = 0.90;
    private static final double HIGH_CONFIDENCE_CHANCE = 0.80;

    private final AttendanceDAO attendanceDAO;
    private final Random random = new Random();

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public AttendanceRecord markAttendance(int studentId, AttendanceStatus status,
                                            boolean livenessPassed, double matchConfidence,
                                            Integer markedBy) throws DuplicateAttendanceException, SQLException {
        LocalDate today = LocalDate.now();
        if (attendanceDAO.existsForStudentOnDate(studentId, today)) {
            throw new DuplicateAttendanceException(
                    "Attendance for student #" + studentId + " has already been recorded today.");
        }
        AttendanceRecord record = new AttendanceRecord(studentId, today, status, livenessPassed, matchConfidence);
        record.setMarkedBy(markedBy);
        return attendanceDAO.create(record);
    }

    public Map<String, Integer> markSessionManual(List<Student> roster, Set<Integer> presentStudentIds,
                                                   Integer markedBy) throws SQLException {
        LocalDate today = LocalDate.now();
        List<AttendanceRecord> batch = new ArrayList<>();

        for (Student s : roster) {
            if (attendanceDAO.existsForStudentOnDate(s.getStudentId(), today)) continue;

            AttendanceStatus status = presentStudentIds.contains(s.getStudentId())
                    ? AttendanceStatus.PRESENT
                    : AttendanceStatus.ABSENT;
            AttendanceRecord record = new AttendanceRecord(s.getStudentId(), today, status, false, 0.0);
            record.setMarkedBy(markedBy);
            batch.add(record);
        }

        return attendanceDAO.markBatch(batch);
    }

    public Map<String, Integer> scanSession(List<Student> roster, Integer markedBy) throws SQLException {
        LocalDate today = LocalDate.now();
        List<AttendanceRecord> batch = new ArrayList<>();
        int notDetected = 0;

        for (Student s : roster) {
            if (attendanceDAO.existsForStudentOnDate(s.getStudentId(), today)) continue;

            if (random.nextDouble() < DETECTION_CHANCE) {
                double confidence = simulateConfidence();
                AttendanceStatus status = (confidence >= CONFIDENCE_THRESHOLD)
                        ? AttendanceStatus.PRESENT
                        : AttendanceStatus.MANUAL_OVERRIDE;
                AttendanceRecord record = new AttendanceRecord(s.getStudentId(), today, status, true, confidence);
                record.setMarkedBy(markedBy);
                batch.add(record);
            } else {
                notDetected++;
            }
        }

        Map<String, Integer> tally = attendanceDAO.markBatch(batch);
        tally.put("NOT_DETECTED", notDetected);
        return tally;
    }

    private double simulateConfidence() {
        double value;
        if (random.nextDouble() < HIGH_CONFIDENCE_CHANCE) {
            value = CONFIDENCE_THRESHOLD + random.nextDouble() * (100.0 - CONFIDENCE_THRESHOLD);
        } else {
            value = 70.0 + random.nextDouble() * (CONFIDENCE_THRESHOLD - 70.0);
        }
        return Math.round(value * 100.0) / 100.0;
    }
}