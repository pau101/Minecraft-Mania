package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.paulf.minecraftmania.function.ChangeLanguageFunction;
import me.paulf.minecraftmania.function.CommandFunction;
import me.paulf.minecraftmania.function.DayTimeFunction;
import me.paulf.minecraftmania.function.DeathAnimationFunction;
import me.paulf.minecraftmania.function.DisableKeyFunction;
import me.paulf.minecraftmania.function.EffectFunction;
import me.paulf.minecraftmania.function.GiveFunction;
import me.paulf.minecraftmania.function.KillFunction;
import me.paulf.minecraftmania.function.NightTimeFunction;
import me.paulf.minecraftmania.function.PostProcessingFunction;
import me.paulf.minecraftmania.function.PressKeyFunction;
import me.paulf.minecraftmania.function.RandomSoundPicker;
import me.paulf.minecraftmania.function.RandomWorldEventFunction;
import me.paulf.minecraftmania.function.SoundFunction;
import me.paulf.minecraftmania.function.SummonFunction;
import me.paulf.minecraftmania.function.SwapKeyFunction;
import me.paulf.minecraftmania.function.TouchyFunction;
import me.paulf.minecraftmania.function.VibratoFunction;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Mod(MinecraftMania.ID)
public final class MinecraftMania {
    public static final String ID = "minecraftmania";

    private final GameSettings settings = Minecraft.getInstance().gameSettings;

    private final CommandSet effectMap = new CommandSet.Builder(EffectFunction::isOperable)
        .add("effect_damage", new EffectFunction(Effects.INSTANT_DAMAGE, 1, 0))
        .add("effect_health", new EffectFunction(Effects.INSTANT_HEALTH, 1, 0))
        .add("effect_saturation", new EffectFunction(Effects.SATURATION, 4, 0))
        .add("effect_blindness", new EffectFunction(Effects.BLINDNESS, 20, 0))
        .add("effect_speed", new EffectFunction(Effects.SPEED, 60, 9))
        .add("effect_slowness", new EffectFunction(Effects.SLOWNESS, 30, 2))
        .add("effect_jumping", new EffectFunction(Effects.JUMP_BOOST, 60, 9))
        .build();

    private final CommandSet summonMap = new CommandSet.Builder(SummonFunction::isOperable)
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
        .add("summon_tropical_fish", new SummonFunction(EntityType.TROPICAL_FISH))
        .build();

    private final CommandSet giveMap = new CommandSet.Builder(GiveFunction::isOperable)
        .add("give_bread", new GiveFunction(Items.BREAD))
        .add("give_cookie", new GiveFunction(Items.COOKIE))
        .add("give_cake", new GiveFunction(Items.CAKE))
        .add("give_cooked_beef", new GiveFunction(Items.COOKED_BEEF))
        .add("give_baked_potato", new GiveFunction(Items.BAKED_POTATO))
        .add("give_golden_apple", new GiveFunction(Items.GOLDEN_APPLE))
        .add("give_enchanted_golden_apple", new GiveFunction(Items.ENCHANTED_GOLDEN_APPLE))
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

    private final CommandSet killMap = new CommandSet.Builder(KillFunction::isOperable)
        .add("kill", new KillFunction())
        .build();

    private final CommandSet timeMap = new CommandSet.Builder(ctx -> ctx.commands().hasTime())
        .add("time_day", new DayTimeFunction())
        .add("time_night", new NightTimeFunction())
        .build();

    // Client
    private final CommandSet keyMap = new CommandSet.Builder()
        .add("disable_forward", new DisableKeyFunction(this.settings.keyBindForward, Duration.ofMinutes(2)))
        .add("disable_back", new DisableKeyFunction(this.settings.keyBindBack, Duration.ofMinutes(2)))
        .add("disable_sneak", new DisableKeyFunction(this.settings.keyBindSneak, Duration.ofMinutes(2)))
        .add("disable_jump", new DisableKeyFunction(this.settings.keyBindJump, Duration.ofMinutes(2)))
        .add("disable_inventory", new DisableKeyFunction(this.settings.keyBindInventory, Duration.ofMinutes(2)))
        .add("swap_forward_back", new SwapKeyFunction(this.settings.keyBindForward, this.settings.keyBindBack, Duration.ofMinutes(2)))
        .add("swap_left_right", new SwapKeyFunction(this.settings.keyBindLeft, this.settings.keyBindRight, Duration.ofMinutes(2)))
        .add("swap_jump_sneak", new SwapKeyFunction(this.settings.keyBindJump, this.settings.keyBindSneak, Duration.ofMinutes(2)))
        .add("swap_use_attack", new SwapKeyFunction(this.settings.keyBindUseItem, this.settings.keyBindAttack, Duration.ofMinutes(2)))
        .add("press_forward", new PressKeyFunction(this.settings.keyBindForward, Duration.ofMinutes(2)))
        .add("press_jump", new PressKeyFunction(this.settings.keyBindJump, Duration.ofMinutes(2)))
        .build();

