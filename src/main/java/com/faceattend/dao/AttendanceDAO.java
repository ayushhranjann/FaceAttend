package com.faceattend.dao;

import com.faceattend.model.AttendanceRecord;

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
}
