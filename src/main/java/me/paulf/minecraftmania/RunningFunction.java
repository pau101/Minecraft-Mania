package me.paulf.minecraftmania;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface RunningFunction {
    default ITextComponent getMessage(final ViewerCommand command, final int seconds) {
        return new TranslationTextComponent("mania." + command.getCommand() + ".running", seconds);
    }

    default void start() {
    }

    default void tick() {
    }

    default void stop() {
    }
}
