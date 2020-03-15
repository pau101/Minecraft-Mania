package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.world.GameRules;

public class DayTimeFunction implements CommandFunction {
    @Override
    public void run(final MinecraftMania.Context context) {
        if (context.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            context.commands().time("day");
        } else {
            context.commands().time("noon");
        }
    }
}
