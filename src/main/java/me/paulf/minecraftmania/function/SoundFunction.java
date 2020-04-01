package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class SoundFunction implements CommandFunction {
    private final Duration duration;

    private final Supplier<Function<ResourceLocation, Optional<SoundEvent>>> sound;

    public SoundFunction(final Duration duration, final Supplier<Function<ResourceLocation, Optional<SoundEvent>>> sound) {
        this.duration = duration;
        this.sound = sound;
    }

    @Override
    public void run(final MinecraftMania.CommandContext context) {
        context.addRunningFunction(this.duration, new MyRunningFunction(this.sound));
    }

    private static final class MyRunningFunction implements RunningFunction {
        private final Supplier<Function<ResourceLocation, Optional<SoundEvent>>> sound;

        private Function<ResourceLocation, Optional<SoundEvent>> factory;

        private MyRunningFunction(final Supplier<Function<ResourceLocation, Optional<SoundEvent>>> sound) {
            this.sound = sound;
            this.factory = sound.get();
        }

        @Override
        public void merge() {
            this.factory = this.sound.get();
        }

        @SubscribeEvent
        public void onPlaySound(final PlaySoundEvent e) {
            final ISound s = e.getSound();
            this.factory.apply(s.getSoundLocation()).ifPresent(sound -> {
                final float volume;
                final float pitch;
                if (s instanceof LocatableSound) {
                    //noinspection ConstantConditions
                    volume = ObfuscationReflectionHelper.getPrivateValue(LocatableSound.class, (LocatableSound) s, "field_147662_b");
                    //noinspection ConstantConditions
                    pitch = ObfuscationReflectionHelper.getPrivateValue(LocatableSound.class, (LocatableSound) s, "field_147663_c");
                } else {
                    volume = 1.0F;
                    pitch = 1.0F;
                }
                e.setResultSound(new SimpleSound(sound, s.getCategory(), volume, pitch, s.getX(), s.getY(), s.getZ()));
            });
        }
    }
}
