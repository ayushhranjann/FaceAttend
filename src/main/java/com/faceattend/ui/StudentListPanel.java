package com.faceattend.ui;

import com.faceattend.dao.StudentDAO;
import com.faceattend.dao.StudentDAOImpl;
import com.faceattend.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentListPanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Roll No.", "Name", "Class", "Enrolled On", "Active"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public StudentListPanel() {
        buildUI();
        loadStudents();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Student Directory");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudents());
        add(refreshButton, BorderLayout.SOUTH);
    }

    private void loadStudents() {
        try {
            List<Student> students = studentDAO.findAll();
            tableModel.setRowCount(0);
            for (Student s : students) {
                tableModel.addRow(new Object[]{
                        s.getRollNumber(), s.getFullName(), s.getClassSection(),
                        s.getEnrolledOn(), s.isActive() ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
