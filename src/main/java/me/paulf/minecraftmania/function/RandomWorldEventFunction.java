package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.time.Duration;
import java.util.Random;

public class RandomWorldEventFunction extends DurationFunction {
    private static final int[] EVENTS = {
        1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007,
        1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015,
        1016, 1017, 1018, 1019, 1020, 1021, 1022, 1024,
        1025, 1026, 1027, 1029, 1030, 1031, 1032, 1033,
        1034, 1035, 1036, 1037, 1039, 1040, 1041, 1042,
        1043, 1500, 1501, 1502, 1503, 2000, 2001, 2002,
        2007, 2003, 2004, 2006, 2008, 3000, 3001
    };

    public RandomWorldEventFunction(final Duration duration) {
        super(duration);
    }

    @Override
    protected RunningFunction createFunction() {
        return new RunningFunction() {
            final Random rng = new Random();

            @Override
            public void tick() {
                if (!(this.rng.nextFloat() < 0.1F)) {
                    return;
                }
                final Minecraft mc = Minecraft.getInstance();
                final ClientWorld world = mc.world;
                final ClientPlayerEntity player = mc.player;
                if (world != null && player != null) {
                    final int r = 2;
                    world.playEvent(
                        EVENTS[this.rng.nextInt(EVENTS.length)],
                        new BlockPos(player).add(
                            this.rng.nextInt(2 * r + 1) - r, this.rng.nextInt(2 * r + 1) - r, this.rng.nextInt(2 * r + 1) - r
                        ),
                        this.rng.nextInt()
                    );
                }
            }
        };
    }
}
