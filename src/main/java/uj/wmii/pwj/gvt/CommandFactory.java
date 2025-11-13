package uj.wmii.pwj.gvt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

    protected final String currentDir;
    protected final ExitHandler exitHandler;
    protected final String[] args;

    public CommandFactory(ExitHandler exitHandler){
        currentDir = System.getProperty("user.dir");
        this.exitHandler = exitHandler;
        this.args = Gvt.argsCopy;
    }

    public Map<String, Command> generateCommands() {
        Map<String, Command> commandMap = new HashMap<>();
        commandMap.put("INIT", gvtInstance -> runWithIoHandling(this::init, "Underlying system problem. ", -3, false));
        commandMap.put("ADD", gvtInstance -> runWithIoHandling(this::add, "File cannot be added. ", 22, true));
        commandMap.put("DETACH", gvtInstance -> runWithIoHandling(this::detach, "File cannot be detached. ", 31, true));
        commandMap.put("CHECKOUT", gvtInstance -> runWithIoHandling(this::checkout, "Underlying system problem. ", -3, false));
        commandMap.put("COMMIT", gvtInstance -> runWithIoHandling(this::commit, "File cannot be committed. ", 52, true));
        commandMap.put("HISTORY", gvtInstance -> runWithIoHandling(this::history, "Underlying system problem. ", -3,false));
        commandMap.put("VERSION", gvtInstance -> runWithIoHandling(this::version, "Underlying system problem. ", -3,false));

        return commandMap;
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }

    private void runWithIoHandling(IoRunnable action, String errorMessage, int code, boolean hasFileArg) {
        try {
            action.run();
        } catch (IOException e) {
            if(hasFileArg && args.length > 1 ) {
                e.printStackTrace(System.err);
                exitHandler.exit(code, errorMessage + "See ERR for details. File: " + args[1]);
            } else {
                e.printStackTrace(System.err);
                exitHandler.exit(code, errorMessage + "See ERR for details.");
            }
        }
    }

    protected void init() throws IOException {}
    protected void add() throws IOException {}
    protected void detach() throws IOException{}
    protected void checkout() throws IOException{}
    protected void commit() throws IOException{}
    protected void history() throws IOException{}
    protected void version() throws IOException{}


}
