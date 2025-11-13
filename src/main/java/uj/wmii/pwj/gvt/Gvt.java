package uj.wmii.pwj.gvt;

import java.util.Map;

public class Gvt {

    private final ExitHandler exitHandler;
    public static String[] argsCopy = null;

    public Gvt(ExitHandler exitHandler) {
        this.exitHandler = exitHandler;

    }

    public static void main(String... args) {
        Gvt gvt = new Gvt(new ExitHandler());
        gvt.mainInternal(args);
    }

    public void mainInternal(String... args) {
        if ( args.length == 0){
            exitHandler.exit(1 , "Please specify command.");
            return;
        }

        String argument = args[0].toUpperCase();
        argsCopy = args;

        CommandFactoryExtension factory = new CommandFactoryExtension(this.exitHandler);
        Map<String, Command> commandToExecutionMap = factory.generateCommands();

        Command command = commandToExecutionMap.get(argument);

        if(command == null){
            exitHandler.exit(1 , "Unknown command " + args[0] + "." );
            return;
        }


        command.exec(this);
    }
}
