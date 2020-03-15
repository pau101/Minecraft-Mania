package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;

public class KillFunction implements CommandFunction {
    @Override
    public void run(final MinecraftMania.Context context) {
        context.commands().kill();
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
