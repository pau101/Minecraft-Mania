package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.CommandSender;
import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.world.GameRules;

public class NightTimeFunction implements CommandFunction {
    @Override
    public void run(final MinecraftMania.CommandContext context) {
        if (context.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            context.commands().time(CommandSender.TimeOfDay.NIGHT);
        } else {
            context.commands().time(CommandSender.TimeOfDay.MIDNIGHT);
        }
    }
}
