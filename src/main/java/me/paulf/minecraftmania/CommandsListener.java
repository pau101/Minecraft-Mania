package me.paulf.minecraftmania;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.ISuggestionProvider;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.BiConsumer;

public class CommandsListener {
    private final BiConsumer<ClientPlayerEntity, CommandDispatcher<ISuggestionProvider>> callback;

    private CommandDispatcher<ISuggestionProvider> knownDispatcher = null;

    public CommandsListener(final BiConsumer<ClientPlayerEntity, CommandDispatcher<ISuggestionProvider>> callback) {
        this.callback = callback;
    }

    public void register(final IEventBus bus) {
        bus.addListener(this::tick);
        bus.addListener(this::login);
        bus.addListener(this::logout);
    }

    // initialize to empty CommandDispatcher to only callback for SCommandListPacket
    private void login(final ClientPlayerNetworkEvent.LoggedInEvent e) {
        final Minecraft mc = Minecraft.getInstance();
        final ClientPlayerEntity player = mc.player;
        if (player != null) {
            this.knownDispatcher = player.connection.getCommandDispatcher();
        }
    }

    private void tick(final TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && this.knownDispatcher != null) {
            final ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                final CommandDispatcher<ISuggestionProvider> dispatcher = player.connection.getCommandDispatcher();
                if (dispatcher != this.knownDispatcher) {
                    this.callback.accept(player, dispatcher);
                    this.knownDispatcher = dispatcher;
                }
            }
        }
    }

    private void logout(final ClientPlayerNetworkEvent.LoggedOutEvent e) {
        this.knownDispatcher = null;
    }
}
