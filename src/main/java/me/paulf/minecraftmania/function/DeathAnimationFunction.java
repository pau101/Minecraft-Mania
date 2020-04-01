package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.time.Duration;
import java.util.Collections;

public class DeathAnimationFunction extends DurationFunction {
    public DeathAnimationFunction(final Duration duration) {
        super(duration);
    }

    @Override
    protected RunningFunction createFunction() {
        return new RunningFunction() {
            @Override
            public void tick() {
                for (final Entity entity : this.entities()) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).deathTime = 1;
                    }
                }
            }

            @Override
            public void stop() {
                for (final Entity entity : this.entities()) {
                    if (entity instanceof LivingEntity && entity.isAlive()) {
                        ((LivingEntity) entity).deathTime = 0;
                    }
                }
            }

            private Iterable<Entity> entities() {
                final ClientWorld w = Minecraft.getInstance().world;
                return w == null ? Collections.emptyList() : w.getAllEntities();
            }
        };
    }
}
