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
        return markSession(roster, presentStudentIds, markedBy, false);
    }

    public Map<String, Integer> markSessionByFaceScan(List<Student> roster, Set<Integer> presentStudentIds,
                                                       Integer markedBy) throws SQLException {
        return markSession(roster, presentStudentIds, markedBy, true);
    }

    private Map<String, Integer> markSession(List<Student> roster, Set<Integer> presentStudentIds,
                                              Integer markedBy, boolean faceScan) throws SQLException {
        LocalDate today = LocalDate.now();

        Set<Integer> alreadyMarked = new HashSet<>();
        for (Student s : roster) {
            if (attendanceDAO.existsForStudentOnDate(s.getStudentId(), today)) {
                alreadyMarked.add(s.getStudentId());
            }
        }

        List<AttendanceRecord> batch = new ArrayList<>();
        for (Student s : roster) {
            if (alreadyMarked.contains(s.getStudentId())) continue;

            AttendanceRecord record;
            if (presentStudentIds.contains(s.getStudentId())) {
                if (faceScan) {
                    double confidence = simulateConfidence();
                    AttendanceStatus status = (confidence >= CONFIDENCE_THRESHOLD)
                            ? AttendanceStatus.PRESENT
                            : AttendanceStatus.MANUAL_OVERRIDE;
                    record = new AttendanceRecord(s.getStudentId(), today, status, true, confidence);
                } else {
                    record = new AttendanceRecord(s.getStudentId(), today, AttendanceStatus.PRESENT, false, 0.0);
                }
            } else {
                record = new AttendanceRecord(s.getStudentId(), today, AttendanceStatus.ABSENT, false, 0.0);
            }
            record.setMarkedBy(markedBy);
            batch.add(record);
        }

        return attendanceDAO.markBatch(batch);
    }

       private static final double HIGH_CONFIDENCE_CHANCE = 0.80;

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