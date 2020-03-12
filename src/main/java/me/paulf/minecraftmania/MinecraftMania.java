package me.paulf.minecraftmania;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.function.Predicate;

@Mod(MinecraftMania.ID)
public final class MinecraftMania {
    public static final String ID = "minecraftmania";

    private final ViewerCommandMap map = new ViewerCommandMap.Builder()
        // Misc
        .add("kill", new KillCommand())
        // Summons
        .add("summon_creeper", new SummonFunction(EntityType.CREEPER))
        .add("summon_blaze", new SummonFunction(EntityType.BLAZE))
        .add("summon_enderman", new SummonFunction(EntityType.ENDERMAN))
        .add("summon_zombie", new SummonFunction(EntityType.ZOMBIE))
        .add("summon_silverfish", new SummonFunction(EntityType.SILVERFISH))
        .add("summon_skeleton", new SummonFunction(EntityType.SKELETON))
        .add("summon_spider", new SummonFunction(EntityType.SPIDER))
        .add("summon_cave_spider", new SummonFunction(EntityType.CAVE_SPIDER))
        .add("summon_slime", new SummonFunction(EntityType.SLIME))
        .add("summon_angry_bee", new SummonFunction(EntityType.BEE, (player, nbt) -> {
            nbt.putInt("Anger", 400 + player.getRNG().nextInt(400));
            nbt.putString("HurtBy", player.getUniqueID().toString());
        }))
        .add("summon_horse", new SummonFunction(EntityType.HORSE))
        .add("summon_chicken", new SummonFunction(EntityType.CHICKEN))
        .add("summon_cow", new SummonFunction(EntityType.COW))
        .add("summon_pig", new SummonFunction(EntityType.PIG))
        .add("summon_sheep", new SummonFunction(EntityType.SHEEP))
        .add("summon_villager", new SummonFunction(EntityType.VILLAGER))
        // Gives
        .add("give_wood", new GiveFunction(Items.OAK_LOG))
        .add("give_iron", new GiveFunction(Items.IRON_INGOT))
        .add("give_diamond", new GiveFunction(Items.DIAMOND))
        .add("give_ender_pearl", new GiveFunction(Items.ENDER_PEARL))
        .add("give_iron_pickaxe", new GiveFunction(Items.IRON_PICKAXE))
        .add("give_iron_sword", new GiveFunction(Items.IRON_SWORD))
        .add("give_iron_axe", new GiveFunction(Items.IRON_AXE))
        .add("give_iron_shovel", new GiveFunction(Items.IRON_SHOVEL))
        .add("give_iron_hoe", new GiveFunction(Items.IRON_HOE))
        .add("give_diamond_pickaxe", new GiveFunction(Items.DIAMOND_PICKAXE))
        .add("give_diamond_sword", new GiveFunction(Items.DIAMOND_SWORD))
        .add("give_diamond_axe", new GiveFunction(Items.DIAMOND_AXE))
        .add("give_diamond_shovel", new GiveFunction(Items.DIAMOND_SHOVEL))
        .add("give_diamond_hoe", new GiveFunction(Items.DIAMOND_HOE))
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
            MinecraftForge.EVENT_BUS.<ClientChatReceivedEvent>addListener(e -> {
                if (e.getType() == ChatType.SYSTEM) {
                    final ITextComponent message = e.getMessage();
                    if (this.test(message, c -> c instanceof TranslationTextComponent && "argument.entity.notfound.player".equals(((TranslationTextComponent) c).getKey()))) {
                        e.setCanceled(true);
                    }
                }
            });
        });
    }

    public boolean test(final ITextComponent message, final Predicate<ITextComponent> predicate) {
        if (predicate.test(message)) {
            return true;
        }
        if (!message.getUnformattedComponentText().isEmpty()) {
            return false;
        }
        for (final ITextComponent sibling : message.getSiblings()) {
            if (predicate.test(sibling)) {
                return true;
            }
            if (!sibling.getUnformattedComponentText().isEmpty()) {
                return false;
            }
        }
        return false;
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
