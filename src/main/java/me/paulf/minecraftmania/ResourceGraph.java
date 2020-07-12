package me.paulf.minecraftmania;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BambooFeature;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.BlockBlobConfig;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.BlockStateProvidingFeatureConfig;
import net.minecraft.world.gen.feature.BlockWithContextConfig;
import net.minecraft.world.gen.feature.ChorusPlantFeature;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredRandomFeatureList;
import net.minecraft.world.gen.feature.CoralFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.KelpFeature;
import net.minecraft.world.gen.feature.LiquidsConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.MultipleWithChanceRandomFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.ReplaceBlockConfig;
import net.minecraft.world.gen.feature.SeaGrassFeature;
import net.minecraft.world.gen.feature.SeaPickleFeature;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.FrozenOceanSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.GiantTreeTaigaSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.GravellyMountainSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.MountainSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.NetherSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ShatteredSavannaSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SwampSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.WoodedBadlandsSurfaceBuilder;
import net.minecraft.world.gen.treedecorator.AlterGroundTreeDecorator;
import net.minecraft.world.gen.treedecorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.treedecorator.CocoaTreeDecorator;
import net.minecraft.world.gen.treedecorator.LeaveVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ResourceGraph {
    public void build(final ServerWorld world) {
        final SetMultimap<Biome, BlockState> naturalResources = HashMultimap.create();
        for (final Biome biome : ForgeRegistries.BIOMES) {
            final Consumer<BlockState> consumer = state -> naturalResources.put(biome, state);
            if (biome.getCategory() == Biome.Category.NETHER) {
                consumer.accept(Blocks.NETHERRACK.getDefaultState());
                consumer.accept(Blocks.LAVA.getDefaultState());
            } else if (biome.getCategory() == Biome.Category.THEEND) {
                consumer.accept(Blocks.END_STONE.getDefaultState());
            } else {
                consumer.accept(Blocks.STONE.getDefaultState());
                consumer.accept(Blocks.WATER.getDefaultState());
            }
            final ConfiguredSurfaceBuilder<?> configuredSurface = biome.getSurfaceBuilder();
            final SurfaceBuilder<?> surfaceBuilder = configuredSurface.builder;
            if (surfaceBuilder instanceof DefaultSurfaceBuilder || surfaceBuilder instanceof SwampSurfaceBuilder) {
                this.addSurfaceConfig(consumer, configuredSurface.config);
                consumer.accept(Blocks.ICE.getDefaultState());
                if (configuredSurface.config.getUnder().getBlock() == Blocks.SAND) {
                    consumer.accept(Blocks.SANDSTONE.getDefaultState());
                }
            } else if (surfaceBuilder instanceof MountainSurfaceBuilder) {
                this.addSurfaceConfig(consumer, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
            } else if (surfaceBuilder instanceof ShatteredSavannaSurfaceBuilder) {
                this.addSurfaceConfig(consumer, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.CORASE_DIRT_DIRT_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
            } else if (surfaceBuilder instanceof GravellyMountainSurfaceBuilder) {
                this.addSurfaceConfig(consumer, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.GRAVEL_CONFIG);
            } else if (surfaceBuilder instanceof GiantTreeTaigaSurfaceBuilder) {
                this.addSurfaceConfig(consumer, SurfaceBuilder.CORASE_DIRT_DIRT_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.PODZOL_DIRT_GRAVEL_CONFIG);
                this.addSurfaceConfig(consumer, SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
            } else if (surfaceBuilder instanceof BadlandsSurfaceBuilder) {
                consumer.accept(Blocks.WHITE_TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.ORANGE_TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.YELLOW_TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.BROWN_TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.RED_TERRACOTTA.getDefaultState());
                consumer.accept(Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState());
                if (surfaceBuilder instanceof WoodedBadlandsSurfaceBuilder) {
                    consumer.accept(Blocks.COARSE_DIRT.getDefaultState());
                    consumer.accept(Blocks.GRASS_BLOCK.getDefaultState());
                }
            } else if (surfaceBuilder instanceof FrozenOceanSurfaceBuilder) {
                consumer.accept(Blocks.PACKED_ICE.getDefaultState());
                consumer.accept(Blocks.SNOW_BLOCK.getDefaultState());
                consumer.accept(Blocks.AIR.getDefaultState());
                consumer.accept(Blocks.GRAVEL.getDefaultState());
                consumer.accept(Blocks.ICE.getDefaultState());
            } else if (surfaceBuilder instanceof NetherSurfaceBuilder) {
                consumer.accept(Blocks.NETHERRACK.getDefaultState());
                consumer.accept(Blocks.GRAVEL.getDefaultState());
                consumer.accept(Blocks.SOUL_SAND.getDefaultState());
            }
            // UNDERGROUND_STRUCTURES, SURFACE_STRUCTURES
            for (final GenerationStage.Decoration decor : new GenerationStage.Decoration[] {
                GenerationStage.Decoration.RAW_GENERATION,
                GenerationStage.Decoration.LOCAL_MODIFICATIONS,
                GenerationStage.Decoration.UNDERGROUND_ORES,
                GenerationStage.Decoration.UNDERGROUND_DECORATION,
                GenerationStage.Decoration.VEGETAL_DECORATION,
                GenerationStage.Decoration.TOP_LAYER_MODIFICATION
            }) {
                for (final ConfiguredFeature<?, ?> feature : biome.getFeatures(decor)) {
                    this.addFeature(consumer, feature);
                }
            }
        }
        final SetMultimap<Biome.Category, BlockState> categoryNaturalResources = HashMultimap.create();
        final SetMultimap<Biome, BlockState> biomeNaturalResources = HashMultimap.create();
        for (final Map.Entry<Biome, Collection<BlockState>> first : naturalResources.asMap().entrySet()) {
            for (final BlockState candidate : first.getValue()) {
                boolean categoryunique = true;
                boolean biomeunique = true;
                for (final Map.Entry<Biome, Collection<BlockState>> second : naturalResources.asMap().entrySet()) {
                    if (second.getKey().getCategory() != first.getKey().getCategory() && second.getValue().contains(candidate)) {
                        categoryunique = false;
                    }
                    if (second.getKey() != first.getKey() && second.getValue().contains(candidate)) {
                        biomeunique = false;
                        if (!categoryunique) break;
                    }
                }
                if (biomeunique) {
                    biomeNaturalResources.put(first.getKey(), candidate);
                } else if (categoryunique) {
                    categoryNaturalResources.put(first.getKey().getCategory(), candidate);
                }
            }
        }
        final Set<BlockState> commonNaturalResources = new HashSet<>(naturalResources.values());
        commonNaturalResources.removeAll(categoryNaturalResources.values());
        final StringBuilder bob = new StringBuilder();
        final Consumer<BlockState> appendBlock = state -> {
            if (state.isAir()) return;
            bob.append("  ").append(state.getBlock().getRegistryName()).append("\n");
            if (state.getBlock().getLootTable() != LootTables.EMPTY) {
                final ObjectSet<ItemStack> drops = new ObjectOpenCustomHashSet<>(ItemEquality.INSTANCE);
                final ResourceLocation table = state.getBlock().getLootTable();
                final LootContext context = new LootContext.Builder(world)
                    .withRandom(new Random(42L))
                    .withParameter(LootParameters.POSITION, BlockPos.ZERO)
                    .withParameter(LootParameters.TOOL, new ItemStack(Items.DIAMOND_PICKAXE))
                    .withNullableParameter(LootParameters.BLOCK_ENTITY, null)
                    .withParameter(LootParameters.BLOCK_STATE, state)
                    .build(LootParameterSets.BLOCK);
                final LootTable loottable = world.getServer().getLootTableManager().getLootTableFromLocation(table);
                for (int n = 0; n < 1024; n++) {
                    drops.addAll(loottable.generate(context));
                }
                for (final ItemStack stack : drops) {
                    if (!stack.isEmpty() && stack.getItem() != state.getBlock().asItem()) {
                        bob.append("    ").append(stack.getItem().getRegistryName()).append("\n");
                    }
                }
            }
        };
        for (final Map.Entry<Biome, Collection<BlockState>> e : biomeNaturalResources.asMap().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getCategory())).collect(Collectors.toList())) {
            bob.append(e.getKey().getDisplayName().getString()).append(":\n");
            //noinspection ConstantConditions
            e.getValue().stream().distinct().sorted(Comparator.comparing(s -> s.getBlock().getRegistryName())).forEach(appendBlock);
        }
        bob.append("\n");
        final Map<Biome.Category, String> categoryNames = StreamSupport.stream(ForgeRegistries.BIOMES.spliterator(), false)
            .collect(Multimaps.toMultimap(Biome::getCategory, Function.identity(), HashMultimap::create)).asMap().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().map(b -> b.getDisplayName().getString()).min(Comparator.comparing(String::length)).orElseThrow(IllegalStateException::new)
            ));
        for (final Map.Entry<Biome.Category, Collection<BlockState>> e : categoryNaturalResources.asMap().entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
            bob.append(categoryNames.get(e.getKey())).append(":\n");
            //noinspection ConstantConditions
            e.getValue().stream().distinct().sorted(Comparator.comparing(s -> s.getBlock().getRegistryName())).forEach(appendBlock);
        }
        bob.append("\n");
        bob.append("Common:\n");
        commonNaturalResources.forEach(appendBlock);
        System.out.printf("%s", bob);
        /*final StringBuilder bob = new StringBuilder();
        for (final Item item : ForgeRegistries.ITEMS) {
            bob.append(item.getRegistryName()).append("  >>  ").append(new AnagramPuzzleScreen.ItemHintFactory().create(minecraft.world, item).stream().map(ITextComponent::getString).collect(Collectors.joining(", "))).append('\n');
        }
        System.out.printf("%s", bob);*/
    }


    private void addSurfaceConfig(final Consumer<BlockState> consumer, final ISurfaceBuilderConfig surface) {
        consumer.accept(surface.getTop());
        consumer.accept(surface.getUnder());
        if (surface instanceof SurfaceBuilderConfig) {
            consumer.accept(((SurfaceBuilderConfig) surface).getUnderWaterMaterial());
        }
    }

    private void addFeature(final Consumer<BlockState> consumer, final ConfiguredFeature<?, ?> feature) {
        if (feature.feature instanceof Structure) return;
        final IFeatureConfig cfg = feature.config;
        if (cfg instanceof BaseTreeFeatureConfig) {
            this.getBlockStates(((BaseTreeFeatureConfig) cfg).trunkProvider, consumer);
            this.getBlockStates(((BaseTreeFeatureConfig) cfg).leavesProvider, consumer);
            for (final TreeDecorator decorator : ((BaseTreeFeatureConfig) cfg).decorators) {
                if (decorator instanceof AlterGroundTreeDecorator) {
                    this.getBlockStates(ObfuscationReflectionHelper.getPrivateValue(AlterGroundTreeDecorator.class, (AlterGroundTreeDecorator) decorator, "field_227410_b_"), consumer);
                } else if (decorator instanceof BeehiveTreeDecorator) {
                    consumer.accept(Blocks.BEE_NEST.getDefaultState());
                } else if (decorator instanceof CocoaTreeDecorator) {
                    consumer.accept(Blocks.COCOA.getDefaultState());
                } else if (decorator instanceof LeaveVineTreeDecorator || decorator instanceof TrunkVineTreeDecorator) {
                    consumer.accept(Blocks.VINE.getDefaultState());
                }
            }
        } else if (cfg instanceof BigMushroomFeatureConfig) {
            this.getBlockStates(((BigMushroomFeatureConfig) cfg).field_227272_a_, consumer);
            this.getBlockStates(((BigMushroomFeatureConfig) cfg).field_227273_b_, consumer);
        } else if (cfg instanceof BlockBlobConfig) {
            consumer.accept(((BlockBlobConfig) cfg).state);
        } else if (cfg instanceof BlockClusterFeatureConfig) {
            this.getBlockStates(((BlockClusterFeatureConfig) cfg).stateProvider, consumer);
        } else if (cfg instanceof BlockStateFeatureConfig) {
            consumer.accept(((BlockStateFeatureConfig) cfg).state);
        } else if (cfg instanceof BlockStateProvidingFeatureConfig) {
            this.getBlockStates(((BlockStateProvidingFeatureConfig) cfg).field_227268_a_, consumer);
        } else if (cfg instanceof BlockWithContextConfig) {
            consumer.accept(((BlockWithContextConfig) cfg).toPlace);
        } else if (cfg instanceof DecoratedFeatureConfig) {
            this.addFeature(consumer, ((DecoratedFeatureConfig) cfg).feature);
        } else if (cfg instanceof LiquidsConfig) {
            consumer.accept(((LiquidsConfig) cfg).state.getBlockState());
        } else if (cfg instanceof MultipleRandomFeatureConfig) {
            this.addFeature(consumer, ((MultipleRandomFeatureConfig) cfg).defaultFeature);
            for (final ConfiguredRandomFeatureList<?> f : ((MultipleRandomFeatureConfig) cfg).features) {
                this.addFeature(consumer, f.feature);
            }
        } else if (cfg instanceof MultipleWithChanceRandomFeatureConfig) {
            for (final ConfiguredFeature<?, ?> f : ((MultipleWithChanceRandomFeatureConfig) cfg).features) {
                this.addFeature(consumer, f);
            }
        } else if (cfg instanceof OreFeatureConfig) {
            consumer.accept(((OreFeatureConfig) cfg).state);
        } else if (cfg instanceof ReplaceBlockConfig) {
            consumer.accept(((ReplaceBlockConfig) cfg).state);
        } else if (cfg instanceof SingleRandomFeature) {
            for (final ConfiguredFeature<?, ?> f : ((SingleRandomFeature) cfg).features) {
                this.addFeature(consumer, f);
            }
        } else if (cfg instanceof SphereReplaceConfig) {
            consumer.accept(((SphereReplaceConfig) cfg).state);
        } else if (cfg instanceof TwoFeatureChoiceConfig) {
            this.addFeature(consumer, ((TwoFeatureChoiceConfig) cfg).field_227285_a_);
            this.addFeature(consumer, ((TwoFeatureChoiceConfig) cfg).field_227286_b_);
        }
        final Feature<?> feat = feature.feature;
        if (feat instanceof ChorusPlantFeature) {
            consumer.accept(Blocks.CHORUS_PLANT.getDefaultState());
            consumer.accept(Blocks.CHORUS_FLOWER.getDefaultState());
        } else if (feat instanceof SeaGrassFeature) {
            consumer.accept(Blocks.TALL_SEAGRASS.getDefaultState());
            consumer.accept(Blocks.SEAGRASS.getDefaultState());
        } else if (feat instanceof KelpFeature) {
            consumer.accept(Blocks.KELP.getDefaultState());
            consumer.accept(Blocks.KELP_PLANT.getDefaultState());
        } else if (feat instanceof CoralFeature) {
            BlockTags.CORALS.getAllElements().stream().map(Block::getDefaultState).forEach(consumer);
            BlockTags.WALL_CORALS.getAllElements().stream().map(Block::getDefaultState).forEach(consumer);
            consumer.accept(Blocks.SEA_PICKLE.getDefaultState());
        } else if (feat instanceof SeaPickleFeature) {
            consumer.accept(Blocks.SEA_PICKLE.getDefaultState());
        } else if (feat instanceof BambooFeature) {
            consumer.accept(Blocks.BAMBOO.getDefaultState());
        }
    }

    private void getBlockStates(final BlockStateProvider provider, final Consumer<BlockState> consumer) {
        if (provider instanceof SimpleBlockStateProvider) {
            consumer.accept(provider.getBlockState(new Random(0L), new BlockPos(0, 64, 0)));
        }
        final Set<BlockState> blocks = new HashSet<>();
        for (long seed = 0L; seed < 4L; seed++) {
            final Random rng = new Random(seed);
            for (int n = 0; n < 1024; n++) {
                final BlockState state = provider.getBlockState(rng, new BlockPos(rng.nextInt(2048), 64, rng.nextInt(2048)));
                if (blocks.add(state)) consumer.accept(state);
            }
        }
    }

    private static class ItemEquality implements Hash.Strategy<ItemStack> {
        static final ItemEquality INSTANCE = new ItemEquality();

        @Override
        public int hashCode(final ItemStack o) {
            if (o == null) return 0;
            final CompoundNBT tag = o.getTag();
            return o.getItem().hashCode() * 31 + (tag != null ? tag.hashCode() : 0);
        }

        @Override
        public boolean equals(final ItemStack a, final ItemStack b) {
            return a != null && b != null ? ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b) : (a == null) == (b == null);
        }
    }
}
