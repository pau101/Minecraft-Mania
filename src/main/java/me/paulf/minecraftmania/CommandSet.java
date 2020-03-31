package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableList;
import me.paulf.minecraftmania.function.CommandFunction;

import java.util.function.Predicate;

public class CommandSet {
    private final Predicate<MinecraftMania.Context> operable;

    private final ImmutableList<Node> nodes;

    private CommandSet(final Builder builder) {
        this.operable = builder.operable;
        this.nodes = builder.nodes.build();
    }

    public void build(final MinecraftMania.Context context, final MinecraftMania.CommandMap.Builder builder) {
        if (this.operable.test(context)) {
            for (final Node n : this.nodes) {
                n.accept(context, builder);
            }
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<Node> nodes = new ImmutableList.Builder<>();

        private final Predicate<MinecraftMania.Context> operable;

        public Builder() {
            this(ctx -> true);
        }

        public Builder(final Predicate<MinecraftMania.Context> operable) {
            this.operable = operable;
        }

        public Builder add(final String name, final CommandFunction command) {
            this.nodes.add((context, builder) -> builder.add(name, command));
            return this;
        }

        public Builder add(final CommandSet set) {
            this.nodes.add(set::build);
            return this;
        }

        public CommandSet build() {
            return new CommandSet(this);
        }
    }

    interface Node {
        void accept(final MinecraftMania.Context context, final MinecraftMania.CommandMap.Builder builder);
    }
}
