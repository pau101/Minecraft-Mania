package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AnagramPuzzleScreen extends ChallengeScreen {
    private final WordBlacklist blacklist;

    private Anagram anagram = new Anagram("", "");

    public AnagramPuzzleScreen(@Nullable final Screen parent, final WordBlacklist blacklist) {
        super(parent, NarratorChatListener.EMPTY);
        this.blacklist = blacklist;
    }

    @Override
    public void init(final Minecraft minecraft, final int width, final int height) {
        super.init(minecraft, width, height);

        final List<String> strings = Stream.concat(
                StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false).filter(i -> i.getGroup() != null).map(Item::getTranslationKey),
                StreamSupport.stream(ForgeRegistries.ENTITIES.spliterator(), false).map(EntityType::getTranslationKey)
            )
            .map(s -> LanguageMap.getInstance().translateKey(s))
            // assume unlocalized translation key
            .filter(s -> s.indexOf('.') == -1)
            .map(AnagramPuzzleScreen::normalize)
            .distinct()
            .filter(s -> s.length() >= 4 && s.length() <= 10 && s.chars().noneMatch(chr -> Character.getType(chr) == Character.COMBINING_SPACING_MARK))
            .collect(Collectors.toList());
        this.anagram = this.generate(strings, new Random());

        final StringBuilder bob = new StringBuilder();
        for (final Item item : ForgeRegistries.ITEMS) {
            bob.append(item.getRegistryName()).append("  >>  ").append(new AnagramPuzzleScreen.ItemHintFactory().create(minecraft.world, item).stream().map(ITextComponent::getString).collect(Collectors.joining(", "))).append('\n');
        }
        System.out.printf("%s", bob);
    }

    private static String normalize(final String s) {
        // TODO: modifier and surrogate character handling
       return Normalizer.normalize(StringUtils.stripControlCodes(s), Normalizer.Form.NFC);
    }

    private Anagram generate(final List<String> strings, final Random rng) {
        int attempt = 0;
        String word;
        String anagram;
        do {
            if (++attempt > strings.size()) {
                return new Anagram("minecraft", "fircatmen");
            }
            word = strings.get(rng.nextInt(strings.size()));
            final char[] chars = word.toCharArray();
            ArrayUtils.shuffle(chars);
            anagram = new String(chars);
        } while (this.containsBlacklisted(anagram));
        return new Anagram(word, anagram);
    }

    interface HintFactory<T> {
        List<ITextComponent> create(final World world, final T object);
    }

    public static class ItemHintFactory implements HintFactory<Item> {
        @Override
        public List<ITextComponent> create(final World world, final Item object) {
            final ImmutableList.Builder<ITextComponent> bob = ImmutableList.builder();
            if (object instanceof BlockItem) {
                bob.add(new TranslationTextComponent("mania.anagram.hint.block"));
                this.addCrafting(world, object, bob);
                if (object.isFood()) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.food"));
                }
                final ItemGroup group = object.getGroup();
                final Block block = ((BlockItem) object).getBlock();
                final BlockState state = block.getDefaultState();
                float hardness = 0.0F;
                try {
                    hardness = state.getBlockHardness(EmptyBlockReader.INSTANCE, BlockPos.ZERO);
                } catch (final NullPointerException ignored) {
                    // assume bad absent block entity handling
                }
                final ItemStack stack = new ItemStack(object);
                if (hardness < 0.0F) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.unbreakable"));
                } else if (hardness == 0.0F) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.breaks_instantly"));
                } else {
                    final Optional<Item> tool = StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false).flatMap(item -> {
                        final ItemStack candidate = new ItemStack(item);
                        final float speed;
                        return (state.getMaterial().isToolNotRequired() || candidate.canHarvestBlock(state)) && (speed = candidate.getDestroySpeed(state)) > 1.0F ? Stream.of(Pair.of(item, speed)) : Stream.empty();
                    }).sorted(Comparator.comparing(Pair::getSecond)).map(Pair::getFirst).findFirst();
                    if (tool.isPresent()) {
                        bob.add(new TranslationTextComponent(state.getMaterial().isToolNotRequired() ? "mania.anagram.hint.broken_by_tool" : "mania.anagram.hint.requires_tool", new ItemStack(tool.get()).getDisplayName()));
                    } else {
                        bob.add(new TranslationTextComponent("mania.anagram.hint.any_tool"));
                    }
                }
                if (block.isIn(BlockTags.SLABS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.slab"));
                } else if (block.isIn(BlockTags.STAIRS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.stair"));
                } else if (block.isIn(BlockTags.FENCES)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.fence"));
                } else if (block.isIn(BlockTags.LOGS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.log"));
                } else if (block.isIn(BlockTags.DOORS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.door"));
                } else if (block.isIn(BlockTags.CORALS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.coral"));
                } else if (block.isIn(BlockTags.FLOWERS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.flower"));
                } else if (block.isIn(BlockTags.CROPS)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.crop"));
                } else if (block instanceof IPlantable) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.plant"));
                }
                if (Arrays.stream(Direction.values()).anyMatch(d -> state.getFlammability(EmptyBlockReader.INSTANCE, BlockPos.ZERO, d) > 0)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.flammable"));
                }
                if (state.getLightValue() > 0 || state.has(BlockStateProperties.LIT)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.luminant"));
                }
                final TileEntity entity = state.createTileEntity(EmptyBlockReader.INSTANCE);
                if (entity != null) {
                    entity.setWorldAndPos(world, BlockPos.ZERO);
                    if (entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).filter(handler -> handler.getSlots() > 0).isPresent()) {
                        bob.add(new TranslationTextComponent("mania.anagram.hint.container"));
                    }
                    entity.remove();
                }
            } else {
                bob.add(new TranslationTextComponent("mania.anagram.hint.item"));
                this.addCrafting(world, object, bob);
                final ItemGroup group = object.getGroup();
                if (object.isFood()) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.food"));
                } else if (object instanceof ToolItem) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.tool"));
                } else if (object instanceof ArmorItem || this.hasAttribute(object, SharedMonsterAttributes.ARMOR, EquipmentSlotType.FEET, EquipmentSlotType.LEGS, EquipmentSlotType.CHEST, EquipmentSlotType.HEAD)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.armor"));
                } else if (object instanceof SwordItem || object instanceof TridentItem || object instanceof ShootableItem || this.hasAttribute(object, SharedMonsterAttributes.ATTACK_DAMAGE, EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND)) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.weapon"));
                } else if (object.isDamageable()) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.damageable"));
                } else if (AbstractFurnaceTileEntity.isFuel(new ItemStack(object))) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.fuel"));
                } else if (Item.class.equals(object.getClass())) {
                    bob.add(new TranslationTextComponent("mania.anagram.hint.material"));
                }
            }
            return bob.build();
        }

        private void addCrafting(final World world, final Item object, final ImmutableList.Builder<ITextComponent> bob) {
            if (world.getRecipeManager().getRecipes().stream().anyMatch(r -> r.getType() == IRecipeType.SMELTING && r.getRecipeOutput().getItem() == object)) {
                bob.add(new TranslationTextComponent("mania.anagram.hint.smelted"));
            } else if (world.getRecipeManager().getRecipes().stream().anyMatch(r -> r.getType() == IRecipeType.CRAFTING && r.getRecipeOutput().getItem() == object)) {
                bob.add(new TranslationTextComponent("mania.anagram.hint.crafted"));
            } else {
                bob.add(new TranslationTextComponent("mania.anagram.hint.uncraftable"));
            }
        }

        private boolean hasAttribute(final Item item, final IAttribute attribute, final EquipmentSlotType... slots) {
            if (Item.class.equals(item.getClass())) return false;
            final ItemStack stack = new ItemStack(item);
            for (final EquipmentSlotType slot : slots) {
                for (final AttributeModifier modifier : item.getAttributeModifiers(slot, stack).get(attribute.getName())) {
                    switch (modifier.getOperation()) {
                        case ADDITION:
                            if (modifier.getAmount() > 0.0D) return true;
                            break;
                        case MULTIPLY_BASE:
                        case MULTIPLY_TOTAL:
                            if (modifier.getAmount() > 1.0D) return true;
                            break;
                    }
                }
            }
            return false;
        }
    }

    static class Anagram {
        final String word;
        final String anagram;

        Anagram(final String word, final String anagram) {
            this.word = word;
            this.anagram = anagram;
        }
    }

    private boolean containsBlacklisted(final String anagram) {
        for (final String s : this.blacklist.getWords()) {
            if (anagram.contains(s)) return true;
        }
        return false;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        this.renderParent(mouseX, mouseY, delta);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        this.renderBackground();
        super.render(mouseX, mouseY, delta);
    }
}
