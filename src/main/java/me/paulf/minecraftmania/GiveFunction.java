package me.paulf.minecraftmania;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GiveFunction implements CommandFunction {
    private final Item item;

    public GiveFunction(final Item item) {
        this.item = item;
    }

    @Override
    public void run(final ViewerCommand command, final CommandSender sender, final World world, final PlayerEntity player) {
        sender.give(this.item, new CompoundNBT(), 1);
        sender.tellraw(player.getGameProfile().getName(), new TranslationTextComponent("mania." + command.getCommand(),
            new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD),
            new TranslationTextComponent(this.item.getTranslationKey()).applyTextStyle(TextFormatting.AQUA)
        ));
    }
}