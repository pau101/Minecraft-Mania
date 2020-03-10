package me.paulf.minecraftmania;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface CommandFunction {
    void run(final ViewerCommand command, final CommandSender sender, final World world, final PlayerEntity player);
}