package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.BiFunction;
import java.util.function.Function;

public class OpenScreenFunction implements CommandFunction {
    private final BiFunction<? super MinecraftMania.CommandContext, ? super Screen, Screen> factory;

    public OpenScreenFunction(final Function<? super Screen, Screen> factory) {
        this((context, parent) -> factory.apply(parent));
    }

    public OpenScreenFunction(final BiFunction<? super MinecraftMania.CommandContext, ? super Screen, Screen> factory) {
        this.factory = factory;
    }

    @Override
    public void run(final MinecraftMania.CommandContext context) {
        final Minecraft mc = Minecraft.getInstance();
        final Screen screen = this.factory.apply(context, mc.currentScreen);
        mc.enqueue(() -> mc.displayGuiScreen(screen));
    }
}
