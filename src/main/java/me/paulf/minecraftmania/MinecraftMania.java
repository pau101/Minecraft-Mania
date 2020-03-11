package me.paulf.minecraftmania;

import com.google.gson.internal.UnsafeAllocator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.profiler.DebugProfiler;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.function.BiFunction;

@Mod(MinecraftMania.ID)
public final class MinecraftMania {
    public static final String ID = "minecraftmania";

    private final ViewerCommandMap map = new ViewerCommandMap.Builder()
        .add("summon_zombie", new SpawnCommand(EntityType.ZOMBIE))
        .add("give_diamond", new GiveCommand(Items.DIAMOND))
        .build();

    private State state = new OutOfGameState();

    private <S> LiteralArgumentBuilder<S> mania(final BiFunction<String, LiteralArgumentBuilder<S>, LiteralArgumentBuilder<S>> consumer) {
        final LiteralArgumentBuilder<S> mania = LiteralArgumentBuilder.literal("mania");
        for (final String command : this.map.keys()) {
            mania.then(consumer.apply(command, LiteralArgumentBuilder.literal(command)));
        }
        return mania;
    }

    private static final UnsafeAllocator ALLOCATOR = UnsafeAllocator.create();

    public MinecraftMania() {
        final Commands commands;
        try {
            commands = ALLOCATOR.newInstance(Commands.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
        ObfuscationReflectionHelper.setPrivateValue(Commands.class, commands, dispatcher, "field_197062_b");

        dispatcher.register(this.mania((command, builder) -> builder.executes(ctx -> {
            this.state.accept(new ViewerCommand(ctx.getSource().getName(), command));
            return 1;
        })));

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedInEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.RespawnEvent>addListener(e -> this.join(e));
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedOutEvent>addListener(e -> this.leave());
            MinecraftForge.EVENT_BUS.<GuiScreenEvent.KeyboardKeyPressedEvent.Pre>addListener(e -> {
                if (e.getGui() instanceof ChatScreen) {
                    final ClientPlayNetHandler net = Minecraft.getInstance().getConnection();
                    if (net != null) {
                        final RootCommandNode<ISuggestionProvider> root = net.getCommandDispatcher().getRoot();
                        if (root.getChild("mania") == null) {
                            root.addChild(this.<ISuggestionProvider>mania((command, builder) -> builder).build());
                        }
                    }
                }
            });
            MinecraftForge.EVENT_BUS.<ClientChatEvent>addListener(e -> {
                final String message = e.getMessage();
                if (message.startsWith("/mania ") || message.equals("/mania")) {
                    e.setCanceled(true);
                    Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(message);
                    final ClientPlayerEntity user = Minecraft.getInstance().player;
                    if (user != null) {
                        commands.handleCommand(this.source(user), message);
                    }
                }
            });
        });
    }

    static final class DummyServer extends IntegratedServer {
        static DummyServer INSTANCE;

        static {
            try {
                INSTANCE = ALLOCATOR.newInstance(DummyServer.class);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        public DummyServer() {
            //noinspection ConstantConditions
            super(null, null, null, null, null, null, null, null, null);
        }

        @Override
        public DebugProfiler getProfiler() {
            return new DebugProfiler(() -> 0);
        }
    }

    private CommandSource source(final Entity e) {
        //noinspection ConstantConditions
        return new CommandSource(new ICommandSource() {
            @Override
            public void sendMessage(final ITextComponent message) {
                e.sendMessage(message);
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return true;
            }

            @Override
            public boolean shouldReceiveErrors() {
                return true;
            }

            @Override
            public boolean allowLogging() {
                return false;
            }
        }, e.getPositionVec(), e.getPitchYaw(), null, 4, e.getName().getString(), e.getDisplayName(), DummyServer.INSTANCE, e);
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
