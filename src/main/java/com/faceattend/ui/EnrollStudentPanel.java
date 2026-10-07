package com.faceattend.ui;

import com.faceattend.dao.StudentDAO;
import com.faceattend.dao.StudentDAOImpl;
import com.faceattend.model.Student;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class EnrollStudentPanel extends JPanel {

    private final JTextField rollNumberField = new JTextField(15);
    private final JTextField fullNameField = new JTextField(15);
    private final JTextField classSectionField = new JTextField(15);
    private final JLabel statusLabel = new JLabel(" ");

    private final StudentDAO studentDAO = new StudentDAOImpl();

    public EnrollStudentPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Enroll New Student");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        add(new JLabel("Roll Number:"), gbc);
        gbc.gridx = 1;
        add(rollNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Class / Section:"), gbc);
        gbc.gridx = 1;
        add(classSectionField, gbc);

        JButton enrollButton = new JButton("Enroll");
        enrollButton.addActionListener(e -> handleEnroll());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(enrollButton, gbc);

        gbc.gridy++;
        add(statusLabel, gbc);
    }

    private void handleEnroll() {
        String roll = rollNumberField.getText().trim();
        String name = fullNameField.getText().trim();
        String section = classSectionField.getText().trim();

        if (roll.isEmpty() || name.isEmpty() || section.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("All fields are required.");
            return;
        }

        try {
            Student student = new Student(roll, name, section, LocalDate.now());
            studentDAO.create(student);
            statusLabel.setForeground(new Color(0, 130, 0));
            statusLabel.setText("Enrolled: " + name + " (ID #" + student.getStudentId() + ")");
            rollNumberField.setText("");
            fullNameField.setText("");
            classSectionField.setText("");
        } catch (Exception ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }
}
