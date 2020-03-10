package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(MinecraftMania.ID)
public final class MinecraftMania {
    public static final String ID = "minecraftmania";

    private final ViewerCommandMap map = new ViewerCommandMap.Builder()
        .add("summon_zombie", new SpawnCommand(EntityType.ZOMBIE))
        .add("give_diamond", new GiveCommand(Items.DIAMOND))
        .build();

    private final ViewerCommandProcessor noop = cmd -> {};

    private ViewerCommandProcessor processor = this.noop;

    public MinecraftMania() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedInEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.RespawnEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedOutEvent>addListener(e -> this.leave());
            MinecraftForge.EVENT_BUS.<ClientChatEvent>addListener(e -> {
                final String message = e.getMessage();
                if (message.startsWith("/mm ") || message.equals("/mm")) {
                    final String command = message.substring("/mm".length()).trim();
                    if (command.isEmpty()) {
                        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("todo"));
                    } else {
                        this.processor.accept(new ViewerCommand(Minecraft.getInstance().getSession().getUsername(), command));
                    }
                    e.setCanceled(true);
                    Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(message);
                }
            });
        });
    }

    private void leave() {
        this.processor = this.noop;
    }

    private void join(final ClientPlayerNetworkEvent event) {
        final ClientPlayerEntity player = event.getPlayer();
        if (player == null) {
            this.leave();
        } else {
            this.processor = new ViewerCommandMessenger(this.map, player, new OperatorCommandSender(player::sendChatMessage));
        }
    }
}
