package com.faceattend.ui;

import com.faceattend.dao.AttendanceDAO;
import com.faceattend.dao.AttendanceDAOImpl;
import com.faceattend.dao.StudentDAO;
import com.faceattend.dao.StudentDAOImpl;
import com.faceattend.model.Student;
import com.faceattend.model.User;
import com.faceattend.service.AttendanceService;
import com.faceattend.service.SyncService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TakeAttendancePanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final AttendanceService attendanceService = new AttendanceService(attendanceDAO);
    private final SyncService syncService = new SyncService(attendanceDAO);
    private final User currentUser;

    private final DefaultListModel<Entry> listModel = new DefaultListModel<>();
    private final JList<Entry> studentList = new JList<>(listModel);
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel syncLabel = new JLabel(" ");
    private final JButton syncButton = new JButton("Sync Now");

    public TakeAttendancePanel(User currentUser) {
        this.currentUser = currentUser;
        buildUI();
        reloadRoster();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Take Attendance");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel info = new JLabel("<html>Tick the students who are present in the room.<br>"
                + "<b>Mark Manually</b> records them directly as PRESENT, with no score.<br>"
                + "<b>Scan Faces (Simulated)</b> runs a mock face match on them: a confidence of "
                + AttendanceService.CONFIDENCE_THRESHOLD + " or above is auto-marked PRESENT, "
                + "below that is flagged for teacher review. (No real camera is used.)</html>");

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(title, BorderLayout.NORTH);
        top.add(info, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        studentList.setCellRenderer(new EntryRenderer());
        studentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = studentList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Entry entry = listModel.get(index);
                    entry.present = !entry.present;
                    listModel.set(index, entry);
                }
            }
        });
        add(new JScrollPane(studentList), BorderLayout.CENTER);

        JButton manualButton = new JButton("Mark Manually");
        manualButton.addActionListener(e -> submitSession(false));
        JButton scanButton = new JButton("Scan Faces (Simulated)");
        scanButton.addActionListener(e -> submitSession(true));
        syncButton.addActionListener(e -> startSync());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(manualButton);
        buttons.add(scanButton);
        buttons.add(syncButton);

        JPanel messages = new JPanel(new GridLayout(2, 1));
        messages.add(statusLabel);
        messages.add(syncLabel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(messages, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void reloadRoster() {
        try {
            listModel.clear();
            for (Student student : studentDAO.findAll()) {
                listModel.addElement(new Entry(student));
            }
        } catch (SQLException e) {
            showStatus("Could not load students: " + e.getMessage(), Color.RED);
        }
    }

    private void submitSession(boolean faceScan) {
        List<Student> roster = new ArrayList<>();
        Set<Integer> present = new HashSet<>();
        for (int i = 0; i < listModel.size(); i++) {
            Entry entry = listModel.get(i);
            roster.add(entry.student);
            if (entry.present) {
                present.add(entry.student.getStudentId());
            }
        }

        if (roster.isEmpty()) {
            showStatus("No students enrolled yet.", Color.RED);
            return;
        }

        try {
            Map<String, Integer> tally = faceScan
                    ? attendanceService.markSessionByFaceScan(roster, present, currentUser.getUserId())
                    : attendanceService.markSessionManual(roster, present, currentUser.getUserId());
            int total = tally.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) {
                showStatus("Today's attendance was already recorded for everyone.", Color.RED);
                return;
            }
            showStatus((faceScan ? "Face scan saved. " : "Manual attendance saved. ")
                    + "Present: " + tally.getOrDefault("PRESENT", 0)
                    + " | Flagged for review: " + tally.getOrDefault("MANUAL_OVERRIDE", 0)
                    + " | Absent: " + tally.getOrDefault("ABSENT", 0), new Color(0, 130, 0));
        } catch (SQLException e) {
            showStatus("Session failed and was rolled back: " + e.getMessage(), Color.RED);
        }
    }

    private void startSync() {
        boolean started = syncService.startSync(
                message -> SwingUtilities.invokeLater(() -> syncLabel.setText(message)),
                () -> SwingUtilities.invokeLater(() -> syncButton.setEnabled(true)));
        if (started) {
            syncButton.setEnabled(false);
        }
    }

    private void showStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }

    private static class Entry {
        final Student student;
        boolean present;

        Entry(Student student) {
            this.student = student;
        }
    }

    private static class EntryRenderer extends JCheckBox implements ListCellRenderer<Entry> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Entry> list, Entry value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value.student.toString());
            setSelected(value.present);
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }
}