package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.util.Random;

public class TouchyFunction extends DurationFunction {
    public TouchyFunction(final Duration duration) {
        super(duration);
    }

    @Override
    protected RunningFunction createFunction() {
        return new RunningFunction() {
            final Random rng = new Random();

            int cooldown;

            @Override
            public void tick() {
                if (this.cooldown > 0) {
                    this.cooldown--;
                    return;
                }
                if (!(this.rng.nextFloat() < 0.1F)) {
                    return;
                }
                final Minecraft mc = Minecraft.getInstance();
                final ClientWorld world = mc.world;
                final ClientPlayerEntity player = mc.player;
                final PlayerController controller = mc.playerController;
                if (world != null && player != null && controller != null) {
                    final Vec3d start = player.getEyePosition(1.0F);
                    final Vec3d look = Vec3d.fromPitchYaw(
                        player.rotationPitch + (this.rng.nextFloat() * 2.0F - 1.0F) * 30.0F,
                        player.rotationYawHead + (this.rng.nextFloat() * 2.0F - 1.0F) * 70.0F
                    );
                    final Vec3d end = start.add(look.scale(controller.getBlockReachDistance()));
                    final BlockRayTraceResult result = world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
                    if (result.getType() != RayTraceResult.Type.BLOCK) {
                        return;
                    }
                    final Hand hand = Hand.MAIN_HAND;
                    boolean success = false;
                    if (this.rng.nextFloat() < 0.5F) {
                        // Use Item
                        final ItemStack stack = player.getHeldItem(hand);
                        final int originalCount = stack.getCount();
                        final ActionResultType actionResult = controller.func_217292_a(player, world, hand, result);
                        if (actionResult.isSuccess()) {
                            player.swingArm(hand);
                            if (!stack.isEmpty() && (stack.getCount() != originalCount || controller.isInCreativeMode())) {
                                mc.gameRenderer.itemRenderer.resetEquippedProgress(hand);
                            }
                            success = true;
                        }
                    } else {
                        // Attack
                        final BlockPos blockpos = result.getPos();
                        final Direction direction = result.getFace();
                        if (controller.onPlayerDamageBlock(blockpos, direction)) {
                            mc.particles.addBlockHitEffects(blockpos, result);
                            player.swingArm(hand);
                            success = true;
                        }
                    }
                    if (success) {
                        this.cooldown = 10;
                    }
                }
            }
        };
    }
}
