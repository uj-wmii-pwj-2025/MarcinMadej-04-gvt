package uj.wmii.pwj.gvt;

@FunctionalInterface
public interface Command {
    void exec(Gvt gvtInstance);
}
