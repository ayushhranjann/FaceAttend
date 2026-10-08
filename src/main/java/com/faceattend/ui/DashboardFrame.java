package com.faceattend.ui;

import com.faceattend.model.User;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame(User user) {
        super("FaceAttend - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcome = new JLabel("Logged in as: " + user.describe());
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        add(welcome, BorderLayout.NORTH);

        TakeAttendancePanel attendancePanel = new TakeAttendancePanel(user);
        ReviewPanel reviewPanel = new ReviewPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Enroll Student", new EnrollStudentPanel());
        tabs.addTab("Student Directory", new StudentListPanel());
        tabs.addTab("Take Attendance", attendancePanel);
        tabs.addTab("Review Flagged", reviewPanel);

        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected == attendancePanel) {
                attendancePanel.reloadRoster();
            } else if (selected == reviewPanel) {
                reviewPanel.reload();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }
}