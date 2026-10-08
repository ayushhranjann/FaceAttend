package com.faceattend.dao;

import com.faceattend.model.AttendanceRecord;
import com.faceattend.model.AttendanceStatus;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface AttendanceDAO {

    AttendanceRecord create(AttendanceRecord record) throws SQLException;

    boolean existsForStudentOnDate(int studentId, LocalDate date) throws SQLException;

    Map<String, Integer> markBatch(List<AttendanceRecord> records) throws SQLException;

    List<AttendanceRecord> findUnsynced() throws SQLException;

    boolean markSynced(int recordId) throws SQLException;

    List<AttendanceRecord> findByDateAndStatus(LocalDate date, AttendanceStatus status) throws SQLException;

    boolean updateStatus(int recordId, AttendanceStatus status) throws SQLException;
    List<AttendanceRecord> findByDate(LocalDate date) throws SQLException;
}
