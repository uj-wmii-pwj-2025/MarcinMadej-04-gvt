package uj.wmii.pwj.gvt;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TrackedFilesManager {

    private final Path trackedFilePath;

    public TrackedFilesManager(Path gvtDirPath) {
        this.trackedFilePath = gvtDirPath.resolve("tracked.txt");
    }

    public void initializeIfNeeded() throws IOException {
        if (!Files.exists(trackedFilePath)) {
            Files.createFile(trackedFilePath);
        }
    }

    public Set<String> getTrackedFiles() throws IOException {
        initializeIfNeeded();
        List<String> lines = Files.readAllLines(trackedFilePath);
        Set<String> tracked = new HashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) tracked.add(trimmed);
        }
        return tracked;
    }

    public boolean isTracked(String fileName) throws IOException {
        return getTrackedFiles().contains(fileName);
    }

    public void addFile(String fileName) throws IOException {
        Set<String> tracked = getTrackedFiles();
        if (tracked.add(fileName)) {
            saveTrackedFiles(tracked);
        }
    }

    public void removeFile(String fileName) throws IOException {
        Set<String> tracked = getTrackedFiles();
        if (tracked.remove(fileName)) {
            saveTrackedFiles(tracked);
        }
    }

    private void saveTrackedFiles(Set<String> tracked) throws IOException {
        Files.write(trackedFilePath, tracked);
    }
}
