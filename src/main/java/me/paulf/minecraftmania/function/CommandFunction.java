package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface CommandFunction {
    default ITextComponent getMessage(final MinecraftMania.Context context) {
        return new TranslationTextComponent("mania." + context.getCommand(), context.getViewerName());
    }

    void run(final MinecraftMania.Context context);
}