    private final CommandSet langMap = new CommandSet.Builder()
        .add("lang_pirate", new ChangeLanguageFunction("en_pt", Duration.ofMinutes(2)))
        .add("lang_shakespearean", new ChangeLanguageFunction("enws", Duration.ofMinutes(2)))
        .add("lang_lolcat", new ChangeLanguageFunction("lol_us", Duration.ofMinutes(2)))
        .build();

    private final CommandSet soundMap = new CommandSet.Builder()
        .add("oink", new SoundFunction(Duration.ofMinutes(2), () -> rl -> Optional.of(SoundEvents.ENTITY_PIG_AMBIENT)))
        .add("ruckus", new SoundFunction(Duration.ofMinutes(2), () -> new RandomSoundPicker(new Random().nextLong())))
        .add("vibrato", new VibratoFunction(Duration.ofMinutes(2)))
        .build();

    private final CommandSet visualMap = new CommandSet.Builder()
        .add("jpeg", new PostProcessingFunction(new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json"), Duration.ofMinutes(2)))
        .add("rgb", new PostProcessingFunction(new ResourceLocation(MinecraftMania.ID, "shaders/post/rgb.json"), Duration.ofMinutes(2)))
        .add("desaturate", new PostProcessingFunction(new ResourceLocation("shaders/post/desaturate.json"), Duration.ofMinutes(2)))
        .build();

    private final CommandSet miscMap = new CommandSet.Builder()
        .add("animate_death", new DeathAnimationFunction(Duration.ofMinutes(2)))
        .add("random_world_events", new RandomWorldEventFunction(Duration.ofMinutes(2)))
        .add("touchy", new TouchyFunction(Duration.ofMinutes(2)))
        .build();

    private final CommandSet challengeMap = new CommandSet.Builder()
        .add("sliding_puzzle", context -> Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().displayGuiScreen(new SlidingPuzzleScreen(Minecraft.getInstance().currentScreen))))
        .add("jigsaw_puzzle", context -> Minecraft.getInstance().enqueue(() -> Minecraft.getInstance().displayGuiScreen(new JigsawPuzzleScreen(Minecraft.getInstance().currentScreen))))
        .build();

    private final CommandSet set = new CommandSet.Builder()
        .add(new CommandSet.Builder()
            .add(this.keyMap)
            .add(this.langMap)
            .add(this.soundMap)
            .add(this.visualMap)
            .add(this.miscMap)
            .add(this.challengeMap)
            .build())
        .add(this.effectMap)
        .add(this.summonMap)
        .add(this.giveMap)
        .add(this.killMap)
        .add(this.timeMap)
        .build();

    private final StickyMessageHelper sticky = new StickyMessageHelper();

    private State state = new OutOfGameState();

    public MinecraftMania() {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            LiveEdit.instance().init();
            final IEventBus bus = MinecraftForge.EVENT_BUS;
            //Minecraft.getInstance().enqueue(() -> bus.register(new ShaderPostProcessing()));
            this.sticky.register(bus);
            bus.<GuiOpenEvent>addListener(e -> {
                final Screen current = Minecraft.getInstance().currentScreen;
                if (current instanceof TilePuzzleScreen<?> && !((TilePuzzleScreen<?>) current).isSolved() && !(e.getGui() instanceof TilePuzzleScreen<?>)) {
                    e.setCanceled(true);
                }
            });
            //bus.<ClientPlayerNetworkEvent.LoggedInEvent>addListener(e -> this.join(e.getPlayer()));
            //bus.<ClientPlayerNetworkEvent.RespawnEvent>addListener(e -> this.join(e.getPlayer()));
            bus.<ClientPlayerNetworkEvent.LoggedOutEvent>addListener(e -> this.leave());
            new CommandsListener(this::join).register(bus);
            new ClientCommandProvider.Builder()
                .add(this::mania)
                .build()
                .register(bus);
            bus.<ClientChatReceivedEvent>addListener(e -> {
                if (e.getType() == ChatType.SYSTEM) {
                    final ITextComponent message = e.getMessage();
                    if (TextComponents.matches(message, TextComponents.translation("argument.entity.notfound.player"))) {
                        e.setCanceled(true);
                    }
                }
            });
        });
    }

    private void moveState(final State state) {
        this.state.stop();
        this.state = state;
        this.state.start();
    }

    private <S> LiteralArgumentBuilder<S> mania(final ClientCommandProvider.Helper<S> helper) {
        return LiteralArgumentBuilder.<S>literal("mania")
            .then(helper.executes(
                RequiredArgumentBuilder.<S, String>argument("command", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                        for (final String command : this.state.commands()) {
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
        this.moveState(new OutOfGameState());
    }

    private void join(final ClientPlayerEntity player, final CommandDispatcher<ISuggestionProvider> dispatcher) {
        final CommandSender sender = new OperatorCommandSender(dispatcher, player::sendChatMessage);
        final CommandMap.Builder builder = new CommandMap.Builder();
        this.set.build(new Context(sender, player.world, player), builder);
        this.moveState(new InGameState(player, builder.build(), sender));
    }

    abstract static class State {
        abstract ImmutableSet<String> commands();

        abstract void accept(final ViewerCommand command);

        void start() {
        }

        void stop() {
        }
    }

    static class OutOfGameState extends State {
        @Override
        ImmutableSet<String> commands() {
            return ImmutableSet.of();
        }

        @Override
        void accept(final ViewerCommand command) {
        }
    }

    public static class Context {
        final CommandSender commands;
        final World world;
        final PlayerEntity player;

        public Context(final CommandSender commands, final World world, final PlayerEntity player) {
            this.commands = commands;
            this.world = world;
            this.player = player;
        }

        public CommandSender commands() {
            return this.commands;
        }

        public World world() {
            return this.world;
        }

        public PlayerEntity player() {
            return this.player;
        }
    }

    public static final class CommandContext extends Context {
        final InGameState state;
        final ViewerCommand command;

        private CommandContext(final InGameState state, final CommandSender commands, final World world, final PlayerEntity player, final ViewerCommand command) {
            super(commands, world, player);
            this.state = state;
            this.command = command;
        }

        public String getCommand() {
            return this.command.getCommand();
        }

        public ITextComponent getViewerName() {
            return new StringTextComponent(this.command.getViewer()).applyTextStyle(TextFormatting.GOLD);
        }

        public <E extends Event> void addRunningFunction(final Duration duration, final RunningFunction function) {
            this.state.addRunningFunction(this.command, Ints.checkedCast(duration.getSeconds()) * 20, function);
        }
    }

    class InGameState extends State {
        final ClientPlayerEntity user;

        final CommandMap commands;

        final CommandSender sender;

        final Map<String, RunningCommandFunction> functions;

        InGameState(final ClientPlayerEntity user, final CommandMap commands, final CommandSender sender) {
            this.user = user;
            this.commands = commands;
            this.sender = sender;
            this.functions = new LinkedHashMap<>();
        }

        @Override
        ImmutableSet<String> commands() {
            return this.commands.keys();
        }

        void addRunningFunction(final ViewerCommand command, final int ticks, final RunningFunction function) {
            this.functions.compute(command.getCommand(), (k, v) -> {
                if (v == null) {
                    final RunningCommandFunction func = new RunningCommandFunction(command, function, ticks);
                    func.start();
                    return func;
                } else {
                    v.function.merge();
                    v.duration += ticks;
                    return v;
                }
            });
        }

        @Override
        void accept(final ViewerCommand command) {
            final CommandContext context = new CommandContext(this, this.sender, this.user.world, this.user, command);
            final CommandFunction function = this.commands.get(command);
            context.commands().tell(function.getMessage(context));
            function.run(context);
        }

        @SubscribeEvent
        public void tick(final TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isGamePaused()) {
                final Iterator<RunningCommandFunction> it = this.functions.values().iterator();
                while (it.hasNext()) {
                    final RunningCommandFunction func = it.next();
                    if (func.tick()) {
                        func.stop();
                        it.remove();
                    }
                }
            }
        }

        @Override
        void start() {
            super.start();
            MinecraftForge.EVENT_BUS.register(this);
            this.user.sendMessage(new TranslationTextComponent("mania.ready", this.commands.keys().size()).applyTextStyle(TextFormatting.GREEN));
        }

        @Override
        void stop() {
            super.stop();
            MinecraftForge.EVENT_BUS.unregister(this);
            this.functions.values().forEach(RunningCommandFunction::stop);
            this.functions.clear();
        }
    }

    class RunningCommandFunction {
        final ViewerCommand command;
        final RunningFunction function;
        int duration;
        int time;

        RunningCommandFunction(final ViewerCommand command, final RunningFunction function, final int duration) {
            this.command = command;
            this.function = function;
            this.duration = duration;
            this.time = 0;
        }

        String getName() {
            return this.command.getCommand();
        }

        boolean tick() {
            if (this.time++ < this.duration) {
                MinecraftMania.this.sticky.add(this.id(), this.getMessage());
                this.function.tick();
            }
            return this.time >= this.duration;
        }

        void start() {
            MinecraftForge.EVENT_BUS.register(this.function);
            this.function.start();
        }

        private int id() {
            // No zeros
            return 1 | this.command.getCommand().hashCode();
        }

        void stop() {
            this.function.stop();
            MinecraftForge.EVENT_BUS.unregister(this.function);
            MinecraftMania.this.sticky.remove(this.id());
        }

        public ITextComponent getMessage() {
            return this.function.getMessage(this.command, (this.duration - this.time + 19) / 20);
        }
    }

}
