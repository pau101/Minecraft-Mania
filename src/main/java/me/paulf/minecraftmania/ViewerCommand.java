package me.paulf.minecraftmania;

public final class ViewerCommand {
    private final String viewer;

    private final String command;

    public ViewerCommand(final String viewer, final String command) {
        this.viewer = viewer;
        this.command = command;
    }

    public String getViewer() {
        return this.viewer;
    }

    public String getCommand() {
        return this.command;
    }
}
