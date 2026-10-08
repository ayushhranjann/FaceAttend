package com.faceattend.ui;

import com.faceattend.dao.AttendanceDAO;
import com.faceattend.dao.AttendanceDAOImpl;
import com.faceattend.dao.StudentDAO;
import com.faceattend.dao.StudentDAOImpl;
import com.faceattend.model.AttendanceRecord;
import com.faceattend.model.AttendanceStatus;
import com.faceattend.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewPanel extends JPanel {

    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final List<AttendanceRecord> pending = new ArrayList<>();
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Roll No.", "Name", "Confidence"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel(" ");

    public ReviewPanel() {
        buildUI();
        reload();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Flagged for Teacher Review (today)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton confirmButton = new JButton("Confirm Present");
        confirmButton.addActionListener(e -> resolveSelected(AttendanceStatus.PRESENT));
        JButton rejectButton = new JButton("Mark Absent");
        rejectButton.addActionListener(e -> resolveSelected(AttendanceStatus.ABSENT));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(confirmButton);
        buttons.add(rejectButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(statusLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void reload() {
        try {
            pending.clear();
            tableModel.setRowCount(0);
            List<AttendanceRecord> flagged =
                    attendanceDAO.findByDateAndStatus(LocalDate.now(), AttendanceStatus.MANUAL_OVERRIDE);
            for (AttendanceRecord record : flagged) {
                Student student = studentDAO.findById(record.getStudentId());
                pending.add(record);
                tableModel.addRow(new Object[]{
                        student != null ? student.getRollNumber() : "?",
                        student != null ? student.getFullName() : "Unknown",
                        record.getMatchConfidence()
                });
            }
            statusLabel.setForeground(Color.DARK_GRAY);
            statusLabel.setText(pending.isEmpty() ? "Nothing waiting for review." : pending.size() + " waiting for review.");
        } catch (SQLException e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Could not load entries: " + e.getMessage());
        }
    }

    private void resolveSelected(AttendanceStatus status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Select an entry first.");
            return;
        }
        try {
            attendanceDAO.updateStatus(pending.get(row).getRecordId(), status);
            reload();
            statusLabel.setForeground(new Color(0, 130, 0));
            statusLabel.setText("Entry marked " + status + ".");
        } catch (SQLException e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Update failed: " + e.getMessage());
        }
    }
}