package uj.wmii.pwj.gvt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

public class CommandFactoryExtension extends  CommandFactory {

    public CommandFactoryExtension(ExitHandler exitHandler){
        super(exitHandler);
    }

    @Override
    protected void init() throws IOException {

        File gvtDir = new File(currentDir, ".gvt");

        if(gvtDir.exists()){
            exitHandler.exit(10,"Current directory is already initialized.");
            return;
        }

        createDir(gvtDir);

        File versionsDir = new File(gvtDir, "versions");
        createDir(versionsDir);

        File initialVersionDir = new File(versionsDir, "0");
        createDir(initialVersionDir);

        updateVersionFiles(0 , 0);

        File history = new File(gvtDir, "history.txt");
        Files.writeString(history.toPath(), "0: GVT initialized.\n");

        File tracked = new File(gvtDir, "tracked.txt");
        Files.createFile(tracked.toPath());

        addMessageAndCheckIfItIsCustom(args, initialVersionDir, 0, "GVT initialized.");

        exitHandler.exit(0,"Current directory initialized successfully.");

    }

    protected void add() throws IOException {
        if(!checkIfGvtIsInitialized()){
            return;
        }

        if (args.length < 2) {
            exitHandler.exit(20, "Please specify file to add.");
            return;
        }

        String fileToAdd = args[1];
        File gvtDir = new File(currentDir, ".gvt");
        File versionsDir = new File(gvtDir, "versions");

        File sourceFile = new File(currentDir, fileToAdd);
        if (!sourceFile.exists()) {
            exitHandler.exit(21, "File not found. File: " + fileToAdd);
            return;
        }

        TrackedFilesManager tracked = new TrackedFilesManager(gvtDir.toPath());
        tracked.initializeIfNeeded();

        if (tracked.isTracked(fileToAdd)) {
            exitHandler.exit(0, "File already added. File: " + fileToAdd);
            return;
        }

        int newVersionNum = getLastVersion(gvtDir) + 1;
        File prevVersionDir = new File(versionsDir, String.valueOf(getLastVersion(gvtDir)));
        File newVersionDir = new File(versionsDir, String.valueOf(newVersionNum));
        createDir(newVersionDir);

        filesCopy(prevVersionDir, newVersionDir);

        Files.copy(sourceFile.toPath(), new File(newVersionDir, fileToAdd).toPath());
        tracked.addFile(fileToAdd);

        updateVersionFiles(newVersionNum, newVersionNum);
        updateHistoryAndAddMessage(newVersionDir, newVersionNum, fileToAdd, "File added successfully. File: ");

        exitHandler.exit(0, "File added successfully. File: " + fileToAdd);

    }


    protected void detach() throws IOException{
        if(!checkIfGvtIsInitialized()){
            return;
        }

        if(args.length < 2) {
            exitHandler.exit(30, "Please specify file to detach.");
            return;
        }

        File gvtDir = new File(currentDir, ".gvt");
        File versionsDir = new File(gvtDir,"versions");

        Integer newVersionNum = getLastVersion(gvtDir) + 1;
        File newVersionDir = new File(versionsDir, String.valueOf(newVersionNum));
        File prevVersionDir = new File(versionsDir, String.valueOf(getLastVersion(gvtDir)));
        String fileToBeDetached = args[1];

        TrackedFilesManager trackedManager = new TrackedFilesManager(gvtDir.toPath());
        trackedManager.initializeIfNeeded();

        if(!trackedManager.isTracked(fileToBeDetached)) {
            exitHandler.exit(0, "File is not added to gvt. File: " + fileToBeDetached );
            return;
        }

        createDir(newVersionDir);
        filesCopy(prevVersionDir, newVersionDir);

        File detachedFile = new File(newVersionDir, fileToBeDetached);
        deleteFile(detachedFile);
        trackedManager.removeFile(fileToBeDetached);

        updateVersionFiles(newVersionNum, newVersionNum);

        updateHistoryAndAddMessage(newVersionDir, newVersionNum, fileToBeDetached, "File detached successfully. File: ");

        exitHandler.exit(0, "File detached successfully. File: " + fileToBeDetached);

    }

    protected  void checkout() throws IOException{
        if(!checkIfGvtIsInitialized()){
            return;
        }

        if (args.length < 2) {
            exitHandler.exit(60, "Invalid version number: null");
            return;
        }

        File gvtDir = new File(currentDir, ".gvt");
        File versionsDir = new File(gvtDir, "versions");

        int versionToCheckout;
        try {
            versionToCheckout = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            exitHandler.exit(60, "Invalid version number: " + args[1]);
            return;
        }

        File checkoutVersionDir = new File(versionsDir, String.valueOf(versionToCheckout));
        if (!checkoutVersionDir.exists()) {
            exitHandler.exit(60, "Invalid version number: " + args[1]);
            return;
        }

        filesCopy(checkoutVersionDir, new File(currentDir));

        updateVersionFiles(versionToCheckout, getLastVersion(gvtDir));

        exitHandler.exit(0, "Checkout successful for version: " + args[1]);
    }

