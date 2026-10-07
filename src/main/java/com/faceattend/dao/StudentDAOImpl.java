package com.faceattend.dao;

import com.faceattend.db.DBConnection;
import com.faceattend.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public Student create(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_number, full_name, class_section, enrolled_on, active) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getFullName());
            ps.setString(3, student.getClassSection());
            ps.setDate(4, Date.valueOf(student.getEnrolledOn()));
            ps.setBoolean(5, student.isActive());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setStudentId(keys.getInt(1));
                }
            }
        }
        return student;
    }

    @Override
    public Student findById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public Student findByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY full_name";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        }
        return students;
    }

    @Override
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, class_section = ?, active = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getFullName());
            ps.setString(2, student.getClassSection());
            ps.setBoolean(3, student.isActive());
            ps.setInt(4, student.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setFullName(rs.getString("full_name"));
        s.setClassSection(rs.getString("class_section"));
        Date enrolled = rs.getDate("enrolled_on");
        if (enrolled != null) s.setEnrolledOn(enrolled.toLocalDate());
        s.setActive(rs.getBoolean("active"));
        return s;
    }
}
