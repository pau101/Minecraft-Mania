package me.paulf.minecraftmania;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

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

    public static ViewerCommand from(final JsonObject json) throws JsonSyntaxException {
        return new ViewerCommand(JsonElements.getString(json, "viewer"), JsonElements.getString(json, "command"));
    }
}