    protected void commit() throws IOException{
        if(!checkIfGvtIsInitialized()){
            return;
        }

        if(args.length < 2){
            exitHandler.exit(50, "Please specify file to commit.");
            return;
        }

        String fileToCommit = args[1];
        File gvtDir = new File(currentDir, ".gvt");
        File versionsDir = new File(gvtDir,"versions");

        if(!checkFileExistenceIn(currentDir, fileToCommit)){
            exitHandler.exit(51, "File not found. File: " + fileToCommit);
            return;
        }

        TrackedFilesManager tracked = new TrackedFilesManager(gvtDir.toPath());
        tracked.initializeIfNeeded();

        if (!tracked.isTracked(fileToCommit)) {
            exitHandler.exit(0, "File is not added to gvt. File: " + fileToCommit);
            return;
        }

        Integer newVersionNum = getLastVersion(gvtDir) + 1;
        File prevVersionDir = new File(versionsDir, String.valueOf(getLastVersion(gvtDir)));
        File newVersionDir = new File(versionsDir, String.valueOf(newVersionNum));
        createDir(newVersionDir);

        filesCopy(prevVersionDir, newVersionDir);

        File sourceFile = new File(currentDir, fileToCommit);
        Files.copy(sourceFile.toPath(), new File(newVersionDir, fileToCommit).toPath(), StandardCopyOption.REPLACE_EXISTING);

        updateVersionFiles(newVersionNum, newVersionNum);
        updateHistoryAndAddMessage(newVersionDir, newVersionNum, fileToCommit, "File committed successfully. File: ");

        exitHandler.exit(0, "File committed successfully. File: " + fileToCommit);

    }

    protected void history() throws IOException {
        if(!checkIfGvtIsInitialized()){
            return;
        }

        File gvtDir = new File(currentDir, ".gvt");
        File historyFile = new File(gvtDir, "history.txt");

        List<String> lines = Files.readAllLines(historyFile.toPath());
        int linesToPrintNum = -1;

        if (args.length >= 3 && "-last".equals(args[1])) {
            try {
                linesToPrintNum = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {}
        }

        if (linesToPrintNum > 0 && linesToPrintNum < lines.size()) {
            lines = lines.subList(0, linesToPrintNum);
        }

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String firstLine = line.split("\n")[0];
            sb.append(firstLine).append("\n");
        }

        exitHandler.exit(0, sb.toString());
    }


    protected void version() throws IOException {
        if(!checkIfGvtIsInitialized()){
            return;
        }

        File gvtDir = new File(currentDir, ".gvt");
        File versionsDir = new File(gvtDir, "versions");

        int versionNum;

        if (args.length < 2) {
            versionNum = getActiveVersion(gvtDir);
        } else {
            try {
                versionNum = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                exitHandler.exit(60, "Invalid version number: " + args[1]);
                return;
            }
        }

        File versionDir = new File(versionsDir, String.valueOf(versionNum));
        if (!versionDir.exists()) {
            exitHandler.exit(60, "Invalid version number: " + versionNum);
            return;
        }

        File messageFile = new File(versionDir, "message.txt");

        String message = Files.readString(messageFile.toPath()).trim();

        exitHandler.exit(0, message);
    }

    private boolean checkIfGvtIsInitialized(){
        File gvtDir = new File(currentDir, ".gvt");
        if (!gvtDir.exists()) {
            exitHandler.exit(-2, "Current directory is not initialized. Please use init command to initialize.");
            return false;
        }
        return true;
    }

    private int getLastVersion(File gvtDir) throws IOException {
        File lastVersion = new File(gvtDir, "lastVersion.txt");
        return Integer.parseInt(Files.readString(lastVersion.toPath()).trim());
    }

    private int getActiveVersion(File gvtDir) throws IOException {
        File activeVersion = new File(gvtDir, "activeVersion.txt");
        return Integer.parseInt(Files.readString(activeVersion.toPath()).trim());
    }

    private boolean checkFileExistenceIn(String dir, String toAdd ){
        File sourceFile = new File(dir, toAdd);
        return sourceFile.exists();
    }

    private void createDir(File dir) throws IOException {
        if (!dir.mkdir()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
    }

    private void deleteFile(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getPath());
        }
    }

    private void filesCopy(File fromDir, File toDir ) throws IOException {
        Files.walk(fromDir.toPath())
                .filter(path -> !Files.isDirectory(path))
                .forEach(source -> {
                    try {
                        File destination = new File(toDir, fromDir.toPath().relativize(source).toString());
                        Files.copy(source, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                        exitHandler.exit(-3, "Underlying system problem. See ERR for details.");
                    }
                });
    }

    private void updateVersionFiles(Integer activeVersionNum, Integer newVersionNum) throws IOException {
        File gvtDir = new File(currentDir, ".gvt");
        File lastVersionFile = new File(gvtDir, "lastVersion.txt");
        File activeVersionFile = new File(gvtDir, "activeVersion.txt");

        Files.writeString(lastVersionFile.toPath(), newVersionNum.toString());
        Files.writeString(activeVersionFile.toPath(), activeVersionNum.toString());
    }

    private void updateHistoryAndAddMessage(File newVersionDir, Integer newVersionNum, String fileUpdated, String defaultMessage) throws IOException {
        File gvtDir = new File(currentDir, ".gvt");
        File history = new File(gvtDir, "history.txt");
        String existingHistory = Files.readString(history.toPath());

        if(addMessageAndCheckIfItIsCustom(args, newVersionDir, newVersionNum, defaultMessage + fileUpdated)){
            String firstLine = args[3].split("\\R")[0];
            String newEntry = newVersionNum + ": " + firstLine + "\n";
            Files.writeString(history.toPath(), newEntry + existingHistory);
        } else {
            String newEntry = newVersionNum + ": " + defaultMessage + fileUpdated + "\n";
            Files.writeString(history.toPath(), newEntry + existingHistory);
        }
    }
    private boolean addMessageAndCheckIfItIsCustom(String[] args, File newVersionDir, Integer newVersionNum, String defaultMessage) throws IOException {
        File message = new File(newVersionDir, "message.txt");
        if (args.length >= 4 && "-m".equals(args[2])) {

            Files.writeString(message.toPath(), "Version: " + newVersionNum + "\n" + args[3]);
            return true;
        }
        Files.writeString(message.toPath(), "Version: " + newVersionNum + "\n" + defaultMessage);
        return false;
    }
}
