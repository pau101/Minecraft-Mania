package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class GiveFunction implements CommandFunction {
    private final Item item;

    public GiveFunction(final Item item) {
        this.item = item;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.CommandContext context) {
        return new TranslationTextComponent("mania.give", context.getViewerName(), new TranslationTextComponent(this.item.getTranslationKey()).applyTextStyle(TextFormatting.AQUA));
    }

    @Override
    public void run(final MinecraftMania.CommandContext context) {
        context.commands().give(this.item, new CompoundNBT(), 1);
    }

    public static boolean isOperable(final MinecraftMania.Context context) {
        return context.commands().hasGive();
    }
}