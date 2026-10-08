package com.faceattend.ui;

import com.faceattend.dao.AttendanceDAO;
import com.faceattend.dao.AttendanceDAOImpl;
import com.faceattend.dao.StudentDAO;
import com.faceattend.dao.StudentDAOImpl;
import com.faceattend.model.AttendanceRecord;
import com.faceattend.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodayRecordsPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Roll No.", "Name", "Status", "Confidence", "Time", "Synced"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel(" ");

    public TodayRecordsPanel() {
        buildUI();
        reload();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Today's Attendance Records");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> reload());

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(refreshButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    public void reload() {
        try {
            tableModel.setRowCount(0);

            Map<Integer, Student> studentsById = new HashMap<>();
            for (Student student : studentDAO.findAll()) {
                studentsById.put(student.getStudentId(), student);
            }

            List<AttendanceRecord> records = attendanceDAO.findByDate(LocalDate.now());
            Map<String, Integer> counts = new HashMap<>();

            for (AttendanceRecord record : records) {
                Student student = studentsById.get(record.getStudentId());
                String status = record.getStatus().name();
                counts.merge(status, 1, Integer::sum);

                tableModel.addRow(new Object[]{
                        student != null ? student.getRollNumber() : "?",
                        student != null ? student.getFullName() : "Unknown",
                        status,
                        record.getMatchConfidence() > 0 ? record.getMatchConfidence() : "-",
                        record.getTimeMarked().format(TIME_FORMAT),
                        record.isSynced() ? "Yes" : "No"
                });
            }

            summaryLabel.setForeground(Color.DARK_GRAY);
            if (records.isEmpty()) {
                summaryLabel.setText("No attendance recorded yet today.");
            } else {
                summaryLabel.setText("Total: " + records.size()
                        + " | Present: " + counts.getOrDefault("PRESENT", 0)
                        + " | Flagged: " + counts.getOrDefault("MANUAL_OVERRIDE", 0)
                        + " | Absent: " + counts.getOrDefault("ABSENT", 0));
            }
        } catch (SQLException e) {
            summaryLabel.setForeground(Color.RED);
            summaryLabel.setText("Could not load records: " + e.getMessage());
        }
    }
}