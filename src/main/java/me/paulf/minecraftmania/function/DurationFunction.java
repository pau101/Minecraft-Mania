package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;

import java.time.Duration;

public abstract class DurationFunction implements CommandFunction {
    private final Duration duration;

    protected DurationFunction(final Duration duration) {
        this.duration = duration;
    }

    @Override
    public void run(final MinecraftMania.Context context) {
        context.addRunningFunction(this.duration, this.createFunction());
    }

    protected abstract RunningFunction createFunction();
}
