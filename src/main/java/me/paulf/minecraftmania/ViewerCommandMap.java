package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ViewerCommandMap {
    private final ImmutableMap<String, CommandFunction> map;

    private final CommandFunction noop = (command, c, w, p) -> {};

    private ViewerCommandMap(final Builder builder) {
        this.map = builder.map.build();
    }

    public ImmutableSet<String> keys() {
        return this.map.keySet();
    }

    public CommandFunction get(final ViewerCommand command) {
        return this.map.getOrDefault(command.getCommand(), this.noop);
    }

    public static class Builder {
        private final ImmutableMap.Builder<String, CommandFunction> map = new ImmutableMap.Builder<>();

        public Builder add(final String name, final CommandFunction command) {
            this.map.put(name, command);
            return this;
        }

        public ViewerCommandMap build() {
            return new ViewerCommandMap(this);
        }
    }
}