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
import java.time.LocalDate;
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
        JLabel info = new JLabel("<html>The list shows students with no record yet today.<br>"
                + "<b>Scan Faces (Simulated)</b> mock-recognises students automatically: a confidence of "
                + AttendanceService.CONFIDENCE_THRESHOLD + " or above is auto-marked PRESENT, below that is "
                + "flagged for review. Students it does not recognise stay in the list.<br>"
                + "<b>Mark Manually</b>: tick the students who are present and press it. Ticked become PRESENT, "
                + "unticked become ABSENT. (No real camera is used.)</html>");

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

        JButton scanButton = new JButton("Scan Faces (Simulated)");
        scanButton.addActionListener(e -> runScan());
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(e -> setAllPresent(true));
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> setAllPresent(false));
        JButton manualButton = new JButton("Mark Manually");
        manualButton.addActionListener(e -> submitManual());
        syncButton.addActionListener(e -> startSync());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(scanButton);
        buttons.add(selectAllButton);
        buttons.add(clearButton);
        buttons.add(manualButton);
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
            LocalDate today = LocalDate.now();
            for (Student student : studentDAO.findAll()) {
                if (!attendanceDAO.existsForStudentOnDate(student.getStudentId(), today)) {
                    listModel.addElement(new Entry(student));
                }
            }
        } catch (SQLException e) {
            showStatus("Could not load students: " + e.getMessage(), Color.RED);
        }
    }

    private void setAllPresent(boolean value) {
        for (int i = 0; i < listModel.size(); i++) {
            Entry entry = listModel.get(i);
            entry.present = value;
            listModel.set(i, entry);
        }
    }

    private List<Student> currentRoster() {
        List<Student> roster = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            roster.add(listModel.get(i).student);
        }
        return roster;
    }

    private void runScan() {
        List<Student> roster = currentRoster();
        if (roster.isEmpty()) {
            showStatus("Nobody is pending: everyone already has a record today, or no students are enrolled.", Color.RED);
            return;
        }

        try {
            Map<String, Integer> tally = attendanceService.scanSession(roster, currentUser.getUserId());
            int notDetected = tally.getOrDefault("NOT_DETECTED", 0);
            showStatus("Face scan done. Present: " + tally.getOrDefault("PRESENT", 0)
                    + " | Flagged for review: " + tally.getOrDefault("MANUAL_OVERRIDE", 0)
                    + " | Not recognised: " + notDetected
                    + (notDetected > 0 ? " (still in the list, mark them manually)" : ""),
                    new Color(0, 130, 0));
            reloadRoster();
        } catch (SQLException e) {
            showStatus("Scan failed and was rolled back: " + e.getMessage(), Color.RED);
        }
    }

    private void submitManual() {
        List<Student> roster = currentRoster();
        Set<Integer> present = new HashSet<>();
        for (int i = 0; i < listModel.size(); i++) {
            Entry entry = listModel.get(i);
            if (entry.present) {
                present.add(entry.student.getStudentId());
            }
        }

        if (roster.isEmpty()) {
            showStatus("Nobody is pending: everyone already has a record today, or no students are enrolled.", Color.RED);
            return;
        }

        if (present.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "No student is ticked. This will mark all " + roster.size()
                            + " listed students ABSENT for today and cannot be redone. Continue?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Map<String, Integer> tally = attendanceService.markSessionManual(roster, present, currentUser.getUserId());
            showStatus("Manual attendance saved. Present: " + tally.getOrDefault("PRESENT", 0)
                    + " | Absent: " + tally.getOrDefault("ABSENT", 0), new Color(0, 130, 0));
            reloadRoster();
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