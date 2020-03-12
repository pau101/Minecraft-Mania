package me.paulf.minecraftmania;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod(MinecraftMania.ID)
public final class MinecraftMania {
    public static final String ID = "minecraftmania";

    private final ViewerCommandMap map = new ViewerCommandMap.Builder()
        .add("summon_zombie", new SpawnCommand(EntityType.ZOMBIE))
        .add("give_diamond", new GiveCommand(Items.DIAMOND))
        .build();

    private State state = new OutOfGameState();

    public MinecraftMania() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedInEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.RespawnEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedOutEvent>addListener(e -> this.leave());
            new ClientCommandProvider.Builder()
                .add(this::mania)
                .build()
                .register(MinecraftForge.EVENT_BUS);
        });
    }

    private <S> LiteralArgumentBuilder<S> mania(final ClientCommandProvider.Helper<S> helper) {
        return LiteralArgumentBuilder.<S>literal("mania")
            .then(helper.executes(
                RequiredArgumentBuilder.<S, String>argument("command", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                        for (final String command : this.map.keys()) {
                            if (command.startsWith(remaining)) {
                                builder.suggest(command);
                            }
                        }
                        return builder.buildFuture();
                    }),
                ctx -> {
                    this.state.accept(new ViewerCommand(ctx.getSource().getName(), StringArgumentType.getString(ctx, "command")));
                    return 1;
                }));
    }

    private void leave() {
        this.state = new OutOfGameState();
    }

    private void join(final ClientPlayerNetworkEvent event) {
        final ClientPlayerEntity player = event.getPlayer();
        if (player == null) {
            this.leave();
        } else {
            this.state = new InGameState(player);
        }
    }

    abstract class State {
        abstract void accept(final ViewerCommand command);
    }

    class OutOfGameState extends State {
        @Override
        void accept(final ViewerCommand command) {

        }
    }

    class InGameState extends State {
        final ClientPlayerEntity user;

        final CommandSender sender;

        InGameState(final ClientPlayerEntity user) {
            this.user = user;
            this.sender = new OperatorCommandSender(user::sendChatMessage);
        }

        @Override
        void accept(final ViewerCommand command) {
            MinecraftMania.this.map.get(command).run(command, this.sender, this.user.world, this.user);
        }
    }
}
