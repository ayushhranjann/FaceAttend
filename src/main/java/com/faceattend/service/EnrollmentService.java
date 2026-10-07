package com.faceattend.service;

import com.faceattend.dao.StudentDAO;
import com.faceattend.model.Student;
import com.faceattend.util.EnrollmentException;

import java.sql.SQLException;
import java.time.LocalDate;

public class EnrollmentService {

    private final StudentDAO studentDAO;

    public EnrollmentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public Student enroll(String rollNumber, String fullName, String classSection) throws EnrollmentException {
        if (rollNumber == null || rollNumber.isBlank()) {
            throw new EnrollmentException("Roll number cannot be empty.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new EnrollmentException("Full name cannot be empty.");
        }

        try {
            if (studentDAO.findByRollNumber(rollNumber.trim()) != null) {
                throw new EnrollmentException("A student with roll number '" + rollNumber + "' is already enrolled.");
            }
            return studentDAO.create(new Student(rollNumber.trim(), fullName.trim(), classSection.trim(), LocalDate.now()));
        } catch (SQLException e) {
            throw new EnrollmentException("Database error: " + e.getMessage(), e);
        }
    }
}
