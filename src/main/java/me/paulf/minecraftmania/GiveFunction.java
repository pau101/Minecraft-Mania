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
        final CompoundNBT nbt = new CompoundNBT();
        //noinspection deprecation
        if (this.item.getMaxStackSize() == 1) {
            final CompoundNBT display = new CompoundNBT();
            display.putString("Name", String.format("{\"translate\":\"%2$s\",\"extra\":[{\"text\":\" from %1$s\"}],\"color\":\"gold\"}", command.getViewer(), this.item.getTranslationKey()));
            nbt.put("display", display);
        }
        sender.give(this.item, nbt, 1);
        sender.tellraw(player.getGameProfile().getName(), new TranslationTextComponent("mania." + command.getCommand(),
            new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD),
            new TranslationTextComponent(this.item.getTranslationKey()).applyTextStyle(TextFormatting.AQUA)
        ));
    }
}