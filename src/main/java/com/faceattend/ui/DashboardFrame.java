package com.faceattend.ui;

import com.faceattend.model.User;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame(User user) {
        super("FaceAttend - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcome = new JLabel("Logged in as: " + user.describe());
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        add(welcome, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Enroll Student", new EnrollStudentPanel());
        tabs.addTab("Student Directory", new StudentListPanel());
        add(tabs, BorderLayout.CENTER);
    }
}
