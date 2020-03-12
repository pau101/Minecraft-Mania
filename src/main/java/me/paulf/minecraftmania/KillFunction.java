package me.paulf.minecraftmania;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class KillFunction implements CommandFunction {
    @Override
    public void run(final ViewerCommand command, final CommandSender sender, final World world, final PlayerEntity player) {
        sender.kill();
        sender.tellraw(
            "@s",
            new TranslationTextComponent("mania.kill",
                new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD)
            )
        );
        sender.tellraw(
            "@a[name=!" + player.getName().getString() + "]",
            new TranslationTextComponent("mania.kill.others",
                player.getName(),
                new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD)
            )
        );
        /*final BlockPos pos = new BlockPos(0, 60, 0);
        sender.setblock(pos, Blocks.COMMAND_BLOCK.getDefaultState().with(CommandBlockBlock.FACING, Direction.NORTH), false);
        sender.setblock(pos.north(), Blocks.CHAIN_COMMAND_BLOCK.getDefaultState(), false);
        ((ClientPlayerEntity) player).connection.sendPacket(new CUpdateCommandBlockPacket(pos.north(), "fill ~ ~ ~ ~ ~ ~1 minecraft:bedrock", CommandBlockTileEntity.Mode.SEQUENCE, false, false, true));
        ((ClientPlayerEntity) player).connection.sendPacket(new CUpdateCommandBlockPacket(pos, "tellraw @a " + ITextComponent.Serializer.toJson(new TranslationTextComponent("death.attack.player",
            player.getDisplayName(),
            new StringTextComponent(command.getViewer()).applyTextStyle(TextFormatting.GOLD)
        )), CommandBlockTileEntity.Mode.REDSTONE, false, false, true));*/
    }
}
