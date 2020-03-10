package me.paulf.minecraftmania;

import net.minecraft.client.entity.player.ClientPlayerEntity;

public class ViewerCommandMessenger implements ViewerCommandProcessor {
    private final ViewerCommandMap map;

    private final ClientPlayerEntity player;

    private final CommandSender sender;

    public ViewerCommandMessenger(final ViewerCommandMap map, final ClientPlayerEntity player, final CommandSender sender) {
        this.map = map;
        this.player = player;
        this.sender = sender;
    }

    @Override
    public void accept(final ViewerCommand command) {
        this.map.get(command).run(command, this.sender, this.player.world, this.player);
    }
}