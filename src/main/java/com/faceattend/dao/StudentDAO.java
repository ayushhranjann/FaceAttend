*package com.faceattend.dao;

import com.faceattend.model.Student;

import java.sql.SQLException;
import java.util.List;

public interface StudentDAO {

    Student create(Student student) throws SQLException;

    Student findById(int studentId) throws SQLException;

    Student findByRollNumber(String rollNumber) throws SQLException;

    List<Student> findAll() throws SQLException;

    boolean update(Student student) throws SQLException;

    boolean delete(int studentId) throws SQLException;
}
