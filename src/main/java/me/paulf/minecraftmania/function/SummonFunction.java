package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public final class SummonFunction implements CommandFunction {
    private final EntityType<?> type;

    private final BiConsumer<PlayerEntity, CompoundNBT> nbtSupplier;

    public SummonFunction(final EntityType<?> type) {
        this(type, (player, nbt) -> {});
    }

    public SummonFunction(final EntityType<?> type, final BiConsumer<PlayerEntity, CompoundNBT> nbtSupplier) {
        this.type = type;
        this.nbtSupplier = nbtSupplier;
    }

    @Override
    public void run(final MinecraftMania.CommandContext context) {
        final AxisAlignedBB spawn = this.optimalSpawn(context.world(), context.player());
        final CompoundNBT nbt = new CompoundNBT();
        this.nbtSupplier.accept(context.player(), nbt);
        final Vec3d center = spawn.getCenter();
        context.commands().summon(this.type, new Vec3d(center.x, spawn.minY, center.z), nbt);
        context.commands().particle(ParticleTypes.LARGE_SMOKE, center, new Vec3d(spawn.getXSize(), spawn.getYSize(), spawn.getZSize()).scale(0.5D), 0.0D, context.world().rand.nextInt(7) + 16);
        context.commands().playsound(SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, center, 0.2F, 1.0F);
    }

    private AxisAlignedBB size() {
        final float w = this.type.getWidth() / 2.0F;
        return new AxisAlignedBB(-w, 0.0D, -w, w, this.type.getHeight(), w);
    }

    private AxisAlignedBB optimalSpawn(final World world, final PlayerEntity player) {
        final Vec3d origin = player.getPositionVec();
        final float diameter = this.type.getWidth() * MathHelper.SQRT_2;
        final float distance = diameter * 0.5F + 1.5F;
        final int count = (int) (2.0F * (float) Math.PI * distance / diameter);
        for (int i = 0; i < count; i++) {
            //noinspection IntegerDivisionInFloatingPointContext
            final float angle = (i > 0 ? i % 2 == 0 ? count - i / 2 : 1 + i / 2 : 0) * 360.0F / count;
            final Vec3d vector = Vec3d.fromPitchYaw(0.0F, player.rotationYaw + angle);
            final Vec3d pos = origin.add(vector.scale(distance));
            final AxisAlignedBB floored = this.fall(world, pos);
            if (this.good(world, player, floored)) {
                return floored;
            }
        }
        for (final Direction facing : nearest(player.rotationYaw)) {
            final AxisAlignedBB floored = this.fall(world, new Vec3d(
                MathHelper.floor(origin.x) + 0.5D + facing.getXOffset() * distance,
                origin.y,
                MathHelper.floor(origin.z) + 0.5D + facing.getZOffset() * distance
            ));
            if (this.good(world, player, floored)) {
                return floored;
            }
        }
        return this.size().offset(origin);
    }

    private boolean good(final World world, final PlayerEntity player, final AxisAlignedBB bounds) {
        return world.hasNoCollisions(bounds) &&
            world.rayTraceBlocks(new RayTraceContext(
                new Vec3d(player.getPosX(), player.getPosYEye(), player.getPosZ()), bounds.getCenter(), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player
            )).getType() == RayTraceResult.Type.MISS;
    }

    private AxisAlignedBB fall(final World world, final Vec3d pos) {
        final double up = 1.0D;
        final double down = 1.0D;
        final AxisAlignedBB target = this.size().offset(pos).offset(0.0D, up, 0.0D);
        return target.offset(0.0D, VoxelShapes.getAllowedOffset(Direction.Axis.Y, target, world.getCollisionShapes(null, target.expand(0.0D, -(up + down), 0.0D)), -(up + down)), 0.0D);
    }

    public static Direction[] nearest(final float yaw) {
        final float theta = -(float) Math.toRadians(yaw);
        final float x = MathHelper.sin(theta);
        final float z = MathHelper.cos(theta);
        final Direction dx = x > 0.0F ? Direction.EAST : Direction.WEST;
        final Direction dz = z > 0.0F ? Direction.SOUTH : Direction.NORTH;
        final Direction first, second;
        if (Math.abs(x) > Math.abs(z)) {
            first = dx;
            second = dz;
        } else {
            first = dz;
            second = dx;
        }
        return new Direction[] { first, second, second.getOpposite(), first.getOpposite() };
    }

    public static boolean isOperable(final MinecraftMania.Context context) {
        return context.commands().hasSummon();
    }
}
