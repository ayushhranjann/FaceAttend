package com.faceattend.service;

import com.faceattend.dao.AttendanceDAO;
import com.faceattend.model.AttendanceRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class SyncService {

    private final AttendanceDAO attendanceDAO;
    private boolean running = false;

    public SyncService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized boolean startSync(Consumer<String> onProgress, Runnable onFinished) {
        if (running) {
            return false;
        }
        running = true;
        Thread worker = new Thread(() -> runSync(onProgress, onFinished), "sync-thread");
        worker.setDaemon(true);
        worker.start();
        return true;
    }

    private void runSync(Consumer<String> onProgress, Runnable onFinished) {
        try {
            List<AttendanceRecord> pending = attendanceDAO.findUnsynced();
            if (pending.isEmpty()) {
                onProgress.accept("Nothing to sync.");
                return;
            }
            int done = 0;
            for (AttendanceRecord record : pending) {
                Thread.sleep(300);
                attendanceDAO.markSynced(record.getRecordId());
                done++;
                onProgress.accept("Synced " + done + " of " + pending.size());
            }
            onProgress.accept("Sync complete.");
        } catch (SQLException e) {
            onProgress.accept("Sync failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onProgress.accept("Sync interrupted.");
        } finally {
            finish();
            onFinished.run();
        }
    }

    private synchronized void finish() {
        running = false;
    }
}