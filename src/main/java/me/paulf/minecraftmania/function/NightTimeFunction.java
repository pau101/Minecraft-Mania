package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;

public class NightTimeFunction implements CommandFunction {
    @Override
    public void run(final MinecraftMania.Context context) {
        if (context.world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            context.commands().time("night");
        } else {
            context.commands().time("midnight");
        }
    }
}
