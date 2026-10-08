package com.faceattend.dao;

import com.faceattend.db.DBConnection;
import com.faceattend.model.AttendanceRecord;
import com.faceattend.model.AttendanceStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceDAOImpl implements AttendanceDAO {

    @Override
    public AttendanceRecord create(AttendanceRecord record) throws SQLException {
        String sql = "INSERT INTO attendance_records "
                + "(student_id, marked_by, attendance_date, time_marked, status, liveness_passed, match_confidence, synced) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRecord(ps, record);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) record.setRecordId(keys.getInt(1));
            }
        }
        return record;
    }

    @Override
    public boolean existsForStudentOnDate(int studentId, java.time.LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM attendance_records WHERE student_id = ? AND attendance_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Map<String, Integer> markBatch(List<AttendanceRecord> records) throws SQLException {
        Map<String, Integer> tally = new HashMap<>();
        tally.put("PRESENT", 0);
        tally.put("ABSENT", 0);

        String sql = "INSERT INTO attendance_records "
                + "(student_id, marked_by, attendance_date, time_marked, status, liveness_passed, match_confidence, synced) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (AttendanceRecord record : records) {
                    bindRecord(ps, record);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();

            for (AttendanceRecord record : records) {
                tally.merge(record.getStatus().name(), 1, Integer::sum);
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return tally;
    }

    @Override
    public List<AttendanceRecord> findUnsynced() throws SQLException {
        String sql = "SELECT * FROM attendance_records WHERE synced = FALSE ORDER BY record_id";
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public boolean markSynced(int recordId) throws SQLException {
        String sql = "UPDATE attendance_records SET synced = TRUE WHERE record_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<AttendanceRecord> findByDateAndStatus(java.time.LocalDate date, AttendanceStatus status) throws SQLException {
        String sql = "SELECT * FROM attendance_records WHERE attendance_date = ? AND status = ? ORDER BY record_id";
        List<AttendanceRecord> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    @Override
    public boolean updateStatus(int recordId, AttendanceStatus status) throws SQLException {
        String sql = "UPDATE attendance_records SET status = ?, synced = FALSE WHERE record_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, recordId);
            return ps.executeUpdate() > 0;
        }
    }



    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord record = new AttendanceRecord();
        record.setRecordId(rs.getInt("record_id"));
        record.setStudentId(rs.getInt("student_id"));
        int markedBy = rs.getInt("marked_by");
        if (!rs.wasNull()) record.setMarkedBy(markedBy);
        record.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());
        record.setTimeMarked(rs.getTimestamp("time_marked").toLocalDateTime());
        record.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
        record.setLivenessPassed(rs.getBoolean("liveness_passed"));
        record.setMatchConfidence(rs.getDouble("match_confidence"));
        record.setSynced(rs.getBoolean("synced"));
        return record;
    }

    private void bindRecord(PreparedStatement ps, AttendanceRecord record) throws SQLException {
        ps.setInt(1, record.getStudentId());
        if (record.getMarkedBy() != null) ps.setInt(2, record.getMarkedBy());
        else ps.setNull(2, Types.INTEGER);
        ps.setDate(3, Date.valueOf(record.getAttendanceDate()));
        ps.setTimestamp(4, Timestamp.valueOf(record.getTimeMarked()));
        ps.setString(5, record.getStatus().name());
        ps.setBoolean(6, record.isLivenessPassed());
        ps.setDouble(7, record.getMatchConfidence());
        ps.setBoolean(8, record.isSynced());
    }
}