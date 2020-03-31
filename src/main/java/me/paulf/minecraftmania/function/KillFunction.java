package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;

public class KillFunction implements CommandFunction {
    @Override
    public void run(final MinecraftMania.CommandContext context) {
        context.commands().kill();
    }

    public static boolean isOperable(final MinecraftMania.Context context) {
        return context.commands().hasKill();
    }
}
