package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import me.paulf.minecraftmania.function.CommandFunction;

import java.util.Comparator;

public final class CommandMap {
    private final ImmutableMap<String, CommandFunction> map;

    private final CommandFunction noop = (p) -> {};

    private CommandMap(final Builder builder) {
        this.map = builder.map.build();
    }

    public ImmutableSet<String> keys() {
        return this.map.keySet();
    }

    public CommandFunction get(final ViewerCommand command) {
        return this.map.getOrDefault(command.getCommand(), this.noop);
    }

    public static final class Builder {
        private final ImmutableSortedMap.Builder<String, CommandFunction> map = new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

        public Builder add(final String name, final CommandFunction command) {
            this.map.put(name, command);
            return this;
        }

        public CommandMap build() {
            return new CommandMap(this);
        }
    }
}
