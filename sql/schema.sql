CREATE DATABASE IF NOT EXISTS faceattend;
USE faceattend;

CREATE TABLE IF NOT EXISTS users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    role         ENUM('ADMIN', 'TEACHER', 'INSPECTOR') NOT NULL DEFAULT 'TEACHER',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    student_id      INT AUTO_INCREMENT PRIMARY KEY,
    roll_number     VARCHAR(30)  NOT NULL UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    class_section   VARCHAR(20)  NOT NULL,
    enrolled_on     DATE         NOT NULL,
    last_reenrolled DATE,
    photo_path      VARCHAR(255),
    active          BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS attendance_records (
    record_id        INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    marked_by         INT,
    attendance_date   DATE NOT NULL,
    time_marked       DATETIME NOT NULL,
    status            ENUM('PRESENT', 'ABSENT', 'MANUAL_OVERRIDE') NOT NULL DEFAULT 'PRESENT',
    liveness_passed   BOOLEAN DEFAULT TRUE,
    match_confidence  DECIMAL(5,2),
    synced            BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_attendance_marked_by FOREIGN KEY (marked_by) REFERENCES users(user_id),
    CONSTRAINT uq_student_date UNIQUE (student_id, attendance_date)
);


INSERT IGNORE INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'Administrator', 'ADMIN'),
('teacher', 'teacher123', 'Class Teacher', 'TEACHER'),
('inspector', 'inspector123', 'Inspection Officer', 'INSPECTOR